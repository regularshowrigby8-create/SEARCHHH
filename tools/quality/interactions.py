#!/usr/bin/env python3
"""Index lexical interaction candidates; manual reachability review is still mandatory."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
CONTROLS = re.compile(r'\b(Button|OutlinedButton|TextButton|IconButton|Checkbox|Switch|OutlinedTextField|TextField|Slider|DropdownMenuItem|BottomNavigationItem|RadioButton|ClickableText|Tab|NavigationDrawerItem)\s*\(|\.(clickable|combinedClickable|setOnClickListener|setOnLongClickListener|addTextChangedListener)\s*[({]|\b(onClick|onLongClick|onValueChange|onCheckedChange|onDismissRequest|action)\s*=')


def discover(root):
    rows = []
    for path in sorted((root / 'app/src/main').rglob('*')):
        if path.suffix not in {'.kt', '.java', '.xml'}:
            continue
        text = path.read_text()
        relative = path.relative_to(root).as_posix()
        duplicates = {}
        if path.suffix == '.xml':
            matches = list(re.finditer(r'<(?:[\w.]+\.)?(Button|ImageButton|EditText|CheckBox|Switch|SeekBar|\w*Preference)\b|android:onClick\s*=', text))
        else:
            # Do not count commented-out controls or examples embedded in strings.
            from tools.quality.unfinished import masked
            code, _ = masked(text)
            matches = list(CONTROLS.finditer(code))
        for match in matches:
            first = text.rfind('\n', 0, match.start()) + 1
            last = text.find('\n', match.start())
            context = ' '.join(text[first:last if last >= 0 else len(text)].split())
            kind = next((group for group in match.groups() if group), 'xml:onClick')
            identity = relative + ':' + kind + ':' + context
            duplicates[identity] = duplicates.get(identity, 0) + 1
            key = hashlib.sha256((identity + ':' + str(duplicates[identity])).encode()).hexdigest()[:12]
            rows.append(dict(id='UI-' + key, screen=path.stem, control=kind, file=relative,
                             line=text.count('\n', 0, match.start()) + 1))
    return rows


def markdown(rows, contracts):
    lines = ['# Interaction inventory', '',
             'Generated from production Kotlin/Java/XML control and callback candidates. A callback and its containing widget may be separate candidates. Preview candidates are included. This is a conservative index, **not a completed manual inventory or coverage claim**.', '',
             'Stable IDs identify source contexts; changing a context invalidates its contract instead of silently inheriting old evidence. Dynamic/custom widgets still require manual closure. `quality/interaction-contracts.json` supplies reviewed labels, actions, state and test links. Missing links block the evidence gate. Reserved test-tag constants do not establish implemented features.', '',
             'Regenerate: `python3 tools/quality/interactions.py --write`. Check: `python3 tools/quality/interactions.py --check`.', '',
             '| ID | Screen | Control | Visible label | Test tag | Expected action | State change | Test file | Status |',
             '|---|---|---|---|---|---|---|---|---|']
    for row in rows:
        contract = contracts.get(row['id'], {})
        fields = [row['id'], row['screen'], f"{row['control']} (`{row['file']}:{row['line']})",
                  contract.get('label', 'Review required'), contract.get('tag', 'Unassigned'),
                  contract.get('action', 'Not mapped'), contract.get('state', 'Not mapped'),
                  contract.get('test_file', 'Not linked'), 'Evidence required' if contract else 'UNVERIFIED']
        lines.append('| ' + ' | '.join(str(value).replace('|', '\\|').replace('\n', ' ') for value in fields) + ' |')
    return '\n'.join(lines) + '\n'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    modes = parser.add_mutually_exclusive_group(required=True)
    modes.add_argument('--write', action='store_true')
    modes.add_argument('--check', action='store_true')
    args = parser.parse_args()
    contracts = json.loads((ROOT / 'quality/interaction-contracts.json').read_text())
    rows = discover(ROOT)
    outputs = {ROOT / 'quality/interaction-inventory.json': json.dumps(rows, indent=2) + '\n',
               ROOT / 'docs/INTERACTION_INVENTORY.md': markdown(rows, contracts)}
    errors = []
    for path, expected in outputs.items():
        if args.write:
            path.write_text(expected)
        elif not path.exists() or path.read_text() != expected:
            errors.append(f'Stale or missing inventory: {path.relative_to(ROOT)}')
    stale = set(contracts) - {row['id'] for row in rows}
    # Route-level behavior contracts intentionally describe dynamic registry
    # navigation rather than one lexical widget. Keep them visible and require
    # their executed test evidence; only orphan lexical contracts are invalid.
    stale = {key for key in stale if not contracts[key].get('route')}
    errors.extend(f'Orphan contract: {key}' for key in sorted(stale))
    print(f'{len(rows)} interaction candidates, {len(contracts)} contracts. Manual and executed evidence are separately gated.')
    print('\n'.join(errors))
    return int(bool(errors))


if __name__ == '__main__':
    # Support both direct invocation and module-based test imports.
    sys.path.insert(0, str(ROOT))
    sys.exit(main())
