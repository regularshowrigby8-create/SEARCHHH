"""CI-only real-service smoke test. Does not assert a provider always returns hits."""
import json
import subprocess
import time
from pathlib import Path
import httpx

values=dict(line.split('=',1) for line in Path('backend/.env').read_text().splitlines() if '=' in line)
client=httpx.Client(base_url='http://127.0.0.1:8000',headers={'Authorization':'Bearer '+values['SEARCHHH_ACCESS_TOKEN']},timeout=10)
for attempt in range(90):
    try:
        if client.get('/health').status_code==200: break
    except httpx.HTTPError: pass
    time.sleep(2)
else: raise AssertionError('Backend dependencies did not become healthy')
assert len(client.get('/v1/engines').json())==33
# Verify SearXNG loaded the configured engines, not just a static app catalog.
cmd=['docker','compose','--env-file','.env','exec','-T','api','python','-c',"import httpx,json; r=httpx.get('http://searxng:8080/config',timeout=20); r.raise_for_status(); print(json.dumps([e['name'] for e in r.json()['engines']]))"]
for attempt in range(30):
    result=subprocess.run(cmd,cwd='backend',capture_output=True,text=True)
    if result.returncode==0: break
    time.sleep(2)
else: raise AssertionError('SearXNG did not become ready: '+result.stderr[-1000:])
loaded=set(json.loads(result.stdout)); requested={e['id'] for e in json.loads(Path('backend/config/engines.json').read_text())}
assert requested <= loaded, f'Missing upstream engines: {requested-loaded}'
created=client.post('/v1/jobs',json={'query':'cohort','mode':'links','engines':['github'],'crawl':False})
assert created.status_code==201,created.text
job=created.json()['id']
for attempt in range(45):
    state=client.get('/v1/jobs/'+job).json()
    if state['round']>=1: break
    time.sleep(2)
else: raise AssertionError('RQ worker never completed a source pass')
assert not any('SearXNG unavailable' in e for e in state['errors']),state['errors']
print('Live pass:',len(state['results']),'results; source status:',state['errors'])
assert client.post('/v1/jobs/'+job+'/stop').json()['status']=='stopped'
assert client.get('/v1/jobs/'+job).json()['status']=='stopped'
print('PostgreSQL + Redis + RQ + 33-adapter SearXNG integration: passed')
