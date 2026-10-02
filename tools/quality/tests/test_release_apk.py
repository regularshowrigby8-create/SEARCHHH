import unittest
import xml.etree.ElementTree as ET
from tools.quality.release_apk import release_errors


class ReleaseApkTests(unittest.TestCase):
    fingerprint = 'ab' * 32
    certificate = 'Signer #1 certificate DN: CN=Searchhh\nSigner #1 certificate SHA-256 digest: ' + fingerprint
    manifest = '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="app.searchhh.browser" android:versionCode="5" android:versionName="0.4.1">
        <application android:debuggable="false" android:testOnly="false"/></manifest>'''

    def test_accepts_release_metadata_with_pinned_certificate(self):
        self.assertEqual([], release_errors(self.manifest, self.certificate, self.fingerprint))

    def test_rejects_debuggable_test_only_and_instrumentation(self):
        for attribute in ('debuggable', 'testOnly'):
            for value in ('true', '1', 'unrecognized'):
                xml = self.manifest.replace(f'{attribute}="false"', f'{attribute}="{value}"')
                self.assertTrue(release_errors(xml, self.certificate, self.fingerprint))
        xml = self.manifest.replace('</manifest>', '<instrumentation/></manifest>')
        self.assertTrue(release_errors(xml, self.certificate, self.fingerprint))

    def test_rejects_wrong_identity_version_or_absent_application(self):
        for before, after in (('app.searchhh.browser', 'app.searchhh.browser.test'),
                              ('versionCode="5"', 'versionCode="4"'),
                              ('versionName="0.4.1"', 'versionName="0.4.0"'),
                              ('<application', '<not-application')):
            self.assertTrue(release_errors(self.manifest.replace(before, after), self.certificate, self.fingerprint))
        with self.assertRaises(ET.ParseError):
            release_errors('<broken', self.certificate, self.fingerprint)

    def test_rejects_missing_wrong_multiple_or_debug_signers(self):
        for certificate in ('', self.certificate.replace(self.fingerprint, 'cd' * 32),
                            self.certificate + '\nSigner #2 certificate SHA-256 digest: ' + self.fingerprint,
                            self.certificate.replace('CN=Searchhh', 'CN=Android Debug')):
            self.assertTrue(release_errors(self.manifest, certificate, self.fingerprint))
        self.assertTrue(release_errors(self.manifest, self.certificate, ''))

    def test_rejects_shell_profiling(self):
        xml = self.manifest.replace('android:testOnly="false"/>',
                                    'android:testOnly="false"><profileable android:shell="true"/></application>')
        self.assertTrue(release_errors(xml, self.certificate, self.fingerprint))
