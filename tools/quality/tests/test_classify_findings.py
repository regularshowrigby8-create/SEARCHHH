import json
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from tools.quality.classify_findings import main


class ClassifyFindingsTest(unittest.TestCase):
    def run_tool(self, finding):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report = root / "unfinished.json"
            output = root / "classified.json"
            report.write_text(json.dumps({"findings": [finding]}))
            with mock.patch("sys.argv", ["classify", str(report), "--output", str(output)]):
                self.assertEqual(main(), 0)
            return json.loads(output.read_text())["findings"][0]

    def test_empty_production_action_is_real_defect(self):
        result = self.run_tool({"file": "app/src/main/Screen.kt", "line": 10, "rule": "empty-action"})
        self.assertEqual(result["category"], "REAL_DEFECT")
        self.assertFalse(result["release_approved"])

    def test_imported_asset_requires_provenance_review(self):
        result = self.run_tool({"file": "app/src/main/assets/vendor.js", "line": 2, "rule": "silent-catch"})
        self.assertEqual(result["category"], "IMPORTED_CODE")
        self.assertTrue(result["provenance_review"])
        self.assertTrue(result["requires_human_approval"])

    def test_test_code_is_not_silently_exempted(self):
        result = self.run_tool({"file": "app/src/test/Fake.kt", "line": 2, "rule": "constant-return-review"})
        self.assertEqual(result["category"], "TEST_DOUBLE")
        self.assertEqual(result["status"], "OPEN")


if __name__ == "__main__":
    unittest.main()
