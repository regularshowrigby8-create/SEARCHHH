#!/usr/bin/env python3
"""Validate SDK-decoded release metadata; SDK signature verification runs first in CI."""
import argparse
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ANDROID = '{http://schemas.android.com/apk/res/android}'


def release_errors(manifest, certificate, expected_fingerprint):
    root = ET.fromstring(manifest)
    errors = []
    if root.tag != 'manifest' or root.get('package') != 'app.searchhh.browser':
        errors.append('Incorrect application identity')
    app = root.find('application')
    if app is None:
        errors.append('Missing application manifest')
    else:
        for attribute in ('debuggable', 'testOnly'):
            if app.get(ANDROID + attribute, 'false') not in ('false', '0'):
                errors.append(f'Release must not enable {attribute}')
        for profile in app.findall('profileable'):
            if profile.get(ANDROID + 'shell', 'false') not in ('false', '0'):
                errors.append('Shell profiling must not be enabled')
    if root.findall('instrumentation'):
        errors.append('Instrumentation runner found in application APK')
    if root.get(ANDROID + 'versionName') != '0.4.1' or root.get(ANDROID + 'versionCode') != '5':
        errors.append('Unexpected release version')
    expected = expected_fingerprint.replace(':', '').strip().lower()
    actual = re.findall(r'^Signer #\d+ certificate SHA-256 digest: ([a-fA-F0-9]+)$', certificate, re.M)
    if not re.fullmatch(r'[a-f0-9]{64}', expected):
        errors.append('A pinned production certificate SHA-256 is required')
    if len(actual) != 1 or actual[0].lower() != expected:
        errors.append('Signing certificate differs from the pinned production identity')
    if re.search(r'CN\s*=\s*Android Debug\b', certificate, re.I):
        errors.append('Android debug signing certificate is forbidden')
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--manifest', type=Path, required=True)
    parser.add_argument('--certificate', type=Path, required=True)
    parser.add_argument('--fingerprint', required=True)
    args = parser.parse_args()
    errors = release_errors(args.manifest.read_text(), args.certificate.read_text(), args.fingerprint)
    for error in errors:
        print(error)
    if errors:
        raise SystemExit(1)
    print('Release identity, version, non-debug manifest and certificate pin verified')


if __name__ == '__main__':
    main()
