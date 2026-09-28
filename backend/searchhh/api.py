import json
import os
import secrets
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Literal
from uuid import uuid4
from fastapi import Depends, FastAPI, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, Field, field_validator
from redis import Redis
from rq import Queue
from sqlalchemy import select, func, text
from .store import Base, engine, Session, SearchJob, SearchResult

CATALOG = json.loads((Path(__file__).parent.parent/'config/engines.json').read_text())
ENGINE_IDS = {e['id'] for e in CATALOG}
redis = Redis.from_url(os.getenv('REDIS_URL','redis://localhost:6379/0'))
queue = Queue('searchhh',connection=redis)
security = HTTPBearer()

def authorized(credentials: HTTPAuthorizationCredentials = Depends(security)):
    expected = os.getenv('SEARCHHH_ACCESS_TOKEN','')
    if len(expected)<32:
        raise HTTPException(503,'Backend owner must configure a 32+ character access token')
    if not secrets.compare_digest(credentials.credentials,expected):
        raise HTTPException(401,'Invalid server access token')

@asynccontextmanager
async def lifespan(app):
    Base.metadata.create_all(engine)
    yield

app = FastAPI(title='Searchhh',version='0.1.0',lifespan=lifespan,description='Private, keyless public-web discovery. Source keys are not needed; the access token protects your own server.')

class StartSearch(BaseModel):
    query: str = Field(min_length=2,max_length=240)
    mode: Literal['opportunities','links'] = 'opportunities'
    engines: list[str] = Field(min_length=1,max_length=33)
    crawl: bool = False
    @field_validator('query')
    @classmethod
    def valid_query(cls,value):
        if len(value.strip())<2:
            raise ValueError('Enter a topic')
        return value.strip()
    @field_validator('engines')
    @classmethod
    def valid_engines(cls,value):
        if not set(value)<=ENGINE_IDS:
            raise ValueError('Unknown or credential-requiring source')
        return list(dict.fromkeys(value))

@app.get('/health')
def health():
    with engine.connect() as conn:
        conn.execute(text('SELECT 1'))
    redis.ping()
    return {'status':'ok','source_count':len(CATALOG)}

@app.get('/v1/engines',dependencies=[Depends(authorized)])
def engines():
    return CATALOG

@app.post('/v1/jobs',status_code=201,dependencies=[Depends(authorized)])
def start(request: StartSearch):
    # One active swarm per private deployment; bound resource use and avoid accidental fan-out.
    with redis.lock('searchhh:create',timeout=10,blocking_timeout=2):
        with Session.begin() as db:
            if db.scalar(select(func.count()).select_from(SearchJob).where(SearchJob.status.in_(['queued','running']))):
                raise HTTPException(409,'Stop the existing swarm first')
            job = SearchJob(id=str(uuid4()),query=request.query,mode=request.mode,engines=request.engines,crawl=int(request.crawl))
            db.add(job)
        try:
            queue.enqueue('searchhh.worker.run_round',job.id,job_timeout=240)
        except Exception:
            with Session.begin() as db:
                db.get(SearchJob,job.id).status='failed'
            raise HTTPException(503,'Queue unavailable; search was not started')
    return {'id':job.id,'status':'queued'}

@app.get('/v1/jobs/{job_id}',dependencies=[Depends(authorized)])
def status(job_id: str):
    with Session() as db:
        job=db.get(SearchJob,job_id)
        if not job: raise HTTPException(404,'Search not found')
        rows=list(db.scalars(select(SearchResult).where(SearchResult.job_id==job_id)))
        results=sorted([r.payload for r in rows],key=lambda r:(r['published'] is not None,r['published'] or '',r['score']),reverse=True)
        return {'id':job.id,'status':job.status,'round':job.round,'duplicates':job.duplicates,'filtered':job.filtered,'errors':job.errors,'results':results}

@app.post('/v1/jobs/{job_id}/stop',dependencies=[Depends(authorized)])
def stop(job_id: str):
    with Session.begin() as db:
        job=db.scalar(select(SearchJob).where(SearchJob.id==job_id).with_for_update())
        if not job: raise HTTPException(404,'Search not found')
        job.status='stopped'
    # A running source request can finish, but no subsequent results may be persisted.
    return {'id':job_id,'status':'stopped'}
