#!/usr/bin/env python3
"""Recover static CI reports through bounded, integrity-checked annotations."""
import argparse
import base64
import gzip
import hashlib
import io
import json
from pathlib import Path, PurePosixPath
import re
import sys

SCHEMA = 1
KIND = 'searchhh-report-page'
PAGE_CHARS = 3000
PAGES_PER_STEP = 8
PAGES_PER_JOB = 40
MAX_SHARDS = 5
MAX_PAGES = PAGES_PER_JOB * MAX_SHARDS
MAX_RAW_BYTES = 32 * 1024 * 1024
REPOSITORY = 'regularshowrigby8-create/SEARCHHH'
GROUPS = {'host', 'release', 'detekt', 'format', 'source'}


def digest(data):
    return hashlib.sha256(data).hexdigest()


def require(condition, message):
    if not condition:
        raise ValueError(message)


def safe_path(value):
    require(isinstance(value, str) and value != '', 'Missing report path')
    path = PurePosixPath(value)
    require(not path.is_absolute() and '\\' not in value and ':' not in value,
            'Unsafe report path')
    require(all(part not in ('', '.', '..') for part in value.split('/')),
            'Noncanonical report path')
    return value


def selected(path):
    name = path.name
    parts = path.parts
    if name.startswith('quality-') and name.endswith('.log'):
        return True
    if 'snapshots' in parts:
        return name in {'interaction-inventory.json', 'interaction-contracts.json',
                        'required-scenarios.json', 'libs.versions.toml', 'gradle-wrapper.properties'}
    if name == 'unfinished.json':
        return True
    if 'reports' not in parts:
        return False
    return (name.startswith('lint-results-') and path.suffix == '.xml'
            or 'detekt' in parts and path.suffix in {'.xml', '.sarif'}
            or 'ktlint' in parts and path.suffix in {'.xml', '.txt'}
            or name == 'unfinished.json')


def formatting_console(content):
    diagnostic = re.search(r'^.*\.kts?:[0-9]+:[0-9]+ .+', content, re.MULTILINE)
    task_failure = re.search(r'^> Task .*ktlint.* FAILED$', content, re.MULTILINE)
    return diagnostic is not None and task_failure is not None


def validate_payload(payload, source, group):
    require(payload.get('schema') == SCHEMA, 'Unknown payload schema')
    require(payload.get('repository') == REPOSITORY, 'Repository mismatch')
    require(payload.get('source') == source and payload.get('group') == group,
            'Payload provenance mismatch')
    require(re.fullmatch(r'[0-9a-f]{40}', source) is not None, 'Invalid source SHA')
    require(group in GROUPS, 'Unknown report group')
    require(payload.get('files'), 'Empty report bundle')
    names = set()
    for entry in payload['files']:
        name = safe_path(entry['path'])
        require(name not in names, 'Duplicate report path')
        require(selected(PurePosixPath(name)), 'Unexpected report type')
        names.add(name)
        data = entry['content'].encode('utf-8')
        require(len(data) == entry['bytes'] and digest(data) == entry['sha256'],
                'Report integrity mismatch')
    if group in {'host', 'release'}:
        variant = 'debug' if group == 'host' else 'release'
        require(any(name.endswith(f'lint-results-{variant}.xml') for name in names),
                'Required lint report missing')
    elif group == 'detekt':
        require(any('detekt' in PurePosixPath(name).parts and name.endswith('.xml')
                    for name in names), 'Required Detekt XML missing')
    elif group == 'format':
        machine_report = any('ktlint' in PurePosixPath(name).parts
                             and name.endswith(('.xml', '.txt')) for name in names)
        if not machine_report:
            logs = [entry['content'] for entry in payload['files']
                    if entry['path'] == 'quality-formatting.log']
            require(len(logs) == 1 and formatting_console(logs[0]),
                    'No ktlint report or complete diagnostic-bearing formatting log')
            require(payload.get('format_report_mode') == 'console-only-no-machine-report',
                    'Console-only formatting limitation must be explicit')
    else:
        require(any(PurePosixPath(name).name == 'unfinished.json' for name in names),
                'Required unfinished report missing')
        for name in ('interaction-inventory.json', 'interaction-contracts.json', 'required-scenarios.json'):
            require('snapshots/' + name in names, 'Required source snapshot missing')


