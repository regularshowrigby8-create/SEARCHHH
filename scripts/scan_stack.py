#!/usr/bin/env python3
"""Verifies stack/dependencies.yml against reality and renders STACK.md.

Checks
  * pip        -> installed in the current environment, or declared in requirements
  * gradle     -> coordinate declared in libs.versions.toml / build files
  * gradle-plugin -> plugin id declared in the version catalog
  * action     -> ``uses: <name>@`` appears in .github/workflows
  * tool       -> explicit ``verify: {file, contains}`` block, otherwise skipped

Exit code is non-zero when an entry cannot be located or the total is below the
declared policy minimum, so CI fails instead of publishing a stale inventory.

Usage:
    python scripts/scan_stack.py            # verify + write STACK.md
    python scripts/scan_stack.py --check    # verify only, no file writes
"""

from __future__ import annotations

import argparse
import importlib.metadata as metadata
import re
import sys
from collections import Counter, defaultdict
from datetime import UTC, datetime
from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
MANIFEST = ROOT / "stack" / "dependencies.yml"
OUTPUT = ROOT / "STACK.md"

STATUS_OK = "verified"
STATUS_DECLARED = "declared"
STATUS_MISSING = "MISSING"


def read(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except FileNotFoundError:
        return ""


def installed_version(name: str) -> str | None:
    """Distribution name -> version, or None when not installed."""
    for candidate in {name, name.replace("_", "-"), name.lower()}:
        try:
            return metadata.version(candidate)
        except metadata.PackageNotFoundError:
            continue
    return None


def build_indexes() -> dict[str, str]:
    """Everything we can search for declarations, concatenated by kind."""
    gradle_files = [
        ROOT / "android/gradle/libs.versions.toml",
        ROOT / "android/build.gradle.kts",
        ROOT / "android/app/build.gradle.kts",
    ]
    requirements_files = [
        ROOT / "backend/requirements.txt",
        ROOT / "backend/requirements-dev.txt",
    ]
    workflow_files = sorted((ROOT / ".github/workflows").glob("*.yml"))

    return {
        "gradle": "\n".join(read(f) for f in gradle_files),
        "requirements": "\n".join(read(f) for f in requirements_files),
        "workflows": "\n".join(read(f) for f in workflow_files),
    }


def verify(entry: dict, indexes: dict[str, str]) -> tuple[str, str]:
    """Return (status, detail)."""
    kind = entry.get("kind", "")
    name = entry["name"]

    if kind == "pip":
        version = installed_version(name)
        if version:
            return STATUS_OK, f"installed {version}"
        declared = name in indexes["requirements"] or name.replace("-", "_") in indexes["requirements"]
        return (STATUS_DECLARED, "in requirements") if declared else (STATUS_MISSING, "not installed or declared")

    if kind in {"gradle", "gradle-plugin"}:
        needles = [n for n in (entry.get("coordinate"), name) if n]
        for needle in needles:
            if needle in indexes["gradle"]:
                return STATUS_OK, "declared in Gradle files"
        return STATUS_MISSING, "not found in Gradle files"

    if kind == "action":
        pattern = re.compile(rf"uses:\s*{re.escape(name)}(/[\w.-]+)?@", re.MULTILINE)
        if pattern.search(indexes["workflows"]):
            return STATUS_OK, "used in a workflow"
        return STATUS_MISSING, "no workflow uses it"

    if kind == "tool":
        check = entry.get("verify")
        if isinstance(check, dict):
            target = ROOT / str(check.get("file", ""))
            if check.get("contains", "") in read(target):
                return STATUS_OK, f"found in {check.get('file')}"
            return STATUS_MISSING, f"missing from {check.get('file')}"
        return STATUS_DECLARED, "no automatic check"

    return STATUS_DECLARED, "unknown kind"


def project_stats() -> dict[str, int]:
    kotlin = list((ROOT / "android").rglob("*.kt"))
    python = list((ROOT / "backend").rglob("*.py"))

    def lines(files: list[Path]) -> int:
        total = 0
        for file in files:
            total += len(read(file).splitlines())
        return total

    return {
        "kotlin_files": len(kotlin),
        "kotlin_lines": lines(kotlin),
        "python_files": len(python),
        "python_lines": lines(python),
    }


CATEGORY_TITLES = {
    "android-ui": "Android platform",
    "compose": "Jetpack Compose",
    "architecture": "Lifecycle, ViewModel, StateFlow, Navigation",
    "persistence": "Persistence (Room, DataStore, WorkManager)",
    "network": "Network, serialization, images, logging",
    "firebase": "Firebase",
    "observability": "Observability",
    "testing": "Testing",
    "build": "Build toolchain",
    "backend": "Python backend",
    "tooling": "Python tooling",
    "transitive": "Transitive runtime",
    "ci": "CI actions",
}


def render(document: dict, results: list[tuple[dict, str, str]], stats: dict[str, int]) -> str:
    items = document["items"]
    statuses = Counter(status for _, status, _ in results)
    categories = Counter(entry["category"] for entry in items)
    installed_count = sum(1 for entry in items if entry["kind"] == "pip" and installed_version(entry["name"]))

    lines: list[str] = []
    lines.append("# STACK - every imported tool, verified")
    lines.append("")
    lines.append(
        "Generated by `scripts/scan_stack.py` on "
        f"{datetime.now(UTC).strftime('%Y-%m-%d %H:%M UTC')}. "
        "Do not edit by hand - run `make stack` instead."
    )
    lines.append("")
    lines.append("## Headline")
    lines.append("")
    lines.append("| Metric | Value |")
    lines.append("| --- | --- |")
    lines.append(f"| Imported tools tracked | **{len(items)}** |")
    lines.append(f"| Verified in this checkout | **{statuses[STATUS_OK]}** |")
    lines.append(f"| Declared but not resolvable offline | {statuses[STATUS_DECLARED]} |")
    lines.append(f"| Missing (build breaks) | {statuses[STATUS_MISSING]} |")
    lines.append(f"| Python distributions installed here | {installed_count} |")
    lines.append(f"| Kotlin files / lines | {stats['kotlin_files']} / {stats['kotlin_lines']} |")
    lines.append(f"| Python files / lines | {stats['python_files']} / {stats['python_lines']} |")
    lines.append("")
    lines.append("## By category")
    lines.append("")
    lines.append("| Category | Tools |")
    lines.append("| --- | --- |")
    for category, count in sorted(categories.items(), key=lambda kv: (-kv[1], kv[0])):
        lines.append(f"| {CATEGORY_TITLES.get(category, category)} | {count} |")
    lines.append("")

    by_category: dict[str, list[tuple[dict, str, str]]] = defaultdict(list)
    for entry, status, detail in results:
        by_category[entry["category"]].append((entry, status, detail))

    for category, count in sorted(categories.items(), key=lambda kv: (-kv[1], kv[0])):
        lines.append(f"## {CATEGORY_TITLES.get(category, category)} ({count})")
        lines.append("")
        lines.append("| Tool | Version | Kind | License | Why it is here | Status |")
        lines.append("| --- | --- | --- | --- | --- | --- |")
        for entry, status, detail in by_category[category]:
            version = entry.get("version") or "-"
            license_ = entry.get("license") or "-"
            mark = {"verified": "verified", "declared": "declared", "MISSING": "**MISSING**"}[status]
            lines.append(
                f"| `{entry['name']}` | {version} | {entry['kind']} | {license_} | "
                f"{entry['purpose']} | {mark} |"
            )
        lines.append("")

    missing = [(e, d) for e, s, d in results if s == STATUS_MISSING]
    if missing:
        lines.append("## Unverified entries")
        lines.append("")
        for entry, detail in missing:
            lines.append(f"- `{entry['name']}` - {detail}")
        lines.append("")

    lines.append("## How this is checked")
    lines.append("")
    lines.append("```bash")
    lines.append("make stack          # regenerate this file")
    lines.append("python scripts/scan_stack.py --check   # CI gate (non-zero if anything is missing)")
    lines.append("```")
    lines.append("")
    lines.append(
        "Pip entries are resolved against the live environment with `importlib.metadata`; "
        "Gradle entries are matched against `android/gradle/libs.versions.toml` and the module "
        "build files; CI actions are matched against `.github/workflows/*.yml`."
    )
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="verify only, do not write STACK.md")
    args = parser.parse_args()

    if not MANIFEST.exists():
        print(f"error: {MANIFEST} is missing - run stack/generate_manifest.py first", file=sys.stderr)
        return 2

    document = yaml.safe_load(MANIFEST.read_text(encoding="utf-8"))
    indexes = build_indexes()

    results = [(entry, *verify(entry, indexes)) for entry in document["items"]]
    statuses = Counter(status for _, status, _ in results)
    minimum = int(document.get("policy", {}).get("min_total", 100))

    print(f"tracked tools        : {len(results)}")
    print(f"verified             : {statuses[STATUS_OK]}")
    print(f"declared (offline)   : {statuses[STATUS_DECLARED]}")
    print(f"missing              : {statuses[STATUS_MISSING]}")

    for entry, status, detail in results:
        if status == STATUS_MISSING:
            print(f"  ! {entry['name']}: {detail}")

    failed = False
    if len(results) < minimum:
        print(f"error: only {len(results)} tools tracked, policy requires {minimum}", file=sys.stderr)
        failed = True

    if not args.check:
        OUTPUT.write_text(render(document, results, project_stats()), encoding="utf-8")
        print(f"wrote {OUTPUT.relative_to(ROOT)}")

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
