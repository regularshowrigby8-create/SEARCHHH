"""RQ orchestration of SearXNG and Scrapy. No provider scraper is reimplemented."""
import json
import os
import subprocess
import sys
import tempfile
import time
from datetime import timedelta
import httpx
from redis import Redis
from rq import Queue
from sqlalchemy import select, func
from .domain import opportunity
from .store import Session, SearchJob, SearchResult

INTENTS=['','cohort application','certification enrollment','site:forms.gle','site:forms.office.com','site:docs.google.com/forms','fellowship scholarship apply','site:forms.microsoft.com']

def active(job_id):
    with Session() as db:
        job=db.get(SearchJob,job_id)
        return bool(job and job.status in ('queued','running'))

def crawl(seeds,job_id):
    with tempfile.TemporaryDirectory() as temp:
        output=os.path.join(temp,'links.json')
        proc=subprocess.Popen([sys.executable,'-m','searchhh.spider',json.dumps(seeds),output],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
        deadline=time.monotonic()+90
        try:
            while proc.poll() is None:
                if not active(job_id) or time.monotonic()>deadline:
                    proc.terminate();proc.wait(timeout=5);return [],['Page crawl stopped or timed out']
                time.sleep(.5)
            if proc.returncode: return [],['Scrapy process failed; no crawl results used']
            return json.load(open(output)) if os.path.exists(output) else [],[]
        finally:
            if proc.poll() is None: proc.kill();proc.wait()

def run_round(job_id):
    with Session.begin() as db:
        job=db.scalar(select(SearchJob).where(SearchJob.id==job_id).with_for_update())
        if not job or job.status not in ('queued','running'): return
        job.status='running'; query=job.query
        if job.mode=='opportunities': query+=' '+INTENTS[job.round%len(INTENTS)]
        page=1+(job.round//len(INTENTS))%5
        selected=job.engines; mode=job.mode; crawl_enabled=bool(job.crawl)
    raw=[]; errors=[]
    try:
        with httpx.Client(timeout=25,trust_env=False) as client:
            response=client.get(os.getenv('SEARXNG_URL','http://searxng:8080')+'/search',params={'q':query,'format':'json','engines':','.join(selected),'pageno':page,'language':'en'})
            response.raise_for_status(); data=response.json()
            raw=data.get('results',[])[:200]
            errors=[f'{item[0]}: {item[1]}' for item in data.get('unresponsive_engines',[])]
    except Exception as exc:
        errors=[f'SearXNG unavailable ({type(exc).__name__}); retry scheduled']
    if not active(job_id): return
    if crawl_enabled:
        seeds=[r['url'] for r in raw if opportunity(r,'opportunities')][:8]
        if seeds:
            found,crawl_errors=crawl(seeds,job_id);raw.extend(found);errors.extend(crawl_errors)
    with Session.begin() as db:
        job=db.scalar(select(SearchJob).where(SearchJob.id==job_id).with_for_update())
        if not job or job.status not in ('queued','running'):return
        count=db.scalar(select(func.count()).select_from(SearchResult).where(SearchResult.job_id==job_id))
        for item in raw:
            result=opportunity(item,mode)
            if not result:job.filtered+=1;continue
            existing=db.get(SearchResult,(job_id,result['id']))
            if existing:
                job.duplicates+=1
                merged=dict(existing.payload);merged['sources']=sorted(set(merged['sources']+result['sources']))
                existing.payload=merged
            elif count<5000:
                db.add(SearchResult(job_id=job_id,key=result['id'],payload=result));db.flush();count+=1
        job.round+=1;job.errors=errors[:40]
        if count>=5000:
            job.status='capacity';job.errors=errors+['5,000-result safety capacity reached. Start a new session to continue.']
    if active(job_id):
        try:
            conn=Redis.from_url(os.getenv('REDIS_URL','redis://redis:6379/0'))
            Queue('searchhh',connection=conn).enqueue_in(timedelta(seconds=120 if errors else 60),'searchhh.worker.run_round',job_id,job_timeout=240)
        except Exception:
            with Session.begin() as db:
                job=db.get(SearchJob,job_id)
                if job.status=='running':job.status='failed';job.errors=['Could not schedule next pass; restart this search']
