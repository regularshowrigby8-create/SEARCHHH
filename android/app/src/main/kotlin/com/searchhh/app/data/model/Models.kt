package com.searchhh.app.data.model

/**
 * Domain models - the only shapes Compose ever renders.
 * Intentionally plain data classes: no annotations, no Android types, so they
 * are usable from plain JVM unit tests.
 */

data class Entry(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val category: String = "general",
    val tags: List<String> = emptyList(),
    val price: Double? = null,
    val currency: String = "ZAR",
    val rating: Double = 0.0,
    val imageUrl: String? = null,
    val location: String? = null,
    val url: String? = null,
    val score: Double = 0.0,
    val highlights: List<String> = emptyList(),
) {
    val hasPrice: Boolean get() = price != null && price > 0.0

    /** "R 249" / "Free" / "—" - the client prints ZAR prices with a space. */
    val priceLabel: String
        get() = when {
            price == null -> "—"
            price <= 0.0 -> "Free"
            else -> "$currency ${price.toInt()}"
        }
}

data class SearchPage(
    val query: String = "",
    val total: Int = 0,
    val tookMs: Double = 0.0,
    val source: String = "memory",
    val results: List<Entry> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val fromCache: Boolean = false,
) {
    val isEmpty: Boolean get() = results.isEmpty()
}

data class Insight(
    val entryId: String,
    val text: String,
    val provider: String = "heuristic",
    val cached: Boolean = false,
)

data class CategoryCount(val name: String, val count: Int)

data class HealthInfo(
    val status: String = "unknown",
    val service: String = "",
    val version: String = "",
    val environment: String = "",
    val gitSha: String = "",
    val uptimeSeconds: Double = 0.0,
    val checks: Map<String, String> = emptyMap(),
) {
    val isHealthy: Boolean get() = status == "ok"
}

/** Sort options exposed as chips in the results screen. */
enum class SortOption(val apiValue: String, val label: String) {
    RELEVANCE("relevance", "Best match"),
    RATING("rating", "Top rated"),
    PRICE_ASC("price_asc", "Cheapest"),
    PRICE_DESC("price_desc", "Priciest"),
    RECENT("recent", "Newest"),
}

/** Funding eligibility, mirroring backend/app/services/funding.py. */
enum class FundingStage(val apiValue: String, val label: String) {
    IDEA("idea", "Idea"),
    SURVIVALIST("survivalist", "Selling already"),
    STARTUP("startup", "Registered"),
    GROWTH("growth", "Growing"),
    EXPANSION("expansion", "Expanding"),
}

data class FundingMatch(
    val id: String,
    val name: String,
    val provider: String,
    val minAmount: Double,
    val maxAmount: Double,
    val status: String,
    val blockers: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val url: String = "",
    val source: String = "",
    val notes: String = "",
) {
    val isEligible: Boolean get() = status == "eligible"
    val needsSteps: Boolean get() = status == "steps_required"

    val amountLabel: String
        get() = if (minAmount == maxAmount) "R${maxAmount.toInt()}" else "R${minAmount.toInt()} – R${maxAmount.toInt()}"
}

data class FundingResult(
    val eligibleCount: Int = 0,
    val matches: List<FundingMatch> = emptyList(),
    val nextActions: List<String> = emptyList(),
    val rulesReviewed: String = "",
)
