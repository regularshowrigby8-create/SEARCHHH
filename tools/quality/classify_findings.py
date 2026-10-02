"""Classify unfinished-code findings before any repair or exception decision.

This tool is intentionally non-mutating. Imported, test and legacy code is not
silently exempted; it receives an explicit classification and remains open.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path

CATEGORIES = {
    "REAL_DEFECT", "INTENTIONAL_NO_OP", "DEFENSIVE_ERROR_HANDLING", "TEST_DOUBLE",
    "IMPORTED_CODE", "LEGACY_NEEDS_REFACTOR", "FALSE_POSITIVE", "UNKNOWN",
}


def classify(path: str, rule: str) -> tuple[str, str, bool]:
    normalized = path.replace("\\", "/")
    if "/src/androidTest/" in normalized or "/src/test/" in normalized or normalized.startswith("backend/tests/"):
        return "TEST_DOUBLE", "Test code requires targeted test review", False
    if "/assets/" in normalized or "/scriptlets_src/" in normalized or normalized.endswith(".min.js"):
        return "IMPORTED_CODE", "Review provenance and execution boundary before editing", True
    if normalized.startswith("ad-filter/") or normalized.startswith("adblock-client/"):
        return "LEGACY_NEEDS_REFACTOR", "Inherited module requires ownership and regression tests", False
    if rule == "silent-catch":
        return "DEFENSIVE_ERROR_HANDLING", "Decide recover, surface, log safely or propagate", False
    if rule == "empty-action":
        return "REAL_DEFECT", "Visible interaction requires a real state or navigation result", False
    if rule == "empty-function-review":
        return "UNKNOWN", "Inspect interface contract before changing", False
    return "UNKNOWN", "Requires source review", False


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("report", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    source = json.loads(args.report.read_text(encoding="utf-8"))
    findings = []
    for item in source.get("findings", []):
        category, reason, provenance_review = classify(item["file"], item["rule"])
        findings.append({
            **item,
            "category": category,
            "reason": reason,
            "provenance_review": provenance_review,
            "status": "OPEN",
            "owner": "review-required",
            "requires_human_approval": category in {"IMPORTED_CODE", "INTENTIONAL_NO_OP", "FALSE_POSITIVE"},
            "exception": None,
            "release_approved": False,
        })
    result = {
        "schema_version": 1,
        "status": "OPEN" if findings else "NO_FINDINGS",
        "source_report": str(args.report),
        "finding_count": len(findings),
        "category_counts": {category: sum(item["category"] == category for item in findings) for category in sorted(CATEGORIES)},
        "findings": findings,
        "release_approved": False,
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"status": result["status"], "finding_count": len(findings), "category_counts": result["category_counts"], "release_approved": False}, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
