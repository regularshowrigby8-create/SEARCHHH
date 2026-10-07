#!/usr/bin/env python3
"""Export raw diagnostic occurrences without pretending they are triaged tickets."""
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET

from tools.quality.report_transport import validate_payload, require

RUNNER_PREFIX = '/home/runner/work/SEARCHHH/SEARCHHH/'
ANSI = re.compile(r'\x1b\[[0-9;]*m')
FORMAT_LINE = re.compile(r'^(.+?):(\d+):(\d+): (.+) \(([^() ]+)\)$')
FORMAT_COUNT = re.compile(r'^\s+([^ ]+): (\d+)$')


def normalize_location(location):
    result = dict(location)
    if 'file' in result:
        result['file'] = result['file'].removeprefix(RUNNER_PREFIX)
    return result


def lint_records(content):
    root = ET.fromstring(content)
    require(root.tag == 'issues', 'Not a lint issue report')
    records = []
    for issue in root.findall('issue'):
        require(all(issue.get(key) for key in ('id', 'severity', 'message')), 'Incomplete lint issue')
        records.append({'rule': issue.get('id'), 'severity': issue.get('severity'),
                        'message': issue.get('message'),
                        'locations': [normalize_location(x.attrib) for x in issue.findall('location')],
                        'code': issue.get('errorLine1')})
    return records


def detekt_records(content):
    root = ET.fromstring(content)
    require(root.tag == 'checkstyle', 'Not a Detekt checkstyle report')
    records = []
    for file in root.findall('file'):
        require(file.get('name'), 'Missing Detekt file')
        for error in file.findall('error'):
            require(all(error.get(key) for key in ('source', 'severity', 'message')), 'Incomplete Detekt issue')
            records.append({'rule': error.get('source'), 'severity': error.get('severity'),
                            'message': error.get('message'), 'locations': [normalize_location({
                                'file': file.get('name'), 'line': error.get('line'),
                                'column': error.get('column')})]})
    return records


def format_records(content):
    records = []
    summaries = {}
    in_summary = False
    for line in ANSI.sub('', content).splitlines():
        if not line.strip():
            continue
        if line == 'Summary error count (descending) by rule:':
            require(not in_summary, 'Duplicate formatting summary')
            in_summary = True
            continue
        if not in_summary:
            match = FORMAT_LINE.fullmatch(line)
            require(match is not None, 'Unparsed formatting diagnostic: ' + line[:180])
            file, row, column, message, rule = match.groups()
            records.append({'rule': rule, 'severity': 'violation', 'message': message,
                            'locations': [normalize_location({'file': file, 'line': row, 'column': column})]})
        else:
            match = FORMAT_COUNT.fullmatch(line)
            require(match is not None, 'Unparsed formatting summary: ' + line[:180])
            rule, count = match.groups()
            require(rule not in summaries, 'Duplicate formatting summary rule')
            summaries[rule] = int(count)
    require(in_summary and records, 'Missing formatting findings/summary')
    require(dict(Counter(record['rule'] for record in records)) == summaries,
            'Formatting occurrence counts do not match original per-rule footer')
    return records


