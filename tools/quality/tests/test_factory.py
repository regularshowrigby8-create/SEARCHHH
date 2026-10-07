"""Regression tests for the real factory's fail-closed safety boundaries."""

import copy
import json
import os
import select
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from tools.factory import factory
from tools.factory.safety import safe_relative, validate_command, validate_tools


class FactoryTest(unittest.TestCase):
    def test_registry_has_64_distinct_real_jobs_not_help_aliases(self):
        tools = factory.load_tools()
        self.assertEqual(len(tools), 72)
        self.assertEqual(len({t["id"] for t in tools}), 72)
        self.assertTrue(
            all(
                t["purpose"] and t["profiles"] and t["upstream"].startswith("https://")
                for t in tools
            )
        )
        for tool in tools:
            validate_command(tool["command"])

    def test_duplicate_or_mutating_registry_fails(self):
        data = json.loads((factory.ROOT / "tools/factory/tools.json").read_text())
        for change in ["duplicate", "write", "timeout", "probe"]:
            item = copy.deepcopy(data)
            if change == "duplicate":
                item["tools"][1]["id"] = item["tools"][0]["id"]
            elif change == "write":
                item["tools"][0]["source_writes"] = True
            elif change == "timeout":
                item["tools"][0]["timeout_seconds"] = 0
            else:
                item["tools"][0]["command"] = ["git", "--version"]
            with self.subTest(change=change), self.assertRaises(ValueError):
                validate_tools(item)

    def test_shell_release_write_flags_rejected(self):
        for argv in [
            ["bash", "-c", "echo bad"],
            ["tool", "--fix=true"],
            ["git", "push"],
            ["./gradlew", ":app:assembleRelease"],
            ["apksigner", "sign", "x.apk"],
            ["x"],
        ]:
            with self.subTest(argv=argv), self.assertRaises(ValueError):
                validate_command(argv)

    def test_paths_reject_traversal_git_absolute_and_symlink_escape(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            for bad in ["../x", "/etc/x", ".git/config", "a//x", "a\\x", "C:/x", ""]:
                with self.subTest(bad=bad), self.assertRaises(ValueError):
                    safe_relative(root, bad)
            (root / "outside").symlink_to("/tmp")
            with self.assertRaises(ValueError):
                safe_relative(root, "outside/x")
            self.assertEqual(
                safe_relative(root, "app/example.apk"), root / "app/example.apk"
            )

    def test_installed_binary_is_not_reported_as_executed_by_doctor(self):
        tool = next(t for t in factory.load_tools() if t["id"] == "git")
        self.assertEqual(
            factory.expand_command(tool, Path("/tmp/out"), None),
            ["git", "diff", "--check"],
        )

    def test_missing_sdk_and_apk_are_blocked_not_fake_inputs(self):
        tool = next(t for t in factory.load_tools() if t["id"] == "apksigner")
        with (
            patch.dict(os.environ, {}, clear=True),
            self.assertRaises(FileNotFoundError),
        ):
            factory.expand_command(tool, Path("/tmp/out"), None)
        tool = {"command": ["x", "{apk}"], "requires": ["apk"]}
        with self.assertRaises(FileNotFoundError):
            factory.expand_command(tool, Path("/tmp/out"), None)

    def test_provider_and_signing_secrets_not_given_to_scanners(self):
        with patch.dict(
            os.environ,
            {
                "GH_TOKEN": "synthetic-token",
                "PROVIDER_KEY": "synthetic-key",
                "SEARCHHH_RELEASE_STORE_PASSWORD": "synthetic-password",
            },
        ):
            env = factory.tool_environment({"id": "ruff"}, Path("/tmp/out"))
            self.assertNotIn("GH_TOKEN", env)
            self.assertNotIn("PROVIDER_KEY", env)
            self.assertNotIn("SEARCHHH_RELEASE_STORE_PASSWORD", env)
            self.assertEqual(
                factory.tool_environment({"id": "gh"}, Path("/tmp/out"))["GH_TOKEN"],
                "synthetic-token",
            )

    def test_real_failure_and_timeout_are_preserved(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "log"
            code, reason = factory.execute(
                [sys.executable, "-c", "raise SystemExit(7)"],
                Path(d),
                os.environ.copy(),
                log,
                5,
            )
            self.assertEqual(code, 7)
            self.assertEqual(factory.classify("check", code, log, reason), "FAIL")
            code, reason = factory.execute(
                [sys.executable, "-c", "import time; time.sleep(10)"],
                Path(d),
                os.environ.copy(),
                log,
                0.1,
            )
            self.assertEqual(reason, "timeout")
            self.assertEqual(factory.classify("check", code, log, reason), "FAIL")

    def test_output_overflow_cannot_pass(self):
        with (
            tempfile.TemporaryDirectory() as d,
            patch.object(factory, "MAX_LOG_BYTES", 100),
        ):
            log = Path(d) / "log"
            code, reason = factory.execute(
                [sys.executable, "-c", 'print("x"*1000)'],
                Path(d),
                os.environ.copy(),
                log,
                5,
            )
            self.assertEqual(reason, "output-limit")
            self.assertLessEqual(log.stat().st_size, 100)
            self.assertEqual(factory.classify("report", code, log, reason), "FAIL")

    def test_zero_exit_secret_findings_are_still_failure(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "log"
            log.write_text(
                json.dumps({"results": {"source.py": [{"hashed_secret": "synthetic"}]}})
            )
            self.assertEqual(factory.classify("secret-scan", 0, log, None), "FAIL")
            log.write_text("{}")
            with self.assertRaises(ValueError):
                factory.classify("secret-scan", 0, log, None)
            log.write_text('{"results":{}}')
            self.assertEqual(factory.classify("secret-scan", 0, log, None), "PASS")
            self.assertEqual(factory.classify("report", 0, log, None), "REPORTED")
            self.assertEqual(factory.classify("search", 1, log, None), "REPORTED")
            self.assertEqual(factory.classify("search", 2, log, None), "FAIL")

    def test_content_edits_detected_even_when_git_status_would_still_say_modified(self):
        with (
            tempfile.TemporaryDirectory() as d,
            patch.object(factory.subprocess, "check_output", return_value=b"a.py\0"),
        ):
            root = Path(d)
            (root / "a.py").write_text("x=1")
            before = factory.source_snapshot(root)
            (root / "a.py").write_text("x=2")
            self.assertNotEqual(before, factory.source_snapshot(root))

    def test_job_source_mutation_cannot_pass(self):
        (factory.ROOT / "build").mkdir(exist_ok=True)
        tool = next(t for t in factory.load_tools() if t["id"] == "git")
        with (
            tempfile.TemporaryDirectory(dir=factory.ROOT / "build") as d,
            patch.object(factory, "source_snapshot", side_effect=["before", "after"]),
        ):
            result = factory.run_tool(tool, Path(d), None)
            self.assertEqual(result["status"], "FAIL")
            self.assertIn("Source changed", result["reason"])

    def test_json_stdout_is_not_corrupted_by_stderr(self):
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "output.log"
            code, reason = factory.execute(
                [
                    sys.executable,
                    "-c",
                    'import sys,json; print(json.dumps({"results":{}})); print("diagnostic", file=sys.stderr)',
                ],
                Path(directory),
                os.environ.copy(),
                log,
                5,
            )
            self.assertEqual(code, 0)
            self.assertIsNone(reason)
            self.assertEqual(factory.classify("secret-scan", code, log, reason), "PASS")
            self.assertIn("diagnostic", log.with_name("stderr.log").read_text())

    def test_stderr_overflow_is_part_of_combined_output_bound(self):
        with (
            tempfile.TemporaryDirectory() as directory,
            patch.object(factory, "MAX_LOG_BYTES", 100),
        ):
            log = Path(directory) / "output.log"
            code, reason = factory.execute(
                [
                    sys.executable,
                    "-c",
                    'import sys; print("x"*60); print("x"*60, file=sys.stderr)',
                ],
                Path(directory),
                os.environ.copy(),
                log,
                5,
            )
            self.assertEqual(reason, "output-limit")
            self.assertLessEqual(
                sum(path.stat().st_size for path in Path(directory).glob("*.log")), 100
            )
            self.assertEqual(factory.classify("check", code, log, reason), "FAIL")

    def test_cancelled_factory_does_not_spawn_queued_jobs(self):
        with (
            tempfile.TemporaryDirectory() as directory,
            patch.object(factory.subprocess, "Popen") as popen,
        ):
            factory.CANCELLED.set()
            try:
                with self.assertRaisesRegex(ValueError, "cancelled"):
                    factory.execute(
                        [sys.executable, "-c", "print(1)"],
                        Path(directory),
                        {},
                        Path(directory) / "log",
                        5,
                    )
                popen.assert_not_called()
            finally:
                factory.CANCELLED.clear()

    def test_descendants_stop_on_timeout_and_normal_parent_exit(self):
        for wait in [True, False]:
            with self.subTest(wait=wait), tempfile.TemporaryDirectory() as directory:
                marker = Path(directory) / "pid"
                child = "import signal,time; signal.signal(signal.SIGTERM, signal.SIG_IGN); time.sleep(30)"
                script = (
                    "import subprocess,sys,time; from pathlib import Path; "
                    f'p=subprocess.Popen([sys.executable,"-c",{child!r}]); '
                    f"Path({str(marker)!r}).write_text(str(p.pid)); "
                    + ("time.sleep(30)" if wait else "sys.exit(0)")
                )
                _, reason = factory.execute(
                    [sys.executable, "-c", script],
                    Path(directory),
                    os.environ.copy(),
                    Path(directory) / "log",
                    0.5,
                )
                self.assertEqual(reason, "timeout" if wait else None)
                try:
                    descriptor = os.pidfd_open(int(marker.read_text()))
                except ProcessLookupError:
                    continue  # Already reaped, so there is no surviving descendant.
                try:
                    ready, _, _ = select.select([descriptor], [], [], 2)
                    self.assertEqual(ready, [descriptor])
                finally:
                    os.close(descriptor)

    def test_duplicate_command_alias_cannot_inflate_tool_count(self):
        data = json.loads((factory.ROOT / "tools/factory/tools.json").read_text())
        data["tools"][1]["command"] = data["tools"][0]["command"]
        with self.assertRaisesRegex(ValueError, "Identical commands"):
            validate_tools(data)

    def test_invalid_json_cannot_erase_actual_execution_receipt(self):
        (factory.ROOT / "build").mkdir(exist_ok=True)
        tool = copy.deepcopy(factory.load_tools()[0])
        tool.update(
            id="invalid-json",
            kind="secret-scan",
            command=[sys.executable, "-c", 'print("invalid-json")'],
        )
        with tempfile.TemporaryDirectory(dir=factory.ROOT / "build") as directory:
            result = factory.run_tool(tool, Path(directory), None)
            self.assertEqual(result["status"], "FAIL")
            self.assertEqual(result["returncode"], 0)
            self.assertIn("log_sha256", result)

    def test_report_files_must_exist_and_be_valid_structured_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            out = Path(directory)
            tool = {
                "reports": [
                    {"path": "report.json", "format": "json", "count_field": "results"}
                ]
            }
            with self.assertRaises(ValueError):
                factory.report_metadata(tool, out)
            (out / "report.json").write_text('{"results":[1,2]}')
            self.assertEqual(factory.report_metadata(tool, out)[0]["records"], 2)
            (out / "report.json").write_text("null")
            with self.assertRaises(ValueError):
                factory.report_metadata(tool, out)
            tool["reports"][0]["path"] = "../escape"
            with self.assertRaises(ValueError):
                factory.report_metadata(tool, out)

    def test_uploaded_secret_reports_require_full_redaction(self):
        tool = next(tool for tool in factory.load_tools() if tool["id"] == "gitleaks")
        self.assertIn("--redact", tool["command"])
        self.assertFalse(any(arg.startswith("--redact=") for arg in tool["command"]))

    def test_registry_binary_archives_match_actual_installer_contract(self):
        from tools.factory import ci_evidence

        tools = factory.load_tools()
        for tool in tools:
            if tool["install"]["type"] == "github-binary":
                self.assertIn(tool["install"]["archive"], {"raw", "tar.gz"})
        with self.assertRaises(ValueError):
            ci_evidence.pages({"results": []}, "synthetic-digest")

    def test_triage_preserves_duplicate_observations_and_never_closes_s00(self):
        item = {
            "source": "a" * 40,
            "reports": [
                {
                    "id": "debug",
                    "tool": "lint",
                    "occurrences": [
                        {
                            "occurrence_id": "one",
                            "rule": "NewApi",
                            "locations": [{"file": "A.kt"}],
                        },
                        {
                            "occurrence_id": "two",
                            "rule": "NewApi",
                            "locations": [{"file": "A.kt"}],
                        },
                    ],
                }
            ],
        }
        result = factory.suggest(item)
        self.assertEqual(result["raw_observations_preserved"], 2)
        self.assertEqual(len(result["packets"]), 1)
        self.assertFalse(result["s00_closed"])
        self.assertFalse(result["packets"][0]["root_cause_confirmed"])
        self.assertFalse(result["packets"][0]["auto_fix_allowed"])
