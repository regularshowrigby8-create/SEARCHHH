"""Offline audit-integrity checks. These do NOT test provider inference/entitlement."""
import json
import unittest
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[2]
DOCS = ROOT / "docs" / "searchhh"


class AiModelAuditTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.audit = json.loads((DOCS / "ai/model-audit-2026-09-29.json").read_text())
        cls.report = (DOCS / "AI-MODEL-AUDIT.md").read_text()

    def test_all_original_numbers_are_covered_exactly_once(self):
        numbers = [n for group in self.audit["original_groups"]
                   for n in group["original_numbers"]]
        self.assertEqual(sorted(numbers), list(range(1, 101)))
        for group in self.audit["original_groups"]:
            self.assertTrue(group["verification_scope"])
            self.assertTrue(group["status"])
            self.assertTrue(group["decision"])

    def test_every_classification_has_resolvable_official_evidence(self):
        sources = self.audit["sources"]
        for row in self.audit["original_groups"] + self.audit["additions"]:
            self.assertTrue(row["sources"])
            for source in row["sources"]:
                self.assertIn(source, sources)
                url = urlsplit(sources[source])
                self.assertEqual(url.scheme, "https")
                self.assertTrue(url.hostname)
                self.assertIsNone(url.username)
                self.assertIsNone(url.password)
                self.assertFalse(url.query)
                self.assertIn(sources[source], self.report)

    def test_added_routes_are_unique_but_not_miscounted_as_families(self):
        rows = self.audit["additions"]
        self.assertEqual(len({row["id"] for row in rows}), len(rows))
        self.assertEqual(len({(row["provider"], row["model_id"]) for row in rows}), len(rows))
        # Alternative provider routes must keep the same family identity.
        for family in ("gpt-oss-20b", "qwen3.8-27b"):
            routes = [r for r in rows if r["model_family"] == family]
            self.assertGreater(len({r["provider"] for r in routes}), 1)

    def test_research_never_implicitly_enables_an_adapter(self):
        allowed = {"documented_free_tier", "current_catalog_free_plan_candidate",
                   "shared_allowance_candidate", "legacy_free_listing_needs_live_check"}
        for row in self.audit["additions"]:
            self.assertIs(row["runtime_enabled"], False)
            self.assertIs(row["inference_tested"], False)
            self.assertEqual(row["role"], "text_review_candidate")
            self.assertIn(row["status"], allowed)
            self.assertTrue(row["note"])
            self.assertIn(row["model_id"], self.report)

    def test_endpoint_observations_are_not_promoted_to_working_models(self):
        observations = self.audit["openrouter_endpoint_observations"]
        self.assertEqual(sum(o["part_of_supplied_nine"] for o in observations), 9)
        self.assertEqual(len({o["model_id"] for o in observations}), len(observations))
        for obs in observations:
            self.assertEqual(obs["observed_endpoints"], [])
            self.assertEqual(obs["verification"], "public_endpoint_metadata_only")
            self.assertEqual(obs["url"],
                             f'https://openrouter.ai/api/v1/models/{obs["model_id"]}/endpoints')
            self.assertNotIn(("openrouter", obs["model_id"]),
                             [(a["provider"], a["model_id"]) for a in self.audit["additions"]])

    def test_deliverables_explain_scope_and_have_implementation_plan(self):
        self.assertIn("not runtime configuration", self.audit["scope"])
        self.assertIn("APK remains unchanged", self.report)
        self.assertTrue((DOCS / "AI-HIVE-KEY-PLAN.md").is_file())


if __name__ == "__main__":
    unittest.main()
