#!/usr/bin/env python3
"""Fail-closed lexical triage, not a proof of runtime correctness.

Strings are masked; comments are retained separately for unfinished markers.
Exact, reviewed exceptions are fingerprinted and expire. No debt baseline.
"""
import argparse
import ast
from dataclasses import asdict, dataclass
from datetime import date
import hashlib
import json
from pathlib import Path
import re
import sys

EXTENSIONS = {'.kt', '.kts', '.java', '.js', '.jsx', '.ts', '.tsx', '.py', '.c', '.cpp', '.cc', '.h', '.sh'}
OUTPUTS = {'build', 'app/build', 'ad-filter/build', 'adblock-client/build', '.git', '.gradle',
           'node_modules', 'js-tests/node_modules', '.venv', 'backend/.venv', '.cache',
           '.cxx', '.externalNativeBuild', '.kotlin'}
OUTPUTS |= {f'{module}/{directory}' for module in ['app', 'ad-filter', 'adblock-client']
            for directory in ['.cxx', '.externalNativeBuild']}
MARKERS = re.compile(r'\b(?:TODO|FIXME|HACK|XXX|IMPLEMENT_THIS|not\s+implemented|coming\s+soon)\b', re.I)
LEX = re.compile(r'("""[\s\S]*?"""|\'\'\'[\s\S]*?\'\'\'|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|`(?:\\.|[^`\\])*`)|(/\*[\s\S]*?\*/|//[^\n]*|\#[^\n]*)')


@dataclass(frozen=True)
class Finding:
    file: str
    line: int
    rule: str
    fingerprint: str


def source_files(root):
    # Walk rather than trusting gitignore: ignoring a new source file cannot hide it.
    import os
    for folder, dirs, files in os.walk(root):
        relative = Path(folder).relative_to(root)
        dirs[:] = sorted(d for d in dirs if (relative / d).as_posix() not in OUTPUTS
                         and d not in {'__pycache__', '.pytest_cache'})
        for name in sorted(files):
            path = Path(folder) / name
            if path.suffix in EXTENSIONS:
                if path.is_symlink():
                    raise ValueError(f'Source symlink requires explicit review: {path.relative_to(root)}')
                yield path


def masked(text):
    comments = []
    def replace(match):
        if match.group(2):
            comments.append((match.start(), match.group()))
        return re.sub(r'[^\n]', ' ', match.group())
    return LEX.sub(replace, text), comments


def closing(code, start):
    depth = 0
    for i in range(start, len(code)):
        if code[i] == '{':
            depth += 1
        elif code[i] == '}':
            depth -= 1
            if depth == 0:
                return i
    return None