def export(bundles):
    require(set(bundles) == {'host', 'release', 'detekt', 'format', 'source'}, 'Missing report group')
    source = bundles['host']['source']
    output = {'schema': 1, 'source': source, 'scope': 'Raw recovered occurrences, not unique bugs or closed tickets',
              'semantic_ticket_assignment_complete': False, 'reports': [], 'compiler_logs': [],
              'interaction_review': {}, 'bundles': {}}
    for group, bundle in sorted(bundles.items()):
        validate_payload(bundle, source, group)
        output['bundles'][group] = {key: value for key, value in bundle.items() if key != 'files'}
        output['bundles'][group]['files'] = [{key: value for key, value in item.items() if key != 'content'}
                                           for item in bundle['files']]
        for file in bundle['files']:
            path = file['path']
            records = None
            tool = None
            if group in {'host', 'release'} and path.endswith('.xml'):
                records = lint_records(file['content'])
                tool = 'lint'
            elif group == 'detekt' and path.endswith('detekt.xml'):
                records = detekt_records(file['content'])
                tool = 'detekt'
                sarif = [json.loads(f['content']) for f in bundle['files'] if f['path'].endswith('.sarif')]
                require(len(sarif) == 1, 'Independent Detekt SARIF missing/ambiguous')
                require(sum(len(run['results']) for run in sarif[0]['runs']) == len(records),
                        'Detekt XML/SARIF result counts differ')
            elif group == 'format' and 'ktlint' in Path(path).parts and path.endswith('.txt'):
                records = format_records(file['content'])
                tool = 'ktlint'
            elif group == 'source' and Path(path).name == 'unfinished.json':
                unfinished = json.loads(file['content'])
                require(not unfinished['errors'], 'Original unfinished checker reports errors')
                records = [{'rule': f['rule'], 'severity': 'review-candidate',
                            'message': 'Scanner candidate; semantic audit required',
                            'fingerprint': f['fingerprint'],
                            'locations': [{'file': f['file'], 'line': f['line']}]} for f in unfinished['findings']]
                tool = 'unfinished'
            elif path.startswith('quality-') and path.endswith('.log'):
                lines = file['content'].splitlines()
                output['compiler_logs'].append({
                    'group': group, 'path': path, 'sha256': file['sha256'],
                    'scope': 'Compiler-prefix/task-failure extraction; no matching line is not a passing gate',
                    'compiler_messages': [line.replace(RUNNER_PREFIX, '') for line in lines
                                          if re.match(r'^(?:w:|e:|(?:ERROR: )?Missing class )', line)],
                    'failed_tasks': [line for line in lines if line.startswith('> Task ') and line.endswith(' FAILED')]})
            if records is not None:
                report_id = group + ':' + path
                for ordinal, record in enumerate(records, 1):
                    # This addresses an occurrence in an immutable raw report, not a stable root-cause ticket.
                    identity = f'{source}\n{report_id}\n{file["sha256"]}\n{ordinal}'
                    record['occurrence_id'] = hashlib.sha256(identity.encode()).hexdigest()[:20]
                output['reports'].append({'id': report_id, 'tool': tool, 'path': path,
                                          'sha256': file['sha256'], 'bytes': file['bytes'],
                                          'original_run': bundle['original_run'],
                                          'count': len(records), 'severity_counts': dict(Counter(r['severity'] for r in records)),
                                          'rule_counts': dict(sorted(Counter(r['rule'] for r in records).items())),
                                          'occurrences': records})
    source_files = {Path(f['path']).name: f for f in bundles['source']['files']}
    inventory = json.loads(source_files['interaction-inventory.json']['content'])
    contracts = json.loads(source_files['interaction-contracts.json']['content'])
    scenarios = json.loads(source_files['required-scenarios.json']['content'])
    output['interaction_review'] = {'candidates': inventory, 'declared_contracts': contracts,
                                    'required_scenarios': scenarios, 'execution_adjudicated': False}
    output['totals_by_tool_raw_nonadditive'] = dict(Counter({tool: sum(r['count'] for r in output['reports']
                                                                               if r['tool'] == tool)
                                                           for tool in ('lint', 'detekt', 'ktlint', 'unfinished')}))
    ids = [row['occurrence_id'] for r in output['reports'] for row in r['occurrences']]
    require(len(ids) == len(set(ids)), 'Occurrence identity collision')
    return output


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--bundles', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    result = export({group: json.loads((args.bundles / (group + '.json')).read_text())
                     for group in ('host', 'release', 'detekt', 'format', 'source')})
    args.output.parent.mkdir(parents=True, exist_ok=True)
    # Compact JSON avoids duplicating multi-megabyte report/log text in Git.
    args.output.write_text(json.dumps(result, ensure_ascii=False, separators=(',', ':')) + '\n')
    print(json.dumps(result['totals_by_tool_raw_nonadditive']))


if __name__ == '__main__':
    main()
