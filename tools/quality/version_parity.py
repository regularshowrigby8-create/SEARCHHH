#!/usr/bin/env python3
"""Fail when the Gradle release version and the release-APK pins disagree.

The same two numbers are written down twice in this repository:

* ``app/build.gradle.kts`` -> ``versionCode`` / ``versionName`` in the ``android`` block;
* ``tools/quality/release_apk.py`` -> the literals it compares the decoded manifest against.

Nothing fails when they drift: ``release_apk.py`` would then reject every *correct* release APK
(it checks the artifact against its own stale copy of the truth), while the build happily produces
an APK whose version nobody gate-checked. This tool closes that gap in the cheap direction -- it
reads both files and refuses when they do not say the same thing.

Unreadable input is a FAILURE, never a pass. If either pattern stops matching (for example because
``release_apk.py`` is refactored away from the ``root.get(ANDROID + 'versionName') != '...'`` idiom)
this exits non-zero with a message naming what it could not find, so the gate cannot rot into a
vacuous green. Refactoring the pins into a shared constant that both files import would be the
better long-term shape, but it means editing the release validator, so it is left to the owner.
"""
from __future__ import annotations

import argparse
import re
from pathlib import Path

GRADLE_PATH = Path('app/build.gradle.kts')
PIN_PATH = Path('tools/quality/release_apk.py')

# The `versionNameSuffix`/`applicationIdSuffix` lines must not be mistaken for the real key, hence
# the `=` immediately after the name and the end-of-line anchor.
GRADLE_CODE = re.compile(r'^[ \t]*versionCode[ \t]*=[ \t]*([0-9]+)[ \t]*$', re.M)
GRADLE_NAME = re.compile(r'^[ \t]*versionName[ \t]*=[ \t]"([^"]*)"[ \t]*$', re.M)

# release_apk.py compares the decoded manifest against inline literals.
PIN_CODE = re.compile(r"ANDROID[ \t]*\+[ \t]*'versionCode'\)[ \t]*!=?[ \t]*'([^']*)'")
PIN_NAME = re.compile(r"ANDROID[ \t]*\+[ \t]*'versionName'\)[ \t]*!=?[ \t]*'([^']*)'")


def _one(text: str, pattern: re.Pattern, side: str, errors: list) -> str | None:
    found = pattern.findall(text)
    if not found:
        errors.append(f'{side}: no value found (pattern {pattern.pattern!r} did not match); '
                      'this check is vacuous and must be updated with the file it reads')
        return None
    distinct = sorted(set(found))
    if len(distinct) > 1:
        errors.append(f'{side}: ambiguous, {len(distinct)} different values {distinct}; the checker '
                      'refuses to guess which one the release is built from')
        return None
    return distinct[0]


def parity_errors(gradle_text: str, pin_text: str) -> list:
    """Return a list of human-readable disagreements; empty means the two sources agree."""
    errors: list = []
    gradle_code = _one(gradle_text, GRADLE_CODE, str(GRADLE_PATH) + ' versionCode', errors)
    gradle_name = _one(gradle_text, GRADLE_NAME, str(GRADLE_PATH) + ' versionName', errors)
    pin_code = _one(pin_text, PIN_CODE, str(PIN_PATH) + ' versionCode pin', errors)
    pin_name = _one(pin_text, PIN_NAME, str(PIN_PATH) + ' versionName pin', errors)
    if errors:
        return errors
    if gradle_code != pin_code:
        errors.append(f'versionCode disagrees: {GRADLE_PATH} builds {gradle_code!r} but '
                      f'{PIN_PATH} accepts only {pin_code!r}')
    if gradle_name != pin_name:
        errors.append(f'versionName disagrees: {GRADLE_PATH} builds {gradle_name!r} but '
                      f'{PIN_PATH} accepts only {pin_name!r}')
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument('--root', type=Path, default=Path('.'), help='repository root to read')
    args = parser.parse_args()
    gradle_file, pin_file = args.root / GRADLE_PATH, args.root / PIN_PATH
    missing = [str(p) for p in (gradle_file, pin_file) if not p.is_file()]
    if missing:
        for path in missing:
            print(f'version parity check cannot run: {path} not found')
        return 1
    gradle_text, pin_text = gradle_file.read_text(), pin_file.read_text()
    errors = parity_errors(gradle_text, pin_text)
    for error in errors:
        print(f'version parity: {error}')
    if errors:
        print('Bump both files together, or update the pin pattern in tools/quality/version_parity.py '
              'if release_apk.py stopped expressing the pins inline.')
        return 1
    print('release version pins agree: '
          f"versionCode={GRADLE_CODE.search(gradle_text).group(1)} "
          f"versionName={GRADLE_NAME.search(gradle_text).group(1)}")
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
