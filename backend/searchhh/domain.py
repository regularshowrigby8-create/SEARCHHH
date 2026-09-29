"""Opportunity policy on top of w3lib canonicalization; no search engine scraping."""
import hashlib
import html
from w3lib.html import remove_tags
import ipaddress
import re
from datetime import datetime, timezone
from urllib.parse import urlsplit, urlunsplit, parse_qsl, urlencode
from w3lib.url import canonicalize_url

SIGNALS = re.compile(r'\b(cohort|fellowship|bootcamp|scholarship|certification|certificate|applications?|enroll|enrol|register|opportunit\w*)\b', re.I)
BOT = re.compile(r'captcha|access denied|verify you are human|robot check|just a moment', re.I)

def public_url(url):
    try:
        p = urlsplit(url)
        host = (p.hostname or '').lower()
        if p.scheme not in ('https', 'http') or not host or p.username or p.password or p.port not in (None,80,443):
            return False
        if host in ('localhost','metadata.google.internal') or host.endswith(('.local','.internal','.localhost','.onion','.bot')):
            return False
        try:
            return ipaddress.ip_address(host.strip('[]')).is_global
        except ValueError:
            return '.' in host and not host.replace('.', '').isdigit()
    except ValueError:
        return False

def canonical(url):
    if not public_url(url):
        return None
    p = urlsplit(url)
    query = [(k,v) for k,v in parse_qsl(p.query,keep_blank_values=True) if not k.lower().startswith('utm_') and k.lower() not in ('fbclid','gclid','msclkid')]
    # Keep form IDs, response IDs, scheme, host and meaningful paths unchanged.
    return canonicalize_url(urlunsplit((p.scheme,p.netloc,p.path,urlencode(query),'')))

def opportunity(raw, mode='opportunities'):
    url = canonical(str(raw.get('url','')))
    title = html.unescape(remove_tags(str(raw.get('title') or url or '')))[:500]
    content = html.unescape(remove_tags(str(raw.get('content') or '')))[:2000]
    if not url or BOT.search(title):
        return None
    p = urlsplit(url)
    form = p.hostname in ('forms.gle','forms.office.com','forms.microsoft.com','forms.cloud.microsoft','forms.office365.com') or (p.hostname=='docs.google.com' and p.path.startswith('/forms/'))
    signals = sorted(set(m.group().lower() for m in SIGNALS.finditer(title+' '+content)))
    if mode=='opportunities' and not form and not signals:
        return None
    kind = 'Application form' if form else 'Cohort' if any(s in signals for s in ('cohort','bootcamp','fellowship')) else 'Certification' if any(s in signals for s in ('certificate','certification')) else 'Opportunity' if signals else 'Link'
    published = None
    # Date must come from the source, never substitute crawl/discovery time.
    try:
        date = datetime.fromisoformat(str(raw.get('publishedDate','')).replace('Z','+00:00'))
        date = date.replace(tzinfo=timezone.utc) if date.tzinfo is None else date
        if date <= datetime.now(timezone.utc):
            published = date.astimezone(timezone.utc).isoformat()
    except ValueError:
        pass
    return {'id':hashlib.sha256(url.encode()).hexdigest(),'url':url,'title':title,'description':content,'kind':kind,'signals':signals,'sources':list(raw.get('engines') or [raw.get('engine','Page crawler')]),'published':published,'discovered':datetime.now(timezone.utc).isoformat(),'score':min(100,(50 if form else 20)+10*len(signals)),'verified':False,'evidenceUrl':canonical(str(raw.get('evidenceUrl') or ''))}
