import json
from pathlib import Path
import pytest
from searchhh.domain import public_url, canonical, opportunity
from searchhh.api import StartSearch, CATALOG
from searchhh.spider import OpportunitySpider, PublicOnly, PublicResolver, is_form
from scrapy import Request
from scrapy.exceptions import IgnoreRequest
from scrapy.http import HtmlResponse

@pytest.mark.parametrize('url',['http://127.0.0.1/a','http://10.1.2.3','http://169.254.169.254','https://[::1]/','https://a.onion','https://a.bot','file:///etc/passwd','https://user:pass@public.org','http://localhost','https://example.org:3000','http://2130706433'])
def test_private_urls_blocked(url):
    assert not public_url(url)
    with pytest.raises(IgnoreRequest): PublicOnly().process_request(Request(url),None)

def test_canonical_preserves_form_identity():
    assert canonical('https://forms.office.com/r/ABC?utm_source=foo#section')=='https://forms.office.com/r/ABC'
    assert canonical('https://docs.google.com/forms/d/ABC/viewform?entry.1=a') != canonical('https://docs.google.com/forms/d/ABC/viewform?entry.1=b')

def test_opportunity_filter_and_unknown_date():
    assert opportunity({'url':'https://example.org','title':'A general page'}) is None
    assert opportunity({'url':'https://example.org','title':'A general page'},'links')
    result=opportunity({'url':'https://forms.gle/abc','title':'Apply'})
    assert result['kind']=='Application form' and result['published'] is None and not result['verified']
    assert opportunity({'url':'https://example.org','title':'Just a moment'}) is None

def test_future_date_not_newest():
    assert opportunity({'url':'https://example.org','title':'Free cohort','publishedDate':'2099-01-01T00:00:00Z'})['published'] is None

def test_33_unique_sources_match_android_and_searxng():
    import yaml
    root=Path(__file__).parents[2]
    assert len(CATALOG)==33 and len({x['id'] for x in CATALOG})==33
    assert json.loads((root/'app/src/main/assets/searchhh-engines.json').read_text())==CATALOG
    settings=yaml.safe_load((root/'backend/config/searxng.yml').read_text())
    assert set(settings['use_default_settings']['engines']['keep_only'])=={x['id'] for x in CATALOG}

def test_unknown_source_rejected():
    with pytest.raises(ValueError): StartSearch(query='cohort',engines=['imaginary-ai'])

def test_spider_discovers_without_submitting_forms():
    spider=OpportunitySpider(seeds=['https://forms.gle/x','http://127.0.0.1','https://example.org'])
    assert spider.start_urls==['https://example.org']
    response=HtmlResponse(url='https://example.org',body=b'<html><title>Cohort</title><a href="https://forms.office.com/r/abc">Apply</a><a href="http://127.0.0.1/apply">Apply</a><form action="/submit"></form></html>',encoding='utf-8')
    rows=list(spider.parse(response))
    assert len(rows)==2 and all(isinstance(row,dict) for row in rows)
    assert rows[1]['url']=='https://forms.office.com/r/abc'
    assert spider.custom_settings['ROBOTSTXT_OBEY'] and not spider.custom_settings['REDIRECT_ENABLED']
