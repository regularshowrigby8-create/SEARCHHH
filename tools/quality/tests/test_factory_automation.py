"""Behavior and fail-closed evidence tests for the expanded development factory."""

import hashlib
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from tools.factory import automation, factory


class FactoryAutomationTest(unittest.TestCase):
    def test_unknown_changes_conservatively_select_every_engine(self):
        result = automation.plan(["unknown/new.file"])
        self.assertEqual(len(result["tools"]), 72)
        self.assertTrue(result["canonical_gates_still_required"])
        self.assertFalse(result["release_approved"])

    def test_backend_changes_include_actual_api_and_conflict_checks(self):
        result = automation.plan(["backend/searchhh/api.py"])
        self.assertTrue(
            {"pytest", "openapi-spec-validator", "pipdeptree", "bandit"}
            <= set(result["tools"])
        )
        self.assertNotIn("apksigner", result["tools"])

    def test_asset_changes_include_browser_a11y_and_canonical_diagnostics(self):
        selected = automation.plan(["app/src/main/assets/error_page.html"])["tools"]
        self.assertTrue(
            {"playwright", "axe-core", "android-lint", "html-validate"} <= set(selected)
        )

    def test_no_changes_is_not_a_quality_pass(self):
        result = automation.plan([])
        self.assertEqual(result["state"], "NO_CHANGES")
        self.assertEqual(result["tools"], [])
        self.assertFalse(result["release_approved"])

    def test_unsafe_path_never_routes(self):
        for path in ["../escape", ".git/config", "/etc/passwd"]:
            with self.subTest(path=path), self.assertRaises(ValueError):
                automation.plan([path])

    def fixture(self, root, data):
        report = root / "build/reports/factory/run/ruff/output.log"
        report.parent.mkdir(parents=True)
        payload = json.dumps(data).encode()
        report.write_bytes(payload)
        return {
            "source_commit": "fixture",
            "results": [
                {
                    "id": "ruff",
                    "status": "FAIL",
                    "log": str(report.relative_to(root)),
                    "log_sha256": hashlib.sha256(payload).hexdigest(),
                }
            ],
        }

    def test_packets_preserve_aliases_without_messages_or_closing(self):
        with tempfile.TemporaryDirectory() as directory:
            data = [
                {
                    "code": "F401",
                    "filename": "backend/searchhh/api.py",
                    "location": {"row": row},
                    "message": "PRIVATE_MARKER",
                }
                for row in [1, 2]
            ]
            summary = self.fixture(Path(directory), data)
            result = automation.summarize(summary, Path(directory))
            self.assertEqual(result["raw_aliases_preserved"], 2)
            self.assertEqual(len(result["packets"]), 1)
            self.assertFalse(result["packets"][0]["closed"])
            self.assertEqual(result["job_counts"], {"FAIL": 1})
            self.assertNotIn("PRIVATE_MARKER", json.dumps(result))

    def test_tampered_report_is_not_an_empty_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            summary = self.fixture(Path(directory), [])
            summary["results"][0]["log_sha256"] = "mismatch"
            result = automation.summarize(summary, Path(directory))
            self.assertEqual(len(result["parse_errors"]), 1)
            self.assertFalse(result["release_approved"])

    def test_stylelint_uses_authenticated_stderr_not_empty_stdout(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            summary = self.fixture(root, [])
            result = summary["results"][0]
            result["id"] = "stylelint"
            log = root / result["log"]
            data = json.dumps(
                [
                    {
                        "source": "app/src/main/assets/highlight.css",
                        "warnings": [{"rule": "block-no-empty", "line": 3}],
                    }
                ]
            ).encode()
            log.with_name("stderr.log").write_bytes(data)
            result["stderr_sha256"] = hashlib.sha256(data).hexdigest()
            parsed = automation.summarize(summary, root)
            self.assertEqual(parsed["raw_aliases_preserved"], 1)
            self.assertEqual(parsed["parse_errors"], [])

    def test_secret_report_parser_never_copies_secret_or_match(self):
        record = {
            "RuleID": "generic-api-key",
            "File": "backend/example.py",
            "StartLine": 4,
            "Secret": "PRIVATE_MARKER",
            "Match": "PRIVATE_MARKER",
        }
        metadata = automation.extract("gitleaks", [record])
        self.assertNotIn("PRIVATE_MARKER", json.dumps(metadata))

    def test_app_security_policy_detects_cleartext_and_allows_delegated_tls_handler(self):
        from tools.factory.app_security_policy import scan
        report = scan()
        rules = {item['rule'] for item in report['findings']}
        self.assertIn('APP-CLEAR-001', rules)
        self.assertIn('APP-CLEAR-002', rules)
        self.assertNotIn('WEBVIEW-TLS-001', rules)
        self.assertNotIn('APP-SECRET-001', rules)
        self.assertEqual(report['reviewed_public_constants'][0]['symbol'], 'TRUSTED_CLIENT_TOKEN')
        self.assertFalse(report['release_approved'])

    def test_auto_issue_ecosystems_are_bounded_to_selected_tools(self):
        from tools.factory.auto_issue import ecosystems, selected_tools
        tools = selected_tools('api')
        self.assertEqual(ecosystems(tools), ['node', 'python'])
        self.assertNotIn('binary', ecosystems(tools))

    def test_auto_issue_has_two_attempt_bound(self):
        from tools.factory.auto_issue import MAX_ATTEMPTS, MAX_PROVISION_SECONDS
        self.assertEqual(MAX_ATTEMPTS, 2)
        self.assertEqual(MAX_PROVISION_SECONDS, 900)

    def test_remediation_plan_assigns_security_owner_without_auto_fix(self):
        from tools.factory.remediation_plan import plan
        result = plan({'findings': [{'rule': 'APP-CLEAR-001', 'path': 'app/src/main/AndroidManifest.xml', 'line': 78}]}, {'results': [{'id': 'gitleaks', 'status': 'BLOCKED'}]})
        self.assertEqual(result['open_count'], 2)
        self.assertEqual(result['actions'][0]['owner'], 'security-owner')
        self.assertFalse(result['actions'][0]['auto_fix'])
        self.assertFalse(result['release_approved'])

    def test_unsupported_output_keeps_original_failure_visible(self):
        result = automation.summarize(
            {"results": [{"id": "osv-scanner", "status": "FAIL"}]}
        )
        self.assertEqual(result["opaque_jobs"][0]["status"], "FAIL")
        self.assertEqual(result["job_counts"], {"FAIL": 1})

    def test_missing_browser_is_blocked_before_fake_execution(self):
        tool = next(t for t in factory.load_tools() if t["id"] == "playwright")
        with (
            tempfile.TemporaryDirectory() as directory,
            patch.object(factory, "HOME", Path(directory)),
            self.assertRaisesRegex(FileNotFoundError, "Chromium missing"),
        ):
            factory.expand_command(tool, Path(directory), None)

    def test_all_engines_have_repositories_and_candidates_are_separate(self):
        manifest = json.loads(
            (factory.ROOT / "tools/factory/repositories.json").read_text()
        )
        self.assertEqual(
            set(manifest["repositories"]), {tool["id"] for tool in factory.load_tools()}
        )
        self.assertTrue(
            all(
                r["repository"].startswith("https://")
                for r in manifest["repositories"].values()
            )
        )
        self.assertEqual(len(manifest["planned"]), 9)

    def test_absent_effort_data_cannot_claim_productivity(self):
        self.assertEqual(automation.savings([])["state"], "NOT_MEASURED")

    def test_measured_savings_use_paired_active_effort_not_tool_count(self):
        samples = [
            {
                "id": str(i),
                "before_minutes": 10,
                "after_minutes": 3,
                "required_checks_complete": True,
            }
            for i in range(10)
        ]
        result = automation.savings(samples)
        self.assertAlmostEqual(result["reduction"], 0.7)
        samples[0]["required_checks_complete"] = False
        with self.assertRaises(ValueError):
            automation.savings(samples)

    def test_duplicate_and_nonfinite_effort_samples_rejected(self):
        samples = [
            {
                "id": str(i),
                "before_minutes": 10,
                "after_minutes": 2,
                "required_checks_complete": True,
            }
            for i in range(10)
        ]
        samples[0]["after_minutes"] = float("nan")
        with self.assertRaises(ValueError):
            automation.savings(samples)
        samples[0]["after_minutes"] = 2
        samples[0]["id"] = "1"
        with self.assertRaises(ValueError):
            automation.savings(samples)

    def test_dashboard_snapshot_label_matches_its_real_counts(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "map.html"
            automation.write_overview(path)
            body = path.read_text()
            self.assertIn("56 executed", body)
            self.assertIn("3f061ae", body)
            self.assertIn("24 PASS", body)
            self.assertNotIn("@@", body)
            self.assertNotIn("Historical source 25f9401", body)

    def test_dashboard_escapes_tool_labels(self):
        tool = dict(factory.load_tools()[0], name="<script>attack()</script>")
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "map.html"
            automation.write_overview(path, [tool])
            body = path.read_text()
            self.assertIn("&lt;script&gt;attack()", body)
            self.assertNotIn("<script>attack()", body)


if __name__ == "__main__":
    unittest.main()
