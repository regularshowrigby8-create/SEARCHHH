import unittest

from tools.factory.finding_lifecycle import transition


class FindingLifecycleTest(unittest.TestCase):
    def packet(self, **extra):
        value = {"id": "UI-123", "state": "OPEN", "release_approved": False}
        value.update(extra)
        return value

    def test_valid_repair_sequence_requires_recheck_and_evidence(self):
        packet = transition(self.packet(), "ASSIGNED", actor="agent")
        packet = transition(packet, "AUTOFIX_ATTEMPTED", actor="agent", reason="Formatting-only repair attempted")
        packet = transition(packet, "RECHECK_REQUIRED", actor="agent", reason="Independent checks must run")
        packet = transition(packet, "FIXED_PENDING_REVIEW", actor="ci", evidence=["report.json"])
        packet["required_checks_complete"] = True
        packet = transition(packet, "VERIFIED", actor="reviewer", evidence=["report.json"])
        self.assertEqual(packet["state"], "VERIFIED")
        self.assertFalse(packet["release_approved"])
        self.assertEqual(len(packet["history"]), 5)

    def test_verified_requires_checks_and_evidence(self):
        packet = transition(self.packet(), "ASSIGNED", actor="agent")
        packet = transition(packet, "RECHECK_REQUIRED", actor="agent", reason="Independent recheck required")
        packet = transition(packet, "FIXED_PENDING_REVIEW", actor="ci", evidence=["report.json"])
        with self.assertRaisesRegex(ValueError, "required_checks_complete"):
            transition(packet, "VERIFIED", actor="reviewer", evidence=["report.json"])
        packet["required_checks_complete"] = True
        with self.assertRaisesRegex(ValueError, "non-empty evidence"):
            transition(packet, "VERIFIED", actor="reviewer")

    def test_invalid_skip_and_block_reason_are_rejected(self):
        with self.assertRaisesRegex(ValueError, "invalid transition"):
            transition(self.packet(), "VERIFIED", actor="agent", evidence=["x"])
        with self.assertRaisesRegex(ValueError, "require a reason"):
            transition(self.packet(), "BLOCKED", actor="agent", reason="short")

    def test_reopen_keeps_release_false(self):
        packet = self.packet(state="VERIFIED", required_checks_complete=True)
        packet = transition(packet, "REOPENED", actor="reviewer", reason="Regression reproduced in a later run")
        self.assertEqual(packet["state"], "REOPENED")
        self.assertFalse(packet["release_approved"])


if __name__ == "__main__":
    unittest.main()