def make_payload(root, run, artifacts, artifact_name, source, group, recovery_source):
    require(run['head_sha'] == source and run['status'] == 'completed',
            'Original run source/status mismatch')
    require(run['head_repository']['full_name'] == REPOSITORY, 'Original repository mismatch')
    require(artifacts['total_count'] == len(artifacts['artifacts']), 'Artifact listing incomplete')
    matches = [item for item in artifacts['artifacts'] if item['name'] == artifact_name]
    require(len(matches) == 1 and not matches[0]['expired'], 'Artifact absent, ambiguous or expired')
    artifact = matches[0]
    require(artifact['workflow_run']['head_sha'] == source
            and artifact['workflow_run']['id'] == run['id'], 'Artifact source/run mismatch')
    files = []
    excluded = []
    for path in sorted(root.rglob('*')):
        require(not path.is_symlink(), 'Symlink in report bundle')
        if not path.is_file():
            continue
        relative = path.relative_to(root).as_posix()
        data = path.read_bytes()
        entry = {'path': relative, 'bytes': len(data), 'sha256': digest(data)}
        if selected(PurePosixPath(relative)):
            entry['content'] = data.decode('utf-8')
            files.append(entry)
        else:
            excluded.append(entry)
    payload = {'schema': SCHEMA, 'repository': REPOSITORY, 'source': source, 'group': group,
               'original_run': run['id'], 'original_conclusion': run['conclusion'],
               'recovery_source': recovery_source,
               'artifact': {key: artifact[key] for key in ('id', 'name', 'digest', 'size_in_bytes')},
               'files': files, 'excluded_files': excluded}
    if group == 'format' and not any('ktlint' in PurePosixPath(entry['path']).parts
                                       for entry in files):
        payload['format_report_mode'] = 'console-only-no-machine-report'
    try:
        validate_payload(payload, source, group)
    except ValueError as error:
        paths = [entry['path'] for entry in files + excluded]
        raise ValueError(str(error) + '; available files: ' + ', '.join(paths)[:2800]) from error
    return payload


def encode(payload):
    validate_payload(payload, payload['source'], payload['group'])
    raw = json.dumps(payload, ensure_ascii=True, sort_keys=True, separators=(',', ':')).encode()
    require(len(raw) <= MAX_RAW_BYTES, 'Report bundle exceeds raw size cap')
    encoded = base64.b64encode(gzip.compress(raw, mtime=0)).decode('ascii')
    count = (len(encoded) + PAGE_CHARS - 1) // PAGE_CHARS
    require(0 < count <= MAX_PAGES,
            f'Report bundle needs {count} pages ({len(encoded)} encoded bytes); cap is {MAX_PAGES}')
    header = {'kind': KIND, 'schema': SCHEMA, 'source': payload['source'], 'group': payload['group'],
              'sha256': digest(raw), 'raw_bytes': len(raw), 'encoded_bytes': len(encoded), 'pages': count}
    return [dict(header, page=index + 1, data=encoded[index * PAGE_CHARS:(index + 1) * PAGE_CHARS])
            for index in range(count)]


