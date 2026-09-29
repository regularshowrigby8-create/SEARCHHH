"""Catalogue integrity, not compilation or operational testing of 120 frameworks."""
import hashlib
import json
import re
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[2]
DATA = json.loads((ROOT / 'app/src/main/assets/searchhh-codebases.json').read_text())
ROWS = DATA['entries']
KEYS = {'robotsTxt', 'rateLimits', 'politenessDelays', 'maxDepth', 'domainScope'}


def test_every_submitted_entry_and_url_is_preserved():
    original = ROWS[:100]
    assert [r['number'] for r in original] == list(range(1, 101))
    lines = (ROOT / 'tools/crawlers/codebases.tsv').read_text().splitlines()
    for row, line in zip(ROWS, lines, strict=True):
        name, language, url, _ = line.split('|')
        assert (row['name'], row['submittedLanguage'], row['submittedUrl']) == (name, language, url)
        assert row['original'] == (row['number'] <= 100)


def test_twenty_additions_are_located_separate_codebases():
    assert len(ROWS) == 120
    assert len({r['number'] for r in ROWS}) == len(ROWS)
    additions = ROWS[100:]
    assert all(r['repositoryStatus'] == 'github_metadata_verified' for r in additions)
    assert len({r['canonicalUrl'] for r in ROWS if r['canonicalUrl']}) == sum(bool(r['canonicalUrl']) for r in ROWS)


def test_five_requested_options_preserved_without_fabricating_support():
    assert DATA['requestedIgnoreProfile'] == dict.fromkeys(KEYS, True)
    assert DATA['requestedProfileIsRuntimeConfig'] is False
    for row in ROWS:
        assert set(row['configurationSupport']) == KEYS
        assert all(isinstance(v, str) and v for v in row['configurationSupport'].values())
    assert 'ROBOTSTXT_OBEY' in ROWS[0]['configurationSupport']['robotsTxt']
    assert ROWS[44]['configurationSupport']['robotsTxt'] == 'not_a_crawler_policy_interface'


def test_broken_entries_are_retained_and_not_given_guessed_launch_links():
    for n in (50, 78, 92):
        row = ROWS[n-1]
        assert row['canonicalUrl'] is None
        assert row['repositoryStatus'] in ('not_found', 'malformed_url')
        assert row['integration'] == 'catalog_only'
        assert row['note']


def test_verified_metadata_includes_source_revision_and_legal_caveat():
    for row in ROWS:
        if row['repositoryStatus'] == 'github_metadata_verified':
            assert re.fullmatch('[0-9a-f]{40}', row['revision'])
            assert row['revision'] in row['sourceUrl']
            assert row['evidenceUrl'].startswith('https://api.github.com/repos/')
        for key in ('canonicalUrl', 'sourceUrl'):
            if row.get(key):
                u = urlsplit(row[key])
                assert u.scheme == 'https' and u.hostname and not u.username
    assert 'licensing statements differ' in ROWS[20]['note']


def test_runtime_integrations_are_not_inflated_and_security_tools_stay_catalogued():
    assert {r['number'] for r in ROWS if r['integration'] != 'catalog_only'} == {1, 65, 66, 67}
    assert all('not_android' in r['executionTarget'] for r in ROWS if r['integration'] != 'catalog_only')
    assert all(r['integration'] == 'catalog_only' for r in ROWS if r['category'] in ('secret_scanner', 'security_suite', 'content_enumeration'))


def test_report_includes_every_entry_and_portals_remain_separate():
    report = (ROOT / 'docs/searchhh/CRAWLER-AUDIT.md').read_text()
    for row in ROWS:
        assert f"| {row['number']} | {row['name']} |" in report
    portals = json.loads((ROOT / 'app/src/main/assets/searchhh-portals.json').read_text())
    assert len(portals) == 110
    assert not set(p['id'] for p in portals) & {f'codebase:{r["number"]}' for r in ROWS}
