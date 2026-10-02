import unittest

from tools.factory.run_status import classify_job, release_approved, validate


class RunStatusTest(unittest.TestCase):
    def test_partial_success_never_approves(self):
        self.assertFalse(release_approved(["EXECUTED_PASS", "BLOCKED"]))
        self.assertFalse(release_approved(["EXECUTED_PASS", "SKIPPED_POLICY"]))

    def test_all_required_checks_must_be_explicitly_passed(self):
        self.assertTrue(release_approved(["EXECUTED_PASS", "VERIFIED"], required_count=2))
        self.assertFalse(release_approved(["EXECUTED_PASS"], required_count=2))

    def test_github_job_results_are_not_generic_pass_fail(self):
        self.assertEqual(classify_job("success"), "EXECUTED_PASS")
        self.assertEqual(classify_job("skipped"), "SKIPPED_POLICY")
        self.assertEqual(classify_job(None), "RUNNING")

    def test_unknown_state_is_rejected(self):
        with self.assertRaises(ValueError):
            validate("PASS")


if __name__ == "__main__":
    unittest.main()
