import os
from contextlib import nullcontext
from unittest.mock import patch
from fastapi.testclient import TestClient
from searchhh.api import app
from searchhh.store import Base, engine, Session, SearchJob, SearchResult
from searchhh.worker import run_round
from searchhh.domain import opportunity

HEADERS={'Authorization':'Bearer '+os.environ['SEARCHHH_ACCESS_TOKEN']}

def test_authenticated_start_status_stop_and_dedup():
    Base.metadata.drop_all(engine)
    with TestClient(app) as client, patch('searchhh.api.redis') as redis, patch('searchhh.api.queue') as queue:
        redis.lock.return_value=nullcontext()
        assert client.get('/v1/engines').status_code==401
        assert client.get('/v1/engines',headers={'Authorization':'Bearer invalid'}).status_code==401
        assert len(client.get('/v1/engines',headers=HEADERS).json())==33
        assert client.post('/v1/jobs',headers=HEADERS,json={'query':'hi','engines':['fake']}).status_code==422
        response=client.post('/v1/jobs',headers=HEADERS,json={'query':'free cohort','engines':['github'],'crawl':False})
        assert response.status_code==201
        job_id=response.json()['id'];queue.enqueue.assert_called_once()
        assert client.post('/v1/jobs',headers=HEADERS,json={'query':'other','engines':['github']}).status_code==409
        with patch('searchhh.worker.httpx.Client') as http, patch('searchhh.worker.Queue'):
            http.return_value.__enter__.return_value.get.return_value.json.return_value={'results':[{'url':'https://forms.gle/abc?utm_source=test','title':'Free cohort'},{'url':'https://forms.gle/abc','title':'Free cohort'}],'unresponsive_engines':[['bing','rate limited']]}
            run_round(job_id)
        state=client.get('/v1/jobs/'+job_id,headers=HEADERS).json()
        assert len(state['results'])==1 and state['duplicates']==1 and state['round']==1
        assert state['errors']==['bing: rate limited']
        assert client.post('/v1/jobs/'+job_id+'/stop',headers=HEADERS).json()['status']=='stopped'
        with patch('searchhh.worker.httpx.Client') as http:
            run_round(job_id);http.assert_not_called()
        assert client.get('/v1/jobs/missing',headers=HEADERS).status_code==404

def test_stop_during_source_request_does_not_commit():
    Base.metadata.drop_all(engine);Base.metadata.create_all(engine)
    with Session.begin() as db: db.add(SearchJob(id='mid-flight',query='cohort',mode='opportunities',engines=['github']))
    def stop_on_response():
        with Session.begin() as db:db.get(SearchJob,'mid-flight').status='stopped'
        return {'results':[{'url':'https://forms.gle/abc','title':'Apply'}]}
    with patch('searchhh.worker.httpx.Client') as http, patch('searchhh.worker.Queue') as queue:
        http.return_value.__enter__.return_value.get.return_value.json.side_effect=stop_on_response
        run_round('mid-flight');queue.assert_not_called()
    with Session() as db: assert db.query(SearchResult).count()==0