def scan_text(path, text):
    code, comments = masked(text)
    uncommented = LEX.sub(lambda match: match.group(1) if match.group(1) else re.sub(r'[^\n]', ' ', match.group()), text)
    findings = []
    def add(rule, start, end):
        line = text.count('\n', 0, start) + 1
        # Include complete containing lines so moving a finding preserves identity,
        # but editing its context invalidates an exception. Do not print source/secrets.
        first = text.rfind('\n', 0, start) + 1
        last = text.find('\n', end)
        context = text[first:last if last >= 0 else len(text)].strip()
        fingerprint = hashlib.sha256((rule + '\n' + context).encode()).hexdigest()
        findings.append(Finding(path, line, rule, fingerprint))
    for offset, comment in comments:
        for match in MARKERS.finditer(comment):
            add('unfinished-marker', offset + match.start(), offset + match.end())
    for match in re.finditer(r'\b(?:TODO\s*\(|NotImplementedError\b|NotImplementedException\b)', code):
        add('unfinished-implementation', match.start(), match.end())
    for match in re.finditer(r'@(?:Ignore|Disabled)\b|(?:pytest\.mark|unittest)\.skip\b|\b(?:test|it|describe)\.skip\s*\(', code):
        add('disabled-test', match.start(), match.end())
    empty = re.compile(r'\s*(?:(?:[\w:,?<> ]+|\([^)]*\))\s*->\s*)?(?:return(?:@\w+)?\s*(?:Unit)?\s*;?)?\s*')
    for match in re.finditer(r'\bcatch\s*\([^)]*\)\s*\{', code):
        start = match.end() - 1
        end = closing(code, start)
        if end is not None and empty.fullmatch(code[start + 1:end]):
            add('silent-catch', match.start(), end + 1)
    callbacks = r'\b(?:on[A-Z]\w*|action|confirmAction|cancelAction)\s*=\s*\{|\.(?:onFailure|onSuccess)\s*\{|(?:->|=>)\s*\{'
    for match in re.finditer(callbacks, code):
        start = match.end() - 1
        end = closing(code, start)
        if end is not None and empty.fullmatch(code[start + 1:end]):
            add('empty-action', match.start(), end + 1)
    sentinel = r'(?:null|false|emptyList\s*\([^)]*\)|emptyMap\s*\([^)]*\)|emptySet\s*\([^)]*\)|listOf\s*\(\s*\)|mapOf\s*\(\s*\))'
    functions = r'\bfun\s+(?:<[^>]+>\s*)?[\w.<>?]+\s*\([^)]*\)\s*(?::\s*[^={\n]+)?\s*'
    for match in re.finditer(functions + r'\{', code):
        start = match.end() - 1
        end = closing(code, start)
        if end is not None:
            body = code[start + 1:end]
            raw_body = uncommented[start + 1:end]
            if not body.strip() and not raw_body.strip():
                add('empty-function-review', match.start(), end + 1)
            elif re.fullmatch(r'\s*return\s+' + sentinel + r'\s*;?\s*', body) or re.fullmatch(r'\s*return\s*(?:""|\'\')\s*;?\s*', raw_body):
                add('constant-return-review', match.start(), end + 1)
    for match in re.finditer(functions + r'=\s*' + sentinel + r'\s*(?=\n|$)', code):
        add('constant-return-review', match.start(), match.end())
    for match in re.finditer(functions + r'=', code):
        tail = uncommented[match.end():].split('\n', 1)[0].strip()
        if tail in ('""', "''", 'Unit'):
            add('constant-return-review', match.start(), match.end() + len(tail))
    if path.endswith('.py'):
        try:
            tree = ast.parse(text)
        except SyntaxError as error:
            add('python-syntax-error', 0, 0)
        else:
            lines = text.splitlines(keepends=True)
            for node in ast.walk(tree):
                if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef, ast.ExceptHandler)):
                    body = list(node.body)
                    if body and isinstance(body[0], ast.Expr) and isinstance(body[0].value, ast.Constant) and isinstance(body[0].value.value, str):
                        body = body[1:]
                    if len(body) == 1 and (isinstance(body[0], ast.Pass) or
                            isinstance(body[0], ast.Return) and
                            (body[0].value is None or
                             isinstance(body[0].value, ast.Constant) and body[0].value.value in (None, False, '') or
                             isinstance(body[0].value, (ast.List, ast.Tuple, ast.Set)) and not body[0].value.elts or
                             isinstance(body[0].value, ast.Dict) and not body[0].value.keys)):
                        start = sum(map(len, lines[:node.lineno - 1]))
                        add('silent-catch' if isinstance(node, ast.ExceptHandler) else 'constant-return-review', start, start)
    return sorted(set(findings), key=lambda item: (item.file, item.line, item.rule))


def apply_exceptions(findings, exceptions, root, today=None):
    today = today or date.today()
    remaining = list(findings)
    errors = []
    seen = set()
    for entry in exceptions:
        try:
            identity = (entry['file'], entry['rule'], entry['fingerprint'])
            if identity in seen:
                raise ValueError('duplicate exception')
            seen.add(identity)
            if entry['kind'] != 'legitimate' or len(entry['reason'].strip()) < 30:
                raise ValueError('only explained legitimate cases, never unfinished debt')
            if not entry['owner'].strip() or date.fromisoformat(entry['expires']) < today:
                raise ValueError('missing owner or expired review')
            test = (root / entry['regression_test']).resolve()
            if not test.is_relative_to(root.resolve()) or not test.is_file():
                raise ValueError('missing repository regression test')
            matching = [f for f in remaining if (f.file, f.rule, f.fingerprint) == identity]
            if len(matching) != 1:
                raise ValueError('exception must match exactly one current finding')
            remaining.remove(matching[0])
        except (KeyError, TypeError, ValueError) as error:
            errors.append(f'Invalid exception: {error}')
    return remaining, errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument('--report', type=Path, default=Path('build/reports/searchhh/unfinished.json'))
    args = parser.parse_args()
    findings = []
    errors = []
    scanned = 0
    try:
        for path in source_files(args.root):
            scanned += 1
            findings.extend(scan_text(path.relative_to(args.root).as_posix(), path.read_text(encoding='utf-8')))
        exceptions = json.loads((args.root / 'quality/unfinished-exceptions.json').read_text())
        if not isinstance(exceptions, list):
            raise ValueError('exceptions must be a list')
        findings, errors = apply_exceptions(findings, exceptions, args.root)
    except (ValueError, OSError, UnicodeError) as error:
        errors.append(str(error))
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps({'scanned_files': scanned, 'findings': [asdict(f) for f in findings], 'errors': errors}, indent=2) + '\n')
    for finding in findings:
        print(f'{finding.file}:{finding.line}: {finding.rule} [{finding.fingerprint[:12]}]')
    for error in errors:
        print(error, file=sys.stderr)
    print(f'Scanned {scanned} files; {len(findings)} findings; {len(errors)} checker errors. Report: {args.report}')
    return 1 if findings or errors else 0


if __name__ == '__main__':
    sys.exit(main())
