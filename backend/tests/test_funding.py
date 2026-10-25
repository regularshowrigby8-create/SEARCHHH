"""Tests for the funding eligibility matcher.

These encode policy, so they are deliberately explicit: a change to NYDA tiers
must break a test and force a human to re-read the source document.
"""

from __future__ import annotations

from app.models.schemas import FundingMatchRequest
from app.services import funding


def _ids(response) -> list[str]:
    return [m.id for m in response.matches]


def test_young_idea_stage_can_reach_threshold_one_after_training():
    # Training is a prerequisite, so a fresh applicant gets "steps_required",
    # not a flat no. Both states must be visible to the user.
    fresh = funding.match(FundingMatchRequest(age=23, stage="idea"))
    tier_one = next(m for m in fresh.matches if m.id == "nyda-threshold-1")
    assert tier_one.status == "steps_required"
    assert any("Training" in step for step in tier_one.steps)

    trained = funding.match(FundingMatchRequest(age=23, stage="idea", training_completed=True))
    tier_one = next(m for m in trained.matches if m.id == "nyda-threshold-1")
    assert tier_one.status == "eligible"


def test_training_is_a_blocker_until_completed():
    blocked = funding.match(FundingMatchRequest(age=24, stage="startup", registered=True))
    threshold_two = next(m for m in blocked.matches if m.id == "nyda-threshold-2")
    assert threshold_two.eligible is False
    assert threshold_two.status == "steps_required"
    assert any("Training" in b for b in threshold_two.steps)

    unblocked = funding.match(FundingMatchRequest(age=24, stage="startup", registered=True, training_completed=True))
    threshold_two = next(m for m in unblocked.matches if m.id == "nyda-threshold-2")
    assert threshold_two.eligible is True


def test_registration_required_from_startup_tier():
    result = funding.match(FundingMatchRequest(age=30, stage="startup", training_completed=True))
    threshold_two = next(m for m in result.matches if m.id == "nyda-threshold-2")
    assert any("CIPC" in b for b in threshold_two.steps)


def test_over_35_is_ineligible_for_nyda_but_not_sefa():
    result = funding.match(FundingMatchRequest(age=41, stage="growth", registered=True, training_completed=True))
    assert all(not m.eligible for m in result.matches if m.provider == "NYDA")
    sefa = next(m for m in result.matches if m.id == "sefa-loan")
    assert sefa.eligible is True


def test_turnover_cap_blocks_nyda():
    result = funding.match(
        FundingMatchRequest(
            age=28,
            stage="growth",
            registered=True,
            training_completed=True,
            annual_turnover=900_000,
        )
    )
    threshold_three = next(m for m in result.matches if m.id == "nyda-threshold-3")
    assert threshold_three.status == "ineligible"
    assert any("Turnover" in b for b in threshold_three.blockers)


def test_next_actions_list_the_blocking_steps_in_order():
    result = funding.match(FundingMatchRequest(age=22, stage="idea"))
    assert any("CIPC" in action for action in result.next_actions)
    assert any("Training" in action for action in result.next_actions)
    assert any("Threshold 1" in action for action in result.next_actions)


def test_eligible_matches_sort_before_ineligible():
    result = funding.match(FundingMatchRequest(age=25, stage="startup", registered=True, training_completed=True))
    flags = [m.eligible for m in result.matches]
    assert flags == sorted(flags, reverse=True)


def test_ineligible_rules_do_not_offer_steps():
    result = funding.match(FundingMatchRequest(age=24, stage="idea"))
    sefa = next(m for m in result.matches if m.id == "sefa-loan")
    assert sefa.status == "ineligible"
    assert sefa.blockers
    assert sefa.steps == []


def test_rules_carry_a_source_and_review_date():
    assert funding.RULES_REVIEWED
    assert all(rule.source for rule in funding.RULES)
    assert len(funding.RULES) >= 6
