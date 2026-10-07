from datetime import datetime, timedelta, timezone
import tempfile
from pathlib import Path
import unittest
from tools.quality.agreement import ACK, TRAILER, agreement_errors
from tools.quality.ci_summary import diagnostic_chunks, summarize
from tools.quality.evidence import junit_results, validate_contract
from tools.quality.interactions import discover


class ContractTests(unittest.TestCase):
    def test_verification_does_not_assemble_a_release_before_quality_passes(self):
        root = Path(__file__).resolve().parents[3]
        checks = (root / '.github/workflows/android-verification.yml').read_text()
        self.assertIn(':app:minifyReleaseWithR8', checks)
        self.assertNotIn(':app:assembleRelease', checks)
        self.assertNotIn(':app:assembleDebug', checks)
        delivery = (root / '.github/workflows/buid-app-workflow.yaml').read_text()
        release = delivery.split('  release-build:', 1)[1].split('  release-smoke:', 1)[0]
        self.assertIn('needs: device-smoke', release)
        self.assertIn(':app:assembleRelease', release)

    def test_lint_group_counts_priorities_and_locations_are_preserved(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            reports = root / 'app/build/reports'
            reports.mkdir(parents=True)
            (reports / 'lint-results-release.xml').write_text(
                '<issues><issue id="Style" severity="Error" priority="2"/>'
                '<issue id="Security" severity="Error" priority="9">'
                f'<location file="{root}/app/src/Screen.kt" line="12"/></issue>'
                '<issue id="Style" severity="Error" priority="2"/></issues>')
            groups = summarize(root)['lint_reports'][0]['groups']
            self.assertEqual(['Security', 'Style'], [group['id'] for group in groups])
            self.assertEqual('app/src/Screen.kt', groups[0]['file'])
            self.assertEqual('12', groups[0]['line'])
            self.assertEqual(2, groups[1]['count'])

    def test_failed_rendering_assertion_is_preserved_in_diagnostics(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            reports = root / 'app/build/outputs/androidTest-results'
            reports.mkdir(parents=True)
            (reports / 'results.xml').write_text(
                '<testsuite><testcase classname="info.plateaukao.einkbro.view.EdgeBorderRenderingTest" '
                'name="renderedPixelsMatchPreFixAcrossStylesDensitySizeAndMirroring">'
                '<failure message="density mismatch"/></testcase></testsuite>')
            result = summarize(root)
            self.assertEqual('density mismatch', result['new_tests'][0]['detail'])
            self.assertTrue(any('density mismatch' in line for line in result['diagnostics']))
            self.assertEqual({}, result['border_evidence_base64'])

    def test_active_border_formatting_diagnostics_are_not_lost(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            line = '/repo/EdgeBorderRenderingTest.kt:10:1 Imports must be ordered'
            (root / 'quality-formatting.log').write_text(line)
            self.assertIn(line, summarize(root)['diagnostics'])

    def test_border_image_transport_requires_current_passing_render_test(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            evidence = root / 'build/reports/searchhh/edge-border'
            evidence.mkdir(parents=True)
            names = ('complete.txt', 'reference-stamp.png', 'current-stamp.png',
                     'reference-dashed.png', 'current-dashed.png')
            for name in names:
                (evidence / name).write_bytes(b'fixture')
            self.assertEqual({}, summarize(root)['border_evidence_base64'])
            reports = root / 'app/build/outputs/androidTest-results'
            reports.mkdir(parents=True)
            (reports / 'results.xml').write_text(
                '<testsuite><testcase classname="info.plateaukao.einkbro.view.EdgeBorderRenderingTest" '
                'name="renderedPixelsMatchPreFixAcrossStylesDensitySizeAndMirroring"/></testsuite>')
            self.assertEqual(set(names), set(summarize(root)['border_evidence_base64']))
            (evidence / 'current-stamp.png').unlink()
            result = summarize(root)
            self.assertEqual({}, result['border_evidence_base64'])
            self.assertTrue(any('Missing or oversized border evidence' in line for line in result['diagnostics']))

    def test_exif_transport_requires_its_passing_test_and_all_files(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            evidence = root / 'build/reports/searchhh/exif'
            evidence.mkdir(parents=True)
            names = ('complete.txt', 'reference-background.png', 'current-background.png',
                     'reference-web.png', 'current-web.png')
            for name in names:
                (evidence / name).write_bytes(b'fixture')
            self.assertEqual({}, summarize(root)['exif_evidence_base64'])
            reports = root / 'app/build/outputs/androidTest-results'
            reports.mkdir(parents=True)
            report = reports / 'results.xml'
            prefix = '<testsuite><testcase classname="info.plateaukao.einkbro.unit.ExifImagePipelineTest" '
            prefix += 'name="jpegOrientationsPreserveBothProductionPipelines"'
            report.write_text(prefix + '><failure message="orientation mismatch"/></testcase></testsuite>')
            self.assertEqual({}, summarize(root)['exif_evidence_base64'])
            report.write_text(prefix + '/></testsuite>')
            self.assertEqual(set(names), set(summarize(root)['exif_evidence_base64']))
            (evidence / 'current-web.png').unlink()
            self.assertEqual({}, summarize(root)['exif_evidence_base64'])

    def test_malformed_lint_report_is_not_reported_as_clean(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            reports = root / 'app/build/reports'
            reports.mkdir(parents=True)
            (reports / 'lint-results-release.xml').write_text('<broken')
            result = summarize(root)
            self.assertFalse(result['lint_reports'])
            self.assertTrue(any('Unparseable lint report' in line for line in result['diagnostics']))

    def test_r8_missing_classes_survive_compact_diagnostics(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            message = 'Missing class java.lang.management.RuntimeMXBean (referenced from: detector)'
            (root / 'quality-release.log').write_text(message + '\n> Task :app:minifyReleaseWithR8 FAILED\n')
            result = summarize(root)
            self.assertIn(message, result['diagnostics'])
            self.assertEqual(2, len(result['diagnostics']))

    def test_diagnostic_chunks_preserve_all_warnings_and_remove_runner_prefix(self):
        root = Path('/repo')
        lines = [f'w: file:///repo/Source.kt:{i}: deprecated API' for i in range(20)]
        chunks = diagnostic_chunks(lines + lines, root, limit=80)
        self.assertTrue(all(len(chunk) <= 80 for chunk in chunks))
        self.assertEqual([line.replace('file:///repo/', '') for line in lines], '\n'.join(chunks).splitlines())
        with self.assertRaises(ValueError):
            diagnostic_chunks(['x' * 81], root, limit=80)

    def test_diagnostics_preserve_compiler_errors_and_failed_test_outcomes(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'quality-build.log').write_text('e: Source.kt:12: unresolved symbol\n> Task :app:compileDebugKotlin FAILED\n')
            reports = root / 'app/build/test-results/testDebugUnitTest'
            reports.mkdir(parents=True)
            (reports / 'results.xml').write_text('<testsuite><testcase classname="SearchhhRouteTest" name="works"/><testcase classname="Other" name="broken"><failure/></testcase></testsuite>')
            result = summarize(root)
            self.assertEqual(2, len(result['diagnostics']))
            self.assertEqual(1, result['test_counts']['passed'])
            self.assertEqual(1, result['test_counts']['failure'])
            self.assertEqual('works', result['new_tests'][0]['method'])
            self.assertEqual([], result['apk_files'])

    def test_unchecked_or_missing_agreement_fails(self):
        self.assertTrue(agreement_errors('pull_request', {'pull_request': {'body': '- [ ] ' + ACK}}, [TRAILER]))
        self.assertTrue(agreement_errors('push', {}, ['Fix bug']))
        self.assertEqual([], agreement_errors('pull_request', {'pull_request': {'body': '- [x] ' + ACK}}, ['Fix bug\n\n' + TRAILER]))

    def test_prose_is_not_a_commit_trailer(self):
        self.assertTrue(agreement_errors('push', {}, [TRAILER + '\n\nActual final paragraph']))

    def test_generated_coauthor_footer_preserves_policy_acknowledgment(self):
        message = 'Fix bug\n\n' + TRAILER + '\n\nCo-authored-by: Agent <agent@example.test>\n'
        self.assertEqual([], agreement_errors('push', {}, [message]))

    def test_missing_contract_does_not_pass(self):
        self.assertTrue(validate_contract(Path('.'), 'UI-example', None, set()))

    def test_deleted_control_does_not_leave_a_discovered_candidate(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            file = root / 'app/src/main/Screen.kt'
            file.parent.mkdir(parents=True)
            file.write_text('Button(onClick = { model.start() })')
            self.assertEqual(2, len(discover(root)))
            file.write_text('// Button(onClick = { model.start() })')
            self.assertEqual([], discover(root))

    def test_action_plus_assertion_and_fresh_passing_test_required(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'Screen.kt').write_text('Button(onClick = { model.start() })')
            test = root / 'ScreenTest.kt'
            test.write_text('fun starts() { button.performClick(); assertEquals("running", model.state) }')
            contract = dict(source='Screen.kt', route='home', action='start', state='running',
                            test_file='ScreenTest.kt', test_class='ScreenTest', test_method='starts', tag='start')
            self.assertTrue(validate_contract(root, 'UI-example', contract, set()))
            self.assertEqual([], validate_contract(root, 'UI-example', contract, {('ScreenTest', 'starts')}))
            test.write_text('fun starts() { button.assertExists() }\nfun unrelated() { button.performClick(); assertEquals(1, state) }')
            self.assertTrue(validate_contract(root, 'UI-example', contract, {('ScreenTest', 'starts')}))

    def test_path_traversal_is_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            contract = dict(source='../outside', route='home', action='start', state='running',
                            test_file='Test.kt', test_class='Test', test_method='starts')
            self.assertTrue(validate_contract(Path(folder), 'UI-example', contract, set()))

    def test_stale_failed_and_skipped_junit_do_not_count(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'tests.xml'
            started = datetime.now(timezone.utc) - timedelta(minutes=1)
            current = datetime.now(timezone.utc).isoformat()
            path.write_text(f'<testsuite timestamp="{current}"><testcase classname="Test" name="works"/><testcase classname="Test" name="fails"><failure/></testcase><testcase classname="Test" name="skipped"><skipped/></testcase></testsuite>')
            self.assertEqual({('Test', 'works')}, junit_results([path], started))
            path.write_text('<testsuite timestamp="2000-01-01T00:00:00Z"><testcase classname="Test" name="works"/></testsuite>')
            self.assertEqual(set(), junit_results([path], started))


if __name__ == '__main__':
    unittest.main()
