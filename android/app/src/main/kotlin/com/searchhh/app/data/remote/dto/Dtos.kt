package com.searchhh.app.data.remote.dto

import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.HealthInfo
import com.searchhh.app.data.model.Insight
import com.searchhh.app.data.model.SearchPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire models - they mirror `backend/app/models/schemas.py` exactly.
 * Nothing in the UI layer imports from this package: DTOs are mapped to domain
 * models in [toDomain] so a backend rename never leaks into Compose code.
 */

@Serializable
data class EntryDto(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val category: String = "general",
    val tags: List<String> = emptyList(),
    val price: Double? = null,
    val currency: String = "ZAR",
    val rating: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val location: String? = null,
    val url: String? = null,
    val score: Double = 0.0,
    val highlights: List<String> = emptyList(),
)

@Serializable
data class SearchResponseDto(
    val query: String = "",
    val total: Int = 0,
    @SerialName("took_ms") val tookMs: Double = 0.0,
    val source: String = "memory",
    val results: List<EntryDto> = emptyList(),
    val suggestions: List<String> = emptyList(),
)

@Serializable
data class SuggestResponseDto(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
)

@Serializable
data class CategoryDto(val name: String, val count: Int)

@Serializable
data class CategoriesResponseDto(
    val count: Int = 0,
    val categories: List<CategoryDto> = emptyList(),
)

@Serializable
data class InsightRequestDto(
    @SerialName("entry_id") val entryId: String,
    val audience: String = "first-time buyer",
)

@Serializable
data class InsightResponseDto(
    @SerialName("entry_id") val entryId: String,
    val insight: String,
    val provider: String = "heuristic",
    val cached: Boolean = false,
)

@Serializable
data class FeedbackRequestDto(
    val message: String,
    val rating: Int = 5,
    @SerialName("entry_id") val entryId: String? = null,
    val contact: String? = null,
    val platform: String = "android",
    @SerialName("app_version") val appVersion: String? = null,
)

@Serializable
data class FeedbackResponseDto(
    val accepted: Boolean,
    val id: String,
    @SerialName("stored_in") val storedIn: String = "memory",
)

@Serializable
data class FundingMatchRequestDto(
    val age: Int = 24,
    val stage: String = "idea",
    val registered: Boolean = false,
    @SerialName("training_completed") val trainingCompleted: Boolean = false,
    @SerialName("annual_turnover") val annualTurnover: Double = 0.0,
    @SerialName("prior_funding") val priorFunding: Double = 0.0,
)

@Serializable
data class FundingMatchDto(
    val id: String,
    val name: String,
    val provider: String,
    @SerialName("min_amount") val minAmount: Double = 0.0,
    @SerialName("max_amount") val maxAmount: Double = 0.0,
    val eligible: Boolean = false,
    val status: String = "ineligible",
    val blockers: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val url: String = "",
    val source: String = "",
    val notes: String = "",
)

@Serializable
data class FundingMatchResponseDto(
    @SerialName("eligible_count") val eligibleCount: Int = 0,
    val matches: List<FundingMatchDto> = emptyList(),
    @SerialName("next_actions") val nextActions: List<String> = emptyList(),
    @SerialName("rules_reviewed") val rulesReviewed: String = "",
)

@Serializable
data class HealthDto(
    val status: String = "ok",
    val service: String = "",
    val version: String = "",
    val env: String = "",
    @SerialName("git_sha") val gitSha: String = "",
    @SerialName("uptime_s") val uptimeSeconds: Double = 0.0,
    val checks: Map<String, String> = emptyMap(),
)

@Serializable
data class TokenRequestDto(
    val subject: String = "android-demo",
    val scopes: List<String> = listOf("read"),
)

@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    @SerialName("expires_in") val expiresIn: Int = 0,
    val scopes: List<String> = emptyList(),
)

// ---------------------------------------------------------------------------
// DTO -> domain mappers
// ---------------------------------------------------------------------------

fun EntryDto.toDomain(): Entry = Entry(
    id = id,
    title = title,
    subtitle = subtitle,
    description = description,
    category = category,
    tags = tags,
    price = price,
    currency = currency,
    rating = rating,
    imageUrl = imageUrl,
    location = location,
    url = url,
    score = score,
    highlights = highlights,
)

fun SearchResponseDto.toDomain(): SearchPage = SearchPage(
    query = query,
    total = total,
    tookMs = tookMs,
    source = source,
    results = results.map { it.toDomain() },
    suggestions = suggestions,
    fromCache = false,
)

fun CategoriesResponseDto.toDomain(): List<CategoryCount> =
    categories.map { CategoryCount(name = it.name, count = it.count) }

fun InsightResponseDto.toDomain(): Insight = Insight(
    entryId = entryId,
    text = insight,
    provider = provider,
    cached = cached,
)

fun FundingMatchDto.toDomain(): com.searchhh.app.data.model.FundingMatch =
    com.searchhh.app.data.model.FundingMatch(
        id = id,
        name = name,
        provider = provider,
        minAmount = minAmount,
        maxAmount = maxAmount,
        status = status,
        blockers = blockers,
        steps = steps,
        url = url,
        source = source,
        notes = notes,
    )

fun FundingMatchResponseDto.toDomain(): com.searchhh.app.data.model.FundingResult =
    com.searchhh.app.data.model.FundingResult(
        eligibleCount = eligibleCount,
        matches = matches.map { it.toDomain() },
        nextActions = nextActions,
        rulesReviewed = rulesReviewed,
    )

fun HealthDto.toDomain(): HealthInfo = HealthInfo(
    status = status,
    service = service,
    version = version,
    environment = env,
    gitSha = gitSha,
    uptimeSeconds = uptimeSeconds,
    checks = checks,
)
