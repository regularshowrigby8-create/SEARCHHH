#!/usr/bin/env python3
"""Verify a recorded policy acknowledgment, not private belief or legal consent."""
import json
import os
from pathlib import Path
import re
import subprocess
import sys

ACK = 'I have read and agree to follow Searchhh Quality Policy v1.'
TRAILER = 'Searchhh-Quality-Policy: accepted-v1'


def agreement_errors(event_name, event, messages):
    errors = []
    if event_name == 'pull_request':
        body = event['pull_request'].get('body') or ''
        if not re.search(r'^\s*- \[[xX]\] ' + re.escape(ACK) + r'\s*$', body, re.M):
            errors.append('PR author must check the policy acknowledgment in the PR template.')
    for index, message in enumerate(messages, 1):
        # Arena/GitHub may insert a blank line before a co-author footer.
        # Accept a final footer block separated by blank lines, never prose mentions.
        footer = []
        for line in reversed(message.rstrip().splitlines()):
            if not line.strip():
                continue
            if not re.fullmatch(r'[A-Za-z][A-Za-z0-9-]*: .+', line):
                break
            footer.append(line)
        if TRAILER not in footer:
            errors.append(f'Commit {index} lacks the required policy trailer.')
    return errors


def main():
    event_name = os.environ.get('GITHUB_EVENT_NAME', 'local')
    event = json.loads(Path(os.environ['GITHUB_EVENT_PATH']).read_text()) if os.environ.get('GITHUB_EVENT_PATH') else {}
    if event_name == 'pull_request':
        base, head = event['pull_request']['base']['sha'], event['pull_request']['head']['sha']
    elif event_name == 'merge_group':
        base, head = event['merge_group']['base_sha'], event['merge_group']['head_sha']
    else:
        base, head = event.get('before'), event.get('after', 'HEAD')
    # Force-pushed repair branches can have an event base that is no longer
    # reachable. In that case, validate the delivered commit rather than
    # accidentally traversing the repository's entire unrelated history.
    if base and set(base) != {'0'}:
        reachable = subprocess.run(['git', 'merge-base', '--is-ancestor', base, head]).returncode == 0
        revision = f'{base}..{head}' if reachable else head + '^!'
    else:
        revision = head + '^!'
    commits = subprocess.check_output(['git', 'rev-list', '--no-merges', revision], text=True).splitlines()
    messages = [subprocess.check_output(['git', 'show', '-s', '--format=%B', sha], text=True) for sha in commits]
    errors = agreement_errors(event_name, event, messages)
    for error in errors:
        print("::error title=Policy acknowledgment::" + error)
    print(f'Checked {len(messages)} non-merge commit acknowledgments; {len(errors)} failures. Merge protection remains administrator-controlled.')
    return int(bool(errors))


if __name__ == '__main__':
    sys.exit(main())
