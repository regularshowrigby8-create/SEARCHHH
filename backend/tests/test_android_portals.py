"""Static Android catalog integrity, distinct from the legacy SearXNG catalog."""
import json
from pathlib import Path
from urllib.parse import urlsplit

def test_global_portal_seed_catalog():
    root = Path(__file__).parents[2]
    rows = json.loads((root / 'app/src/main/assets/searchhh-portals.json').read_text())
    assert len(rows) >= 100
    assert len({r['id'] for r in rows}) == len(rows)
    assert len({r['url'] for r in rows}) == len(rows)
    assert any(r['default'] and r['format'] == 'feed' for r in rows)
    for row in rows:
        url = urlsplit(row['url'])
        assert url.scheme == 'https' and url.hostname and not url.username
        assert row['format'] in ('feed', 'html')
        assert row['scope'].startswith('International')
        assert row['evidence'] == row['url']
        assert url.hostname not in ('google.com', 'duckduckgo.com', 'bing.com')
