#!/usr/bin/env python3
"""Publish compact, actionable hosted findings without downloading artifacts.

The report is processed on the GitHub runner while the XML is available.  The
full XML remains an artifact, but remediation does not depend on later blob
storage access.  The output is GitHub-step-summary-safe plain Markdown.
"""
from __future__ import annotations

import argparse
import glob
import html
import xml.etree.ElementTree as ET
from collections import Counter
from pathlib import Path


def files(patterns: list[str]) -> list[Path]:
    return sorted({Path(p) for pattern in patterns for p in glob.glob(pattern, recursive=True)})


def detekt(paths: list[Path]) -> tuple[int, Counter[str], list[str]]:
    count = 0
    rules: Counter[str] = Counter()
    examples: list[str] = []
    for path in paths:
        try:
            root = ET.parse(path).getroot()
        except (OSError, ET.ParseError):
            continue
        for issue in root.iter("issue"):
            count += 1
            rule = issue.attrib.get("id", "unknown")
            rules[rule] += 1
            if len(examples) < 20:
                examples.append(
                    f"`{html.escape(issue.attrib.get('location','?'))}` — {html.escape(rule)}: "
                    f"{html.escape(issue.attrib.get('message',''))}"
                )
    return count, rules, examples


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--detekt", action="append", default=[])
    parser.add_argument("--ktlint", action="append", default=[])
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    detekt_paths = files(args.detekt)
    ktlint_paths = files(args.ktlint)
    count, rules, examples = detekt(detekt_paths)
    lines = ["## Hosted quality findings", "", "This summary was generated on the hosted runner before artifact upload.", ""]
    lines.append(f"- Detekt XML reports: **{len(detekt_paths)}**")
    lines.append(f"- Detekt findings: **{count}**")
    lines.append(f"- KtLint XML reports: **{len(ktlint_paths)}**")
    lines.append("")
    if rules:
        lines += ["### Detekt rules by volume", "", "| Rule | Findings |", "|---|---:|"]
        lines += [f"| `{rule}` | {n} |" for rule, n in rules.most_common(30)]
    if examples:
        lines += ["", "### First 20 exact findings", ""] + [f"- {e}" for e in examples]
    if not detekt_paths and not ktlint_paths:
        lines += ["", "> No XML reports were present; this is not a PASS."]
    Path(args.output).write_text("\n".join(lines) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
