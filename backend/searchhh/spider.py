"""Small Scrapy adapter. Scrapy owns scheduling, robots, throttling and parsing."""
import ipaddress
import json
import sys
from urllib.parse import urlsplit
import scrapy
from scrapy.crawler import CrawlerProcess
from scrapy.exceptions import IgnoreRequest
from scrapy.resolver import CachingThreadedResolver
from .domain import public_url, SIGNALS, BOT

class PublicResolver(CachingThreadedResolver):
    def getHostByName(self, name, timeout=()):
        def check(address):
            if not ipaddress.ip_address(address).is_global:
                raise OSError('Non-public DNS address blocked')
            return address
        return super().getHostByName(name,timeout).addCallback(check)

class PublicOnly:
    def process_request(self, request, spider):
        if not public_url(request.url):
            raise IgnoreRequest('Non-public URL blocked')

class OpportunitySpider(scrapy.Spider):
    name='searchhh_opportunities'
    custom_settings={
        'ROBOTSTXT_OBEY':True,'USER_AGENT':'SearchhhBot/0.1 (+https://github.com/regularshowrigby8-create/SEARCHHH)',
        'CONCURRENT_REQUESTS':4,'CONCURRENT_REQUESTS_PER_DOMAIN':1,'DOWNLOAD_DELAY':3,
        'AUTOTHROTTLE_ENABLED':True,'AUTOTHROTTLE_TARGET_CONCURRENCY':1,
        'DEPTH_LIMIT':1,'CLOSESPIDER_PAGECOUNT':12,'CLOSESPIDER_TIMEOUT':75,
        'DOWNLOAD_TIMEOUT':12,'DOWNLOAD_MAXSIZE':2*1024*1024,'RETRY_ENABLED':False,
        'COOKIES_ENABLED':False,'REDIRECT_ENABLED':False,'METAREFRESH_ENABLED':False,
        'TWISTED_DNS_RESOLVER':'searchhh.spider.PublicResolver',
        'DOWNLOADER_MIDDLEWARES':{'searchhh.spider.PublicOnly':50},'LOG_LEVEL':'ERROR',
    }
    def __init__(self, seeds, **kwargs):
        super().__init__(**kwargs)
        self.start_urls=[]
        for url in seeds[:8]:
            if public_url(url) and not is_form(url): self.start_urls.append(url)

    def parse(self,response):
        if not isinstance(response,scrapy.http.TextResponse): return
        title=response.css('title::text').get('')
        if BOT.search(title): return
        description=response.css('meta[name="description"]::attr(content)').get('')
        yield {'url':response.url,'title':title,'content':description,'engine':'Scrapy','publishedDate':response.css('meta[property="article:published_time"]::attr(content)').get()}
        for a in response.css('a[href]')[:200]:
            url=response.urljoin(a.attrib['href'])
            text=' '.join(a.css('::text').getall()).strip()
            if public_url(url) and (is_form(url) or SIGNALS.search(text+' '+url)):
                # Discover links without fetching form contents, submitting, or recursive expansion.
                yield {'url':url,'title':text or 'Application link','content':'Discovered on '+urlsplit(response.url).hostname+'. Application status is unverified.','engine':'Scrapy'}

def is_form(url):
    p=urlsplit(url)
    return p.hostname in ('forms.gle','forms.office.com','forms.microsoft.com') or (p.hostname=='docs.google.com' and p.path.startswith('/forms'))

if __name__=='__main__':
    seeds=json.loads(sys.argv[1]); output=sys.argv[2]
    # DNS resolver is process-wide; spider settings alone do not install it.
    process=CrawlerProcess({**OpportunitySpider.custom_settings,'FEEDS':{output:{'format':'json','overwrite':True}}})
    process.crawl(OpportunitySpider,seeds=seeds)
    process.start()
