package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import com.google.gson.Gson

data class CodebaseEntry(
    val number: Int,
    val original: Boolean,
    val name: String,
    val submittedUrl: String,
    val submittedLanguage: String,
    val reportedLanguage: String?,
    val category: String,
    val canonicalUrl: String?,
    val sourceUrl: String?,
    val evidenceUrl: String?,
    val repositoryStatus: String,
    val license: String,
    val archived: Boolean?,
    val revision: String?,
    val integration: String,
    val executionTarget: String,
    val description: String,
    val note: String?,
    val configurationSupport: Map<String, String>,
) {
    fun matches(query: String): Boolean =
        query.trim().split(Regex("\\s+")).all { term ->
            "$number $name $submittedLanguage ${reportedLanguage.orEmpty()} $category $repositoryStatus $integration ${note.orEmpty()}"
                .contains(term, ignoreCase = true)
        }

    // Malformed original links remain visible but are never silently launched as guessed URLs.
    fun codeLink(): String? = (sourceUrl ?: canonicalUrl)?.takeIf { LocalPolicy.canonical(it) != null }
}

data class CodebaseRegistry(
    val schemaVersion: Int,
    val auditedAt: String,
    val evidenceScope: String,
    val requestedIgnoreProfile: Map<String, Boolean>,
    val requestedProfileIsRuntimeConfig: Boolean,
    val entries: List<CodebaseEntry>,
) {
    companion object {
        fun load(context: Context): CodebaseRegistry =
            context.assets
                .open("searchhh-codebases.json")
                .bufferedReader()
                .use { Gson().fromJson(it, CodebaseRegistry::class.java) }
                .also {
                    require(it.schemaVersion == 1 && !it.requestedProfileIsRuntimeConfig)
                    require(
                        it.entries
                            .filter { e -> e.original }
                            .map { e -> e.number }
                            .sorted() == (1..100).toList(),
                    )
                    require(
                        it.entries
                            .map { e -> e.number }
                            .distinct()
                            .size == it.entries.size,
                    )
                }
    }
}
