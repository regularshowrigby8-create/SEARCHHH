package info.plateaukao.einkbro.searchhh.local

/**
 * A bounded capability exposed by Searchhh, not an arbitrary executable from a
 * third-party repository. The codebase catalogue points to the upstream project;
 * these four profiles are the reviewed Android-native implementations that can
 * actually run inside the app today.
 */
data class CrawlerAdapter(
    val id: String,
    val entryNumber: Int,
    val upstreamName: String,
    val label: String,
    val description: String,
    val implementation: String,
)

object CrawlerAdapters {
    val PHONE_SCHEDULER =
        CrawlerAdapter(
            id = "phone-scheduler",
            entryNumber = 1,
            upstreamName = "Scrapy",
            label = "Bounded scheduler",
            description = "Runs one public portal batch at a time with robots, cooldowns, same-host limits and cancellation.",
            implementation = "Android PortalCrawler + OkHttp",
        )
    val PHONE_ARTICLE =
        CrawlerAdapter(
            id = "phone-article",
            entryNumber = 65,
            upstreamName = "Trafilatura",
            label = "Article extraction",
            description = "Extracts readable evidence from the selected portal page without running page scripts.",
            implementation = "Android Jsoup main/article extraction",
        )
    val PHONE_STRUCTURED =
        CrawlerAdapter(
            id = "phone-structured",
            entryNumber = 66,
            upstreamName = "Extruct",
            label = "Structured data",
            description = "Reads bounded JSON-LD opportunity records and keeps the page as evidence.",
            implementation = "Android StructuredDiscovery JSON-LD parser",
        )
    val PHONE_SELECTOR =
        CrawlerAdapter(
            id = "phone-selector",
            entryNumber = 67,
            upstreamName = "Parsel",
            label = "Selector extraction",
            description = "Extracts matching links and surrounding context with scoped CSS selectors.",
            implementation = "Android PortalParser CSS selectors",
        )

    val available = listOf(PHONE_SCHEDULER, PHONE_STRUCTURED, PHONE_ARTICLE, PHONE_SELECTOR)
    val default = PHONE_SCHEDULER

    fun byId(id: String?): CrawlerAdapter? = available.firstOrNull { it.id == id }

    fun forEntry(entry: CodebaseEntry): CrawlerAdapter? =
        available.firstOrNull { it.entryNumber == entry.number }
}
