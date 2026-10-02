#!/usr/bin/env python3
"""Require traceable action/state tests, fresh JUnit outcomes and requested visual evidence."""
from datetime import datetime, timezone
import json
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
REQUIRED_SCENARIOS = {
    'search-start', 'search-stop-cancels', 'search-filters', 'save-restart-persistence',
    'result-details-preserve-id', 'drawer-open-close', 'all-drawer-routes', 'back-navigation',
    'unknown-route-safe', 'theme-persistence', 'browser-open-url', 'browser-back-forward',
    'browser-reload-close', 'unsupported-url-rejected', 'https-default-http-approval',
    'invalid-tls-cancelled', 'native-bridge-origin-safety', 'loading-state', 'empty-state',
    'error-retry', 'offline-recovery', 'expired-results', 'duplicate-results',
    'accessibility-labels-touch-targets', 'disabled-control-explanation', 'rotation-restoration',
    'inventory-manual-closure', 'performance-baseline', 'history-route', 'live-search-route', 'material3-theme',
    'visual-home-light', 'visual-home-dark', 'visual-drawer', 'visual-loading', 'visual-results',
    'visual-empty', 'visual-error', 'visual-filter', 'visual-saved', 'visual-browser', 'visual-settings',
}


def repository_file(root, value):
    path = (root / value).resolve()
    if not path.is_relative_to(root.resolve()) or not path.is_file():
        raise ValueError(f'Invalid repository evidence file: {value}')
    return path


def validate_contract(root, key, contract, passed):
    errors = []
    if not isinstance(contract, dict):
        return [f'{key}: contract missing']
    required = ['source', 'route', 'action', 'state', 'test_file', 'test_class', 'test_method']
    for field in required:
        if not isinstance(contract.get(field), str) or not contract[field].strip():
            errors.append(f'{key}: missing {field}')
    if errors:
        return errors
    try:
        repository_file(root, contract['source'])
        test = repository_file(root, contract['test_file']).read_text()
        # This checks supporting evidence, not logical correctness. Human review remains required.
        from tools.quality.unfinished import closing, masked
        code, _ = masked(test)
        name = re.escape(contract['test_method'])
        match = re.search(r'\b(?:fun|void)\s+`?' + name + r'`?\s*\([^)]*\)[^{\n]*\{', code)
        body = ''
        if match:
            end = closing(code, match.end() - 1)
            if end is not None:
                body = code[match.end():end]
        if not body:
            errors.append(f'{key}: declared behavior method body absent or unsupported')
        actions = list(re.finditer(r'perform(?:Click|TextInput|TouchInput|ScrollTo)|\.onReceivedSslError\(|\.fromId\(|\.start\(|\.stop\(', body))
        assertions = list(re.finditer(r'assert\w*\s*\(|verify\s*\(', body))
        if not actions or not any(assertion.start() > action.start() for action in actions for assertion in assertions):
            errors.append(f'{key}: declared method needs an action followed by an outcome assertion')
        if (contract['test_class'], contract['test_method']) not in passed:
            errors.append(f'{key}: no fresh, passing, non-skipped JUnit result')
        if key.startswith('UI-') and not contract.get('tag'):
            errors.append(f'{key}: stable semantic tag missing')
        if key.startswith('visual-'):
            for field in ['before', 'after']:
                image = repository_file(root, contract.get(field, ''))
                if image.read_bytes()[:8] != b'\x89PNG\r\n\x1a\n':
                    errors.append(f'{key}: {field} is not PNG evidence')
            for field in ['before_source_sha', 'after_source_sha', 'comparison_test']:
                if not contract.get(field):
                    errors.append(f'{key}: missing {field}')
    except (OSError, ValueError, TypeError) as error:
        errors.append(f'{key}: {error}')
    return errors


def junit_results(paths, started):
    passed = set()
    for path in paths:
        tree = ET.parse(path)
        for suite in tree.iter('testsuite'):
            raw = suite.get('timestamp')
            if not raw:
                continue
            timestamp = datetime.fromisoformat(raw.replace('Z', '+00:00'))
            if timestamp.tzinfo is None:
                timestamp = timestamp.replace(tzinfo=timezone.utc)
            if timestamp < started or timestamp > datetime.now(timezone.utc):
                continue
            for case in suite.findall('testcase'):
                if not any(case.find(kind) is not None for kind in ['failure', 'error', 'skipped']):
                    passed.add((case.get('classname'), case.get('name')))
    return passed


def main():
    from tools.quality.interactions import discover
    errors = []
    passed = set()
    try:
        metadata = json.loads((ROOT / 'build/reports/searchhh/run.json').read_text())
        sha = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
        if metadata['sha'] != sha:
            raise ValueError('JUnit run metadata is for a different commit')
        started = datetime.fromisoformat(metadata['started_at'])
        now = datetime.now(timezone.utc)
        if started.tzinfo is None or not 0 <= (now - started).total_seconds() <= 7200:
            raise ValueError('JUnit run metadata must be timezone-aware and less than two hours old')
        paths = list((ROOT / 'app/build/test-results').rglob('*.xml')) + list((ROOT / 'app/build/outputs/androidTest-results').rglob('*.xml'))
        passed = junit_results(paths, started)
    except (OSError, ValueError, KeyError, ET.ParseError, subprocess.CalledProcessError) as error:
        errors.append(f'Execution evidence unavailable: {error}')
    contracts = json.loads((ROOT / 'quality/interaction-contracts.json').read_text())
    required = json.loads((ROOT / 'quality/required-scenarios.json').read_text())
    for row in discover(ROOT):
        errors.extend(validate_contract(ROOT, row['id'], contracts.get(row['id']), passed))
    for key in sorted(REQUIRED_SCENARIOS):
        errors.extend(validate_contract(ROOT, key, required.get(key), passed))
    report = ROOT / 'build/reports/searchhh/interaction-evidence.json'
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text(json.dumps({'status': 'FAIL' if errors else 'PASS', 'fresh_passing_tests': len(passed), 'errors': errors}, indent=2) + '\n')
    print(f'{len(errors)} missing/invalid evidence requirements. Full report: {report.relative_to(ROOT)}')
    for error in errors[:20]:
        print(error)
    return int(bool(errors))


if __name__ == '__main__':
    sys.path.insert(0, str(ROOT))
    sys.exit(main())
