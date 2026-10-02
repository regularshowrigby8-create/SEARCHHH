import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from tools.factory import repair_handlers


class RepairHandlersTest(unittest.TestCase):
    def test_policy_accepts_exact_trailer(self):
        with tempfile.TemporaryDirectory() as directory:
            report = Path(directory) / "policy.json"
            with patch.object(repair_handlers.subprocess, "check_output", return_value="Subject\n\nSearchhh-Quality-Policy: accepted-v1\n"):
                self.assertEqual(repair_handlers.policy(report), 0)
            self.assertEqual(json.loads(report.read_text())["state"], "VERIFIED")

    def test_policy_does_not_rewrite_history(self):
        with tempfile.TemporaryDirectory() as directory:
            report = Path(directory) / "policy.json"
            with patch.object(repair_handlers.subprocess, "check_output", return_value="Subject\n"):
                self.assertEqual(repair_handlers.policy(report), 2)
            result = json.loads(report.read_text())
            self.assertEqual(result["state"], "REPAIR_REQUIRED")
            self.assertIn("history is not rewritten", result["action"])

    def test_format_commands_are_explicit(self):
        with patch.object(repair_handlers, "run", return_value=0) as runner:
            self.assertEqual(repair_handlers.gradle("ktlint", True, Path(tempfile.mktemp())), 0)
            self.assertIn("ktlintFormat", runner.call_args.args[0])
            self.assertEqual(repair_handlers.gradle("detekt", True, Path(tempfile.mktemp())), 0)
            self.assertIn("--auto-correct", runner.call_args.args[0])


if __name__ == "__main__":
    unittest.main()
