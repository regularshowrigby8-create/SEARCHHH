"""Optional server adapter: reuse Extruct and Trafilatura on already-fetched HTML.
No network, browser, JSON-LD context resolution, form submission or AI calls here.
"""
from collections import deque
from urllib.parse import urljoin
import extruct
import trafilatura
from w3lib.html import remove_tags
from .domain import public_url

TYPES = {'JobPosting', 'Course', 'CourseInstance', 'EducationalOccupationalProgram',
         'Event', 'EducationEvent'}


def article_text(html):
    if len(html) > 2 * 1024 * 1024:
        return ''
    return (trafilatura.extract(html, include_comments=False, include_tables=False,
                               include_links=False, favor_precision=True) or '')[:2000]


def structured_rows(html, base):
    if len(html) > 2 * 1024 * 1024 or not public_url(base):
        return []
    try:
        extracted = extruct.extract(html, base_url=base, syntaxes=['json-ld'], errors='ignore')
    except (ValueError, TypeError, RecursionError):
        return []
    queue = deque(extracted.get('json-ld', [])[:30])
    rows, seen = [], set()
    budget = 300
    while queue and budget and len(rows) < 30:
        budget -= 1
        obj = queue.popleft()
        if isinstance(obj, list):
            queue.extend(obj[:100])
            continue
        if not isinstance(obj, dict):
            continue
        types = obj.get('@type', [])
        types = [types] if isinstance(types, str) else types
        recognized = isinstance(types, list) and any(
            isinstance(t, str) and t.rsplit('/', 1)[-1].rsplit('#', 1)[-1] in TYPES for t in types)
        if recognized:
            title = obj.get('name') or obj.get('title')
            raw_url = obj.get('url', base)
            if isinstance(raw_url, dict):
                raw_url = raw_url.get('@id')
            if isinstance(title, str) and isinstance(raw_url, str):
                url = urljoin(base, raw_url)
                if public_url(url) and url not in seen:
                    seen.add(url)
                    description = obj.get('description', '')
                    description = remove_tags(description)[:1500] if isinstance(description, str) else ''
                    fields = '; '.join(f'{k}: {obj[k][:100]}' for k in
                                       ('validThrough', 'startDate', 'endDate') if isinstance(obj.get(k), str))
                    rows.append({'url': url, 'title': title[:500],
                                 'content': (description + ' ' + fields).strip(),
                                 'engine': 'Extruct JSON-LD', 'evidenceUrl': base,
                                 'publishedDate': obj.get('datePosted') or obj.get('datePublished')})
        for key in ('@graph', 'mainEntity', 'itemListElement', 'item', 'hasCourseInstance'):
            if isinstance(obj.get(key), (dict, list)):
                queue.append(obj[key])
    return rows
