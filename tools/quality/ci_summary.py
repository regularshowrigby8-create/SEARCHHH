#!/usr/bin/env python3
"""Publish compact diagnostics through check annotations when log downloads are unavailable."""
import base64
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET



def lint_reports(root, diagnostics):
    reports = []
    for module in ('app', 'ad-filter', 'adblock-client'):
        for path in sorted((root / module / 'build/reports').glob('lint-results-*.xml')):
            try:
                tree = ET.parse(path)
            except ET.ParseError as error:
                diagnostics.append(f'Unparseable lint report {path}: {error}')
                continue
            groups = {}
            for issue in tree.iter('issue'):
                key = (issue.get('id', 'unknown'), issue.get('severity', 'unknown'))
                if key not in groups:
                    location = issue.find('location')
                    file = location.get('file', '') if location is not None else ''
                    groups[key] = {
                        'id': key[0], 'severity': key[1], 'count': 0,
                        'priority': int(issue.get('priority', '0')),
                        'file': file.removeprefix(str(root) + '/'),
                        'line': location.get('line') if location is not None else None,
                    }
                groups[key]['count'] += 1
            reports.append({'report': str(path.relative_to(root)), 'groups': sorted(
                groups.values(), key=lambda group: (-group['priority'], -group['count'], group['id']))})
    return reports


def summarize(root):
    diagnostics = []
    for log in sorted(root.glob('quality-*.log')):
        for line in log.read_text(errors='replace').splitlines():
            active_border = any(name + ':' in line for name in (
                'EdgeBorderRenderingTest.kt', 'LegacyEdgeBorderRenderer.kt',
                'EdgeDashEffectCache.kt', 'ThemedEdgeBorderView.kt',
                'SupernoteStorage.kt', 'SupernoteStorageTest.kt',
                'ExifImageFixtures.kt', 'ExifImagePipelineTest.kt'))
            if active_border or re.search(r'^e:|^w:|^(?:ERROR: )?Missing class |^> Task .* FAILED|Execution failed for task|Lint found \d+ errors|weighted issues', line):
                diagnostics.append(line[:500])
    lint = lint_reports(root, diagnostics)
    results = []
    for module in ['app', 'ad-filter', 'adblock-client']:
        for directory in ['test-results', 'outputs/androidTest-results']:
            for path in sorted((root / module / 'build' / directory).rglob('*.xml')):
                try:
                    tree = ET.parse(path)
                    for case in tree.iter('testcase'):
                        outcome = next((kind for kind in ['failure', 'error', 'skipped'] if case.find(kind) is not None), 'passed')
                        result = {'class': case.get('classname'), 'method': case.get('name'), 'outcome': outcome}
                        if outcome in ('failure', 'error'):
                            node = case.find(outcome)
                            detail = node.get('message') or node.text or 'JUnit failure has no detail'
                            result['detail'] = ' '.join(detail.split())[:800]
                        results.append(result)
                except ET.ParseError as error:
                    diagnostics.append(f'Unparseable JUnit report {path}: {error}')
    counts = {state: sum(r['outcome'] == state for r in results) for state in ['passed', 'failure', 'error', 'skipped']}
    focused = [r for r in results if r['class'] and any(name in r['class'] for name in ['SearchhhRouteTest', 'WebViewSslHandlerTest', 'NavigationBehaviorTest', 'BrowserMemoryTrimmerTest', 'ImageCacheTrimPolicyTest', 'ImageCacheBehaviorTest', 'SearchhhRobotsTest', 'KtorAndroidCompatibilityTest', 'InternalBackendTest', 'ComposeDialogWindowTest', 'EdgeBorderRenderingTest', 'SupernoteStorageTest', 'ExifImagePipelineTest'])]
    focused += [r for r in results if r['method'] in ['encryptedPreferencesReopenWithoutPlaintextCredentials', 'localeScriptAndRegionSurvivePersistence', 'legacyLanguageAndUnderscoreLocalesRemainReadable']]
    diagnostics.extend(f"Test {r['outcome']}: {r['class']}.{r['method']}: {r['detail']}"
                       for r in focused if 'detail' in r)
    rendered = any(r['class'] == 'info.plateaukao.einkbro.view.EdgeBorderRenderingTest'
                   and r['method'] == 'renderedPixelsMatchPreFixAcrossStylesDensitySizeAndMirroring'
                   and r['outcome'] == 'passed' for r in results)
    border_files = collect_images(root, 'edge-border', (
        'complete.txt', 'reference-stamp.png', 'current-stamp.png',
        'reference-dashed.png', 'current-dashed.png'), rendered, diagnostics)
    exif_rendered = any(r['class'] == 'info.plateaukao.einkbro.unit.ExifImagePipelineTest'
                        and r['method'] == 'jpegOrientationsPreserveBothProductionPipelines'
                        and r['outcome'] == 'passed' for r in results)
    exif_files = collect_images(root, 'exif', (
        'complete.txt', 'reference-background.png', 'current-background.png',
        'reference-web.png', 'current-web.png'), exif_rendered, diagnostics)
    return {'exif_evidence_base64': exif_files, 'border_evidence_base64': border_files, 'diagnostics': diagnostics, 'lint_reports': lint, 'test_counts': counts, 'new_tests': focused,
            'apk_files': [str(path.relative_to(root)) for path in sorted((root / 'app/build/outputs/apk').rglob('*.apk'))]}



