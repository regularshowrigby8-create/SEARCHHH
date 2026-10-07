package info.plateaukao.einkbro.searchhh.testing

/** Stable semantic IDs. A declared ID does not mean the corresponding feature exists. */
object SearchhhTestTags {
    const val HOME = "home_screen"
    const val LIVE_SEARCH = "live_search_screen"
    const val SOURCES = "sources_screen"
    const val SAVED = "saved_screen"
    const val HISTORY = "history_screen"
    const val SETTINGS = "settings_screen"
    const val BROWSER_SCREEN = "browser_screen"
    const val DRAWER_BUTTON = "drawer_button"
    const val SEARCH_FIELD = "search_field"
    const val START_STOP_SEARCH = "start_stop_search_button"
    const val STOP_SEARCH = "stop_search_button"
    const val FILTER_BUTTON = "filter_button"
    const val SOURCE_FILTER = "source_filter"
    const val RESULT_OPEN = "result_open"
    const val RESULT_SAVE = "result_save"
    const val RESULT_DETAILS = "result_details"
    const val RESULTS_WEBVIEW = "results_webview"
    const val SAVED_WEBVIEW = "saved_webview"

    /** Result controls are repeated; append the stable entity ID instead of reusing one tag. */
    fun resultOpen(id: String) = "$RESULT_OPEN:$id"

    fun resultSave(id: String) = "$RESULT_SAVE:$id"

    fun resultDetails(id: String) = "$RESULT_DETAILS:$id"

    fun sourceFilter(id: String) = "$SOURCE_FILTER:$id"

    const val BROWSER_URL = "browser_url"
    const val BROWSER_BACK = "browser_back"
    const val BROWSER_FORWARD = "browser_forward"
    const val BROWSER_RELOAD = "browser_reload"
    const val BROWSER_CLOSE = "browser_close"
    const val MENU_HOME = "menu_home"
    const val MENU_LIVE_SEARCH = "menu_live_search"
    const val MENU_SOURCES = "menu_sources"
    const val MENU_SAVED = "menu_saved"
    const val MENU_HISTORY = "menu_history"
    const val MENU_SETTINGS = "menu_settings"
    const val THEME_DIALOG_CLOSE = "theme_dialog_close"
    const val THEME_SWITCH = "theme_switch"
    const val ERROR_MESSAGE = "error_message"
    const val RETRY_BUTTON = "retry_button"
    const val EMPTY_STATE = "empty_state"
}
