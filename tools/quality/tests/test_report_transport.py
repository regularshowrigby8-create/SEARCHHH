import copy
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from tools.quality import report_transport as transport

SOURCE = 'a' * 40


def payload():
    content = '<issues><issue id="NewApi" severity="Error" message="keep 100% &amp; newline"/></issues>\n'
    data = content.encode()
    return {'schema': 1, 'repository': transport.REPOSITORY, 'source': SOURCE, 'group': 'host',
            'original_run': 123, 'original_conclusion': 'failure', 'recovery_source': 'b' * 40,
            'artifact': {'id': 456, 'name': 'original', 'digest': 'sha256:abc', 'size_in_bytes': 300},
            'files': [{'path': 'app/build/reports/lint-results-debug.xml', 'content': content,
                       'bytes': len(data), 'sha256': transport.digest(data)}], 'excluded_files': []}


def annotations(pages):
    return [{'message': json.dumps(page)} for page in pages]


class ReportTransportTest(unittest.TestCase):
    def test_round_trip_reordered_pages_preserves_original_failure_and_content(self):
        with patch.object(transport, 'PAGE_CHARS', 200):
            original = payload()
            pages = transport.encode(original)
            self.assertGreater(len(pages), 1)
            actual = transport.decode(annotations(list(reversed(pages))), SOURCE, 'host')
            self.assertEqual(actual, original)
            self.assertEqual(actual['original_conclusion'], 'failure')

    def test_missing_duplicate_truncated_and_tampered_pages_are_rejected(self):
        with patch.object(transport, 'PAGE_CHARS', 200):
            pages = transport.encode(payload())
            variants = [pages[:-1], pages + [pages[0]], [pages[0]] * len(pages)]
            truncated = copy.deepcopy(pages)
            truncated[-1]['data'] = truncated[-1]['data'][:-4]
            variants.append(truncated)
            tampered = copy.deepcopy(pages)
            for item in tampered:
                item['sha256'] = '0' * 64
            variants.append(tampered)
            for variant in variants:
                with self.subTest(variant=variant[-1]['page']), self.assertRaises(ValueError):
                    transport.decode(annotations(variant), SOURCE, 'host')

    def test_mixed_source_group_schema_and_bundle_are_rejected(self):
        pages = transport.encode(payload())
        for field, value in [('source', 'c' * 40), ('group', 'detekt'), ('schema', 2)]:
            changed = copy.deepcopy(pages)
            changed[0][field] = value
            with self.subTest(field=field), self.assertRaises(ValueError):
                transport.decode(annotations(changed), SOURCE, 'host')
        with patch.object(transport, 'PAGE_CHARS', 200):
            changed = transport.encode(payload())
            changed[-1]['sha256'] = '0' * 64
            with self.assertRaisesRegex(ValueError, 'Mixed'):
                transport.decode(annotations(changed), SOURCE, 'host')

    def test_missing_required_report_or_invalid_per_file_digest_rejected(self):
        for change in ['missing', 'hash', 'length', 'duplicate']:
            item = payload()
            if change == 'missing':
                item['files'] = []
            elif change == 'hash':
                item['files'][0]['sha256'] = '0' * 64
            elif change == 'length':
                item['files'][0]['bytes'] += 1
            else:
                item['files'].append(item['files'][0])
            with self.subTest(change=change), self.assertRaises(ValueError):
                transport.encode(item)

    def test_unsafe_paths_and_unexpected_file_types_rejected(self):
        for name in ['../lint-results-debug.xml', '/tmp/unsafe', 'a/../b', 'a//b',
                     'a\\b', 'C:/b', 'app/build/outputs/apk/release.apk']:
            item = payload()
            item['files'][0]['path'] = name
            with self.subTest(name=name), self.assertRaises(ValueError):
                transport.encode(item)

    def test_caps_fail_instead_of_publishing_partial_payload(self):
        with patch.object(transport, 'PAGE_CHARS', 10), patch.object(transport, 'MAX_PAGES', 1):
            with self.assertRaisesRegex(ValueError, 'cap is'):
                transport.encode(payload())
        with patch.object(transport, 'MAX_RAW_BYTES', 10):
            with self.assertRaisesRegex(ValueError, 'raw size'):
                transport.encode(payload())
        pages = transport.encode(payload())
        pages[0]['raw_bytes'] = transport.MAX_RAW_BYTES + 1
        with self.assertRaisesRegex(ValueError, 'raw size'):
            transport.decode(annotations(pages), SOURCE, 'host')

    def test_decompression_cannot_exceed_declared_cap(self):
        pages = transport.encode(payload())
        for page in pages:
            page['raw_bytes'] = 300
        with patch.object(transport, 'MAX_RAW_BYTES', 300):
            with self.assertRaisesRegex(ValueError, 'checksum'):
                transport.decode(annotations(pages), SOURCE, 'host')

    def test_empty_or_truncated_json_annotations_cannot_pass(self):
        for value in [[], [{'message': '{"kind":"searchhh-report-page"'}]]:
            with self.assertRaisesRegex(ValueError, 'No report pages'):
                transport.decode(value, SOURCE, 'host')

    def test_artifact_selection_checks_provenance_and_keeps_exclusion_manifest(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report = root / 'app/build/reports/lint-results-debug.xml'
            report.parent.mkdir(parents=True)
            report.write_text('<issues/>')
            (root / 'test-only.png').write_bytes(b'image')
            run = {'id': 123, 'head_sha': SOURCE, 'status': 'completed', 'conclusion': 'failure',
                   'head_repository': {'full_name': transport.REPOSITORY}}
            artifact = {'id': 456, 'name': 'original', 'digest': 'sha256:abc', 'size_in_bytes': 300,
                        'expired': False, 'workflow_run': {'head_sha': SOURCE, 'id': 123}}
            catalog = {'total_count': 1, 'artifacts': [artifact]}
            result = transport.make_payload(root, run, catalog, 'original', SOURCE, 'host', 'b' * 40)
            self.assertEqual(len(result['files']), 1)
            self.assertEqual(result['excluded_files'][0]['path'], 'test-only.png')
            artifact['workflow_run']['head_sha'] = 'c' * 40
            with self.assertRaisesRegex(ValueError, 'source/run'):
                transport.make_payload(root, run, catalog, 'original', SOURCE, 'host', 'b' * 40)
            artifact['workflow_run']['head_sha'] = SOURCE
            catalog['total_count'] = 2
            with self.assertRaisesRegex(ValueError, 'listing incomplete'):
                transport.make_payload(root, run, catalog, 'original', SOURCE, 'host', 'b' * 40)

    def test_flat_source_artifact_and_complete_snapshots_required(self):
        item = payload()
        item['group'] = 'source'
        item['files'] = []
        for name in ['unfinished.json', 'snapshots/interaction-inventory.json',
                     'snapshots/interaction-contracts.json', 'snapshots/required-scenarios.json']:
            item['files'].append({'path': name, 'content': '{}', 'bytes': 2,
                                  'sha256': transport.digest(b'{}')})
        self.assertEqual(transport.decode(annotations(transport.encode(item)), SOURCE, 'source'), item)
        item['files'].pop()
        with self.assertRaisesRegex(ValueError, 'snapshot missing'):
            transport.encode(item)

    def test_workflow_is_read_only_pinned_and_does_not_change_existing_gates(self):
        text = Path('.github/workflows/quality-report-recovery.yml').read_text()
        self.assertIn('contents: read', text)
        self.assertIn('actions: read', text)
        self.assertIn('6abf29b57982b93fe929674c70230bb17f6940cf', text)
        self.assertIn("original_run: '36628444279'", text)
        self.assertIn("original_run: '36628443911'", text)
        self.assertNotIn('continue-on-error', text)
        self.assertNotIn('assemble', text)
        self.assertNotIn('secrets.', text)

    def test_large_realistic_bundle_survives_4096_character_messages_and_step_budgets(self):
        import base64
        import random
        item = payload()
        content = '<issues>' + base64.b64encode(random.Random(0).randbytes(160000)).decode() + '</issues>'
        item['files'][0].update(content=content, bytes=len(content), sha256=transport.digest(content.encode()))
        pages = transport.encode(item)
        self.assertGreater(len(pages), 50)
        emitted = []
        for batch in range(transport.MAX_PAGES // transport.PAGES_PER_STEP):
            selected = transport.batch_pages(pages, batch)
            self.assertLessEqual(len(selected), 8)
            for page in selected:
                self.assertLessEqual(len(json.dumps(page)), 4096)
                emitted.append({'message': json.dumps(page)[:4096]})
        self.assertEqual(transport.decode(emitted, SOURCE, 'host'), item)
        with self.assertRaises(ValueError):
            transport.batch_pages(pages, -1)

    def test_plain_ktlint_reports_are_preserved_not_replaced_by_logs(self):
        item = payload()
        item['group'] = 'format'
        item['files'][0]['path'] = 'app/build/reports/ktlint/ktlintMainSourceSetCheck.txt'
        self.assertEqual(transport.decode(annotations(transport.encode(item)), SOURCE, 'format'), item)
        item['files'][0]['path'] = 'quality-formatting.log'
        with self.assertRaisesRegex(ValueError, 'No ktlint report'):
            transport.encode(item)

    def test_shards_stay_below_observed_fifty_annotation_job_cap(self):
        self.assertEqual(transport.PAGES_PER_JOB, 40)
        self.assertEqual(transport.MAX_PAGES, transport.MAX_SHARDS * transport.PAGES_PER_JOB)
        self.assertLess(transport.PAGES_PER_JOB + 2, 50)
        text = Path('.github/workflows/quality-report-recovery.yml').read_text()
        self.assertIn('shard: 1', text)
        self.assertEqual(text.count('--batch \"$((RECOVERY_SHARD * 5 + '), 5)
        self.assertIn('shard: 4', text)

    def test_actual_console_only_formatting_requires_diagnostics_failure_and_disclosure(self):
        item = payload()
        item['group'] = 'format'
        item['format_report_mode'] = 'console-only-no-machine-report'
        content = '> Task :app:ktlintMainSourceSetCheck FAILED\n/app/Example.kt:12:4 Missing newline\n'
        item['files'][0].update(path='quality-formatting.log', content=content,
                                bytes=len(content), sha256=transport.digest(content.encode()))
        self.assertEqual(transport.decode(annotations(transport.encode(item)), SOURCE, 'format'), item)
        item.pop('format_report_mode')
        with self.assertRaisesRegex(ValueError, 'limitation must be explicit'):
            transport.encode(item)
        self.assertFalse(transport.formatting_console('BUILD FAILED'))
        self.assertFalse(transport.formatting_console('Example.kt:12:4 Missing newline'))
