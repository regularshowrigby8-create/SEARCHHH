import unittest

from tools.factory.summarize_run import summarize


class SummarizeRunTest(unittest.TestCase):
    def test_failed_required_job_blocks_release(self):
        result = summarize({
            "policy-and-source": {"result": "success"},
            "android-host": {"result": "failure"},
            "device-and-evidence": {"result": "success"},
            "required-quality": {"result": "failure"},
        })
        self.assertEqual(result["status"], "BLOCKED_OR_FINDINGS")
        self.assertFalse(result["release_approved"])
        self.assertFalse(result["apk_link_allowed"])

    def test_missing_job_is_incomplete(self):
        result = summarize({"policy-and-source": {"result": "success"}})
        self.assertEqual(result["status"], "INCOMPLETE")
        self.assertFalse(result["release_approved"])

    def test_only_four_successful_required_jobs_approve(self):
        needs = {name: {"result": "success"} for name in (
            "policy-and-source", "android-host", "device-and-evidence", "required-quality"
        )}
        self.assertTrue(summarize(needs)["release_approved"])


if __name__ == "__main__":
    unittest.main()
