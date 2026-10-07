from scrapy.http import HtmlResponse
from searchhh.extraction import article_text, structured_rows
from searchhh.spider import OpportunitySpider, is_form
from searchhh.domain import opportunity

HTML = '''<html><head><title>Climate fellowship</title>
<script type="application/ld+json">{"@context":"https://example.invalid/not-fetched",
"@graph":[{"@type":"EducationalOccupationalProgram","name":"Climate fellowship",
"description":"Students can apply for the funded fellowship.","url":"/apply",
"validThrough":"2026-12-01","datePublished":"2025-01-01"},
{"@type":"Course","name":"Private fellowship","url":"http://127.0.0.1/private"}]}</script>
</head><body><nav>Navigation links</nav><main><h1>Climate fellowship</h1>
<p>Applications for this fellowship are open worldwide. Students can apply for a funded place.
Participants study climate science and build research skills during the programme.</p>
<p>The programme offers mentorship and practical research experience. Selection is competitive;
read the eligibility requirements and application deadline before you apply.</p>
<a href="https://forms.cloud.microsoft/r/test">Apply now</a></main></body></html>'''


def test_extruct_relative_links_types_and_evidence():
    rows = structured_rows(HTML, 'https://example.org/programme')
    assert len(rows) == 1
    assert rows[0]['url'] == 'https://example.org/apply'
    assert rows[0]['evidenceUrl'] == 'https://example.org/programme'
    assert 'validThrough: 2026-12-01' in rows[0]['content']
    record = opportunity(rows[0])
    assert record['evidenceUrl'] == rows[0]['evidenceUrl']
    assert record['verified'] is False


def test_trafilatura_extracts_evidence_not_script_data():
    text = article_text(HTML)
    assert 'mentorship' in text and 'not-fetched' not in text
    assert len(text) <= 2000


def test_scrapy_adapter_reuses_both_extractors():
    spider = OpportunitySpider(seeds=['https://example.org/programme'])
    rows = list(spider.parse(HtmlResponse('https://example.org/programme', body=HTML.encode(), encoding='utf-8')))
    assert any(r['engine'] == 'Extruct JSON-LD' for r in rows)
    assert any(r['engine'] == 'Scrapy' and 'mentorship' in r['content'] for r in rows)
    assert any(r['url'] == 'https://forms.cloud.microsoft/r/test' for r in rows)


def test_declared_profiles_select_bounded_extraction(monkeypatch):
    response = HtmlResponse('https://example.org/programme', body=HTML.encode(), encoding='utf-8')
    spider = OpportunitySpider(seeds=['https://example.org/programme'])

    monkeypatch.setenv('SEARCHHH_CRAWLER_PROFILE', 'phone-structured')
    structured = list(spider.parse(response))
    assert [row['engine'] for row in structured] == ['Extruct JSON-LD']

    monkeypatch.setenv('SEARCHHH_CRAWLER_PROFILE', 'phone-article')
    article = list(spider.parse(response))
    assert len(article) == 1 and article[0]['engine'] == 'Trafilatura profile'

    monkeypatch.setenv('SEARCHHH_CRAWLER_PROFILE', 'phone-selector')
    selector = list(spider.parse(response))
    assert len(selector) == 1 and selector[0]['engine'] == 'Parsel profile'


def test_invalid_and_oversized_payloads_and_form_guards():
    assert structured_rows('<script type="application/ld+json">broken</script>', 'https://example.org/') == []
    assert structured_rows(HTML, 'http://127.0.0.1/') == []
    assert article_text('a' * (2 * 1024 * 1024 + 1)) == ''
    assert is_form('https://forms.cloud.microsoft/r/test')
    assert not is_form('https://docs.google.com/forms-fake/')
