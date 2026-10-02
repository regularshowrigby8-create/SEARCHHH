import json
import tempfile
import unittest
from pathlib import Path

from tools.factory.language_router import detect


class LanguageRouterTest(unittest.TestCase):
    def test_mixed_changed_files_select_multiple_profiles(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            (repo / "gradlew").write_text("")
            result = detect(repo, ["app/src/main/HomeScreen.kt", "backend/api.py", "app/src/main/assets/page.html"])
        self.assertEqual(set(result["selected_languages"]), {"kotlin", "python", "html"})
        self.assertIn("ktlint", result["tools"])
        self.assertIn("ruff", result["tools"])
        self.assertIn("html-validate", result["tools"])

    def test_project_markers_select_profiles_even_without_changed_files(self):
        with tempfile.TemporaryDirectory() as directory:
            repo = Path(directory)
            (repo / "package.json").write_text("{}")
            result = detect(repo, ["README.md"])
        self.assertIn("javascript", result["project_languages"])
        self.assertIn("eslint", result["tools"])

    def test_unknown_or_empty_changes_do_not_claim_a_tool_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            result = detect(Path(directory), [])
        self.assertTrue(result["unknown_changes"])
        self.assertEqual(result["tools"], [])


if __name__ == "__main__":
    unittest.main()