def decode(annotations, source, group):
    pages = []
    for annotation in annotations:
        message = annotation.get('message', '')
        if not message.startswith('{'):
            continue
        try:
            item = json.loads(message)
        except json.JSONDecodeError:
            continue  # Missing/truncated pages still fail the count and digest checks below.
        if isinstance(item, dict) and item.get('kind') == KIND:
            pages.append(item)
    require(pages, 'No report pages found')
    first = pages[0]
    require(first['schema'] == SCHEMA and first['source'] == source and first['group'] == group,
            'Page provenance mismatch')
    count = first['pages']
    require(type(count) is int and 0 < count <= MAX_PAGES, 'Invalid page count')
    require(len(pages) == count, 'Missing or duplicate report pages')
    require(type(first['raw_bytes']) is int and 0 < first['raw_bytes'] <= MAX_RAW_BYTES,
            'Invalid raw size')
    require(type(first['encoded_bytes']) is int
            and 0 < first['encoded_bytes'] <= PAGE_CHARS * MAX_PAGES, 'Invalid encoded size')
    header = {key: value for key, value in first.items() if key not in {'page', 'data'}}
    for item in pages:
        require({key: value for key, value in item.items() if key not in {'page', 'data'}} == header,
                'Mixed report bundles')
        require(type(item['page']) is int and isinstance(item['data'], str)
                and 0 < len(item['data']) <= PAGE_CHARS, 'Invalid page data')
    require(sorted(item['page'] for item in pages) == list(range(1, count + 1)),
            'Duplicate or invalid page index')
    encoded = ''.join(item['data'] for item in sorted(pages, key=lambda item: item['page']))
    require(len(encoded) == first['encoded_bytes'], 'Truncated encoded payload')
    compressed = base64.b64decode(encoded, validate=True)
    with gzip.GzipFile(fileobj=io.BytesIO(compressed)) as stream:
        raw = stream.read(MAX_RAW_BYTES + 1)
    require(len(raw) == first['raw_bytes'] and digest(raw) == first['sha256'],
            'Payload size or checksum mismatch')
    payload = json.loads(raw)
    validate_payload(payload, source, group)
    return payload


def batch_pages(pages, batch):
    require(type(batch) is int and 0 <= batch < MAX_PAGES // PAGES_PER_STEP,
            'Invalid annotation batch')
    start = batch * PAGES_PER_STEP
    selected_pages = pages[start:start + PAGES_PER_STEP]
    require(all(len(json.dumps(page)) <= 4096 for page in selected_pages),
            'Annotation exceeds observed 4096-character transport limit')
    return selected_pages


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='command', required=True)
    pack = sub.add_parser('pack')
    pack.add_argument('--root', type=Path, required=True)
    pack.add_argument('--run', type=Path, required=True)
    pack.add_argument('--artifacts', type=Path, required=True)
    pack.add_argument('--artifact-name', required=True)
    pack.add_argument('--recovery-source', required=True)
    pack.add_argument('--pages-output', type=Path, required=True)
    pack.add_argument('--shards', type=int, choices=range(1, MAX_SHARDS + 1), required=True)
    emit = sub.add_parser('emit')
    emit.add_argument('--pages-input', type=Path, required=True)
    emit.add_argument('--batch', type=int, required=True)
    unpack = sub.add_parser('decode')
    unpack.add_argument('--annotations', type=Path, required=True)
    unpack.add_argument('--output', type=Path, required=True)
    for command in (pack, unpack, emit):
        command.add_argument('--source', required=True)
        command.add_argument('--group', choices=sorted(GROUPS), required=True)
    args = parser.parse_args()
    if args.command == 'pack':
        payload = make_payload(args.root, json.loads(args.run.read_text()),
                               json.loads(args.artifacts.read_text()), args.artifact_name,
                               args.source, args.group, args.recovery_source)
        # Construct all pages before publishing any; overflow is never silent truncation.
        pages = encode(payload)
        require(len(pages) <= args.shards * PAGES_PER_JOB, 'Insufficient recovery job shards')
        args.pages_output.write_text(json.dumps(pages))
        print(f'Prepared {len(pages)} integrity-checked pages; emit in separate bounded steps.')
    elif args.command == 'emit':
        pages = json.loads(args.pages_input.read_text())
        decode([{'message': json.dumps(page)} for page in pages], args.source, args.group)
        for page in batch_pages(pages, args.batch):
            print('::notice title=Complete static report page::' + json.dumps(page))
    else:
        annotations = json.loads(args.annotations.read_text())
        if annotations and isinstance(annotations[0], list):
            annotations = [entry for batch in annotations for entry in batch]
        payload = decode(annotations, args.source, args.group)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(payload, indent=2) + '\n')
        print(f"Recovered {len(payload['files'])} complete files from {payload['source']}")


if __name__ == '__main__':
    try:
        main()
    except ValueError as error:
        message = str(error).replace('%', '%25').replace('\n', '%0A').replace('\r', '%0D')
        print('::error title=Report recovery validation failed::' + message)
        sys.exit(1)