def collect_images(root, group, names, test_passed, diagnostics):
    directory = root / 'build/reports/searchhh' / group
    if not test_passed or not (directory / 'complete.txt').is_file():
        return {}
    files = {}
    for name in names:
        path = directory / name
        size = path.stat().st_size if path.is_file() else None
        if size is None or not 0 < size <= 16384:
            label = 'border' if group == 'edge-border' else group
            diagnostics.append(f'Missing or oversized {label} evidence: {name}, bytes={size}')
            return {}
        files[name] = base64.b64encode(path.read_bytes()).decode('ascii')
    return files


def diagnostic_chunks(lines, root, limit=3000):
    """Keep warnings readable within GitHub's per-message/per-step limits."""
    chunks = []
    current = ''
    for line in dict.fromkeys(lines):
        line = line.replace('file://' + str(root) + '/', '').replace(str(root) + '/', '')
        if len(line) > limit:
            raise ValueError('Diagnostic exceeds annotation budget; full text remains in the report')
        if current and len(current) + len(line) + 1 > limit:
            chunks.append(current)
            current = ''
        current += ('\n' if current else '') + line
    if current:
        chunks.append(current)
    return chunks


def main():
    root = Path(__file__).resolve().parents[2]
    summary = summarize(root)
    output = root / 'build/reports/searchhh/ci-summary.json'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(summary, indent=2) + '\n')
    # GitHub caps annotations per step: put execution outcomes first, then a
    # bounded selection of actual compiler errors/warnings, then task failures.
    evidence = {'test_counts': summary['test_counts'], 'new_tests': [r['class'].rsplit('.', 1)[-1] + '.' + r['method'] + ':' + r['outcome'] for r in summary['new_tests']], 'apk_files': summary['apk_files']}
    print('::notice title=Executed test evidence::' + json.dumps(evidence))
    lint_preview = []
    for report in summary['lint_reports']:
        preview = {'report': report['report'], 'groups': report['groups'][:6],
                   'omitted_groups': max(0, len(report['groups']) - 6),
                   'draw_allocation_count': sum(g['count'] for g in report['groups'] if g['id'] == 'DrawAllocation'),
                   'recycle_count': sum(g['count'] for g in report['groups'] if g['id'] == 'Recycle'),
                   'exif_interface_count': sum(g['count'] for g in report['groups'] if g['id'] == 'ExifInterface')}
        if len(json.dumps(lint_preview + [preview])) > 5000:
            break
        lint_preview.append(preview)
    if lint_preview:
        message = json.dumps(lint_preview).replace('%', '%25').replace('\n', '%0A').replace('\r', '%0D')
        print('::notice title=Lint priorities (bounded; full groups in ci-summary.json)::' + message)
    # At most one five-file set in annotations. Both complete sets remain in artifacts/JSON.
    images = summary['exif_evidence_base64'] or summary['border_evidence_base64']
    file_key = 'exif_file' if summary['exif_evidence_base64'] else 'border_file'
    for name, encoded in images.items():
        print('::notice title=Image regression evidence::' + json.dumps({file_key: name, 'base64': encoded}))
    errors = [line for line in summary['diagnostics'] if line.startswith(('e:', 'Missing class ', 'ERROR: Missing class '))]
    warnings = [line for line in summary['diagnostics'] if line.startswith('w:') and '.gradle.kts:' not in line]
    tasks = [line for line in summary['diagnostics'] if not line.startswith(('e:', 'w:', 'Missing class ', 'ERROR: Missing class '))]
    chunks = diagnostic_chunks(errors + warnings + tasks, root)
    available = (8 if lint_preview else 9) - len(images)
    budget = available - 1 if len(chunks) > available else available
    for index, chunk in enumerate(chunks[:budget], 1):
        print(f'::notice title=Verification diagnostics {index}/{len(chunks)}::' + chunk.replace('%', '%25').replace('\n', '%0A').replace('\r', '%0D'))
    if len(chunks) > budget:
        print(f'::notice title=Diagnostic overflow::{len(chunks) - budget} further chunks are in ci-summary.json and the uploaded log; not all diagnostics fit annotations.')



if __name__ == '__main__':
    main()
