"""Tests for tools/quality/version_parity.py.

The suite is deliberately asymmetric: the *positive* case (the real repository agrees with itself)
is the regression guard, and the *negative* cases prove the checker is not vacuous — that it fails
when a value is missing, when a value is renamed, and when only the decoy `versionNameSuffix` exists.
"""
import contextlib
import io
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from tools.quality.version_parity import GRADLE_PATH, PIN_PATH, main, parity_errors

REPO = Path(__file__).resolve().parents[3]

GRADLE_OK = '''android {
    defaultConfig {
        versionCode = 5
        versionName = "0.4.1"
    }
    buildTypes {
        create("release") {
            versionNameSuffix = "-a"
        }
    }
}
'''
PIN_OK = """    if root.get(ANDROID + 'versionName') != '0.4.1' or root.get(ANDROID + 'versionCode') != '5':
        errors.append('Unexpected release version')
"""


def tree(root: Path, gradle: str, pin: str):
    (root / GRADLE_PATH).parent.mkdir(parents=True, exist_ok=True)
    (root / PIN_PATH).parent.mkdir(parents=True, exist_ok=True)
    (root / GRADLE_PATH).write_text(gradle)
    (root / PIN_PATH).write_text(pin)


class VersionParityTests(unittest.TestCase):
    def test_matching_sources_produce_no_errors(self):
        self.assertEqual([], parity_errors(GRADLE_OK, PIN_OK))

    def test_version_name_mismatch_is_reported_with_both_sides(self):
        errors = parity_errors(GRADLE_OK, PIN_OK.replace("'0.4.1'", "'0.4.2'"))
        self.assertEqual(1, len(errors))
        self.assertIn('versionName disagrees', errors[0])
        self.assertIn("'0.4.1'", errors[0])   # what the build produces
        self.assertIn("'0.4.2'", errors[0])   # what the release check accepts

    def test_version_code_mismatch_is_reported(self):
        errors = parity_errors(GRADLE_OK.replace('versionCode = 5', 'versionCode = 6'), PIN_OK)
        self.assertEqual(['versionCode disagrees: app/build.gradle.kts builds \'6\' but '
                          'tools/quality/release_apk.py accepts only \'5\''], errors)

    def test_both_sides_drifting_produces_two_findings(self):
        pin = PIN_OK.replace("'0.4.1'", "'0.9.9'").replace("!= '5'", "!= '77'")
        self.assertEqual(2, len(parity_errors(GRADLE_OK, pin)))

    # --- negative tests: the checker must never pass because it found nothing --------------------

    def test_rewritten_pin_idiom_fails_instead_of_passing(self):
        """If release_apk.py stops carrying inline literals the check must not go vacuously green."""
        errors = parity_errors(GRADLE_OK, "EXPECTED = '0.4.1'\nif root.get('x') != EXPECTED:\n")
        self.assertTrue(errors, 'a pin file with no readable pin must fail, not pass')
        self.assertTrue(all('vacuous' in e for e in errors), errors)

    def test_gradle_without_version_name_fails_even_though_suffix_exists(self):
        gradle = 'android {\n    defaultConfig {\n        versionCode = 5\n    }\n    ' \
                 'buildTypes { create("release") { versionNameSuffix = "-a" } }\n}\n'
        errors = parity_errors(gradle, PIN_OK)
        self.assertEqual(1, len(errors))
        self.assertIn('versionName', errors[0])
        self.assertIn('no value found', errors[0])

    def test_two_different_gradle_versions_are_ambiguous_not_guessed(self):
        gradle = GRADLE_OK.replace('versionCode = 5', 'versionCode = 5\n        versionCode = 9')
        errors = parity_errors(gradle, PIN_OK)
        self.assertEqual(1, len(errors))
        self.assertIn('ambiguous', errors[0])
        self.assertIn("['5', '9']", errors[0])

    # --- end to end ------------------------------------------------------------------------

    def test_cli_exits_zero_on_agreement_and_one_on_disagreement(self):
        for gradle, expected in ((GRADLE_OK, 0), (GRADLE_OK.replace('"0.4.1"', '"0.5.0"'), 1)):
            with self.subTest(expected=expected), tempfile.TemporaryDirectory() as tmp:
                root = Path(tmp)
                tree(root, gradle, PIN_OK)
                argv = ['version_parity.py', '--root', str(root)]
                with mock.patch.object(sys, 'argv', argv), contextlib.redirect_stdout(io.StringIO()) as out:
                    self.assertEqual(expected, main())
                printed = out.getvalue()
                if expected == 0:
                    self.assertIn('release version pins agree: versionCode=5 versionName=0.4.1', printed)
                    self.assertNotIn('version parity:', printed)
                else:
                    self.assertIn('version parity: versionName disagrees', printed)
                    self.assertIn('Bump both files together', printed)

    def test_cli_fails_when_a_file_is_absent(self):
        with tempfile.TemporaryDirectory() as tmp, mock.patch.object(
                sys, 'argv', ['version_parity.py', '--root', tmp]), \
                contextlib.redirect_stdout(io.StringIO()) as out:
            self.assertEqual(1, main())
        self.assertIn('not found', out.getvalue())

    def test_the_real_repository_files_agree(self):
        """Guard: today's tree must pass, and this test breaks the moment one side is bumped."""
        gradle = (REPO / GRADLE_PATH).read_text()
        pin = (REPO / PIN_PATH).read_text()
        self.assertEqual([], parity_errors(gradle, pin))
        self.assertIn('versionCode = 5', gradle)      # pinned on purpose: if the release version is
        self.assertIn('"0.4.1"', gradle)              # bumped deliberately, update this test too

    def test_patterns_are_applied_to_the_real_pin_line(self):
        """The real file is one long `if` line; prove the regexes read it without being loosened."""
        from tools.quality.version_parity import PIN_CODE, PIN_NAME
        line = (REPO / PIN_PATH).read_text()
        self.assertEqual('5', PIN_CODE.search(line).group(1))
        self.assertEqual('0.4.1', PIN_NAME.search(line).group(1))


if __name__ == '__main__':
    unittest.main()
