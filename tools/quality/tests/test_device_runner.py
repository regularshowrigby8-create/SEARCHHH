import os
from pathlib import Path
import subprocess
import tempfile
import unittest

RUNNER = Path(__file__).resolve().parents[1] / 'device_verification.sh'


class DeviceRunnerTests(unittest.TestCase):
    def run_fixture(self, gradle_exit, crash, border=False, copy_failure=False, exif=False):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            gradle = root / 'gradlew'
            gradle.write_text('#!/bin/bash\nprintf "%s\\n" "$@" > gradle-arguments.txt\nexit '
                              + str(gradle_exit) + '\n')
            gradle.chmod(0o755)
            adb = root / 'adb'
            adb.write_text('''#!/bin/bash
if [[ "$1 $2 $3 $4" == "shell run-as app.searchhh.browser test" ]]; then
    if [[ "$6" == files/exif-evidence/* ]]; then [[ "$MOCK_EXIF" == "1" ]]; exit $?; fi
    [[ "$MOCK_BORDER" == "1" ]]; exit $?
fi
if [[ "$1 $2" == "exec-out run-as" ]]; then
    if [[ "$MOCK_COPY_FAILURE" == "1" ]]; then exit 7; fi
    echo 'synthetic border evidence'; exit 0
fi
if [[ "$*" == "logcat -d -b crash" && "$MOCK_CRASH" == "1" ]]; then echo 'FATAL EXCEPTION'; fi
if [[ "$1" == "pull" ]]; then echo 'Less noise.' > "$3"; fi
exit 0
''')
            adb.chmod(0o755)
            result = subprocess.run(['bash', str(RUNNER)], cwd=root,
                                    env={**os.environ, 'PATH': str(root) + ':' + os.environ['PATH'],
                                         'MOCK_CRASH': '1' if crash else '0',
                                         'MOCK_BORDER': '1' if border else '0',
                                         'MOCK_EXIF': '1' if exif else '0',
                                         'MOCK_COPY_FAILURE': '1' if copy_failure else '0'}, capture_output=True)
            self.assertIn('-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true',
                          (root / 'gradle-arguments.txt').read_text().splitlines())
            if border and not copy_failure:
                for name in ('complete.txt', 'reference-stamp.png', 'current-stamp.png',
                             'reference-dashed.png', 'current-dashed.png'):
                    self.assertEqual('synthetic border evidence\n',
                                     (root / 'build/reports/searchhh/edge-border' / name).read_text())
            if exif and not copy_failure:
                for name in ('complete.txt', 'reference-background.png', 'current-background.png',
                             'reference-web.png', 'current-web.png'):
                    self.assertEqual('synthetic border evidence\n',
                                     (root / 'build/reports/searchhh/exif' / name).read_text())
            return result.returncode, (root / 'build/reports/searchhh/crash.log').read_text()

    def test_exif_capture_preserves_failed_master(self):
        status, _ = self.run_fixture(13, False, exif=True)
        self.assertEqual(13, status)

    def test_failed_exif_copy_cannot_pass(self):
        status, _ = self.run_fixture(0, False, exif=True, copy_failure=True)
        self.assertEqual(1, status)

    def test_border_evidence_collection_does_not_mask_failed_master(self):
        status, _ = self.run_fixture(13, False, border=True)
        self.assertEqual(13, status)

    def test_failed_border_evidence_copy_cannot_pass(self):
        status, _ = self.run_fixture(0, False, border=True, copy_failure=True)
        self.assertEqual(1, status)

    def test_failed_master_stays_failed_after_successful_diagnostic_capture(self):
        status, log = self.run_fixture(13, False)
        self.assertEqual(13, status)
        self.assertEqual('', log)

    def test_crash_fails_even_when_master_and_launch_succeed(self):
        status, log = self.run_fixture(0, True)
        self.assertEqual(1, status)
        self.assertIn('FATAL EXCEPTION', log)

    def test_success_requires_both_master_and_clean_crash_buffer(self):
        status, log = self.run_fixture(0, False)
        self.assertEqual(0, status)
        self.assertEqual('', log)


if __name__ == '__main__':
    unittest.main()
