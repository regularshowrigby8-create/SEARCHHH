package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.testing.SearchhhTestTags

/** Only destinations actually implemented by the Searchhh launcher are registered here. */
enum class SearchhhRoute(
    val id: String,
    val label: String,
    val screenTag: String,
    val menuTag: String,
) {
    DISCOVER("discover", "Discover", SearchhhTestTags.HOME, SearchhhTestTags.MENU_HOME),
    SOURCES("sources", "Sources", SearchhhTestTags.SOURCES, SearchhhTestTags.MENU_SOURCES),
    SAVED("saved", "Saved", SearchhhTestTags.SAVED, SearchhhTestTags.MENU_SAVED),
    SETTINGS("settings", "Settings", SearchhhTestTags.SETTINGS, SearchhhTestTags.MENU_SETTINGS),
    ;

    companion object {
        fun fromId(id: String?): SearchhhRoute = entries.firstOrNull { it.id == id } ?: DISCOVER
    }
}
