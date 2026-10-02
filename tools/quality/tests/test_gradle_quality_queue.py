import json
import tempfile
import unittest
from pathlib import Path
from unittest import mock

from tools.factory.gradle_quality_queue import main


class GradleQualityQueueTest(unittest.TestCase):
    def test_normalizes_reports_and_splits_packets(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            report = root / "detekt.xml"
            report.write_text("<checkstyle><file name='app/A.kt'><error line='8' column='2' source='LongMethod' message='too long'/></file></checkstyle>")
            output = root / "queue.json"
            with mock.patch("sys.argv", ["queue", "--detekt", str(report), "--output", str(output)]):
                self.assertEqual(main(), 0)
            result = json.loads(output.read_text())
        self.assertEqual(result["finding_count"], 1)
        self.assertEqual(result["packets"][0]["priority"], "P1")
        self.assertFalse(result["release_approved"])

    def test_missing_reports_are_not_a_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "queue.json"
            with mock.patch("sys.argv", ["queue", "--output", str(output)]):
                self.assertEqual(main(), 0)
            self.assertEqual(json.loads(output.read_text())["status"], "NO_REPORTS")


if __name__ == "__main__":
    unittest.main()
