package com.searchhh.app.ui.navigation

import android.net.Uri

/**
 * String routes (Navigation Compose). Centralised so a route change is one edit
 * and every `navigate` call is typo-free.
 */
object Routes {
    const val HOME = "home"
    const val SAVED = "saved"
    const val FUNDING = "funding"
    const val SETTINGS = "settings"

    const val RESULTS = "results?q={q}&category={category}"
    const val DETAIL = "detail/{id}"

    const val ARG_QUERY = "q"
    const val ARG_CATEGORY = "category"
    const val ARG_ID = "id"

    fun results(query: String = "", category: String? = null): String =
        "results?q=${Uri.encode(query)}&category=${Uri.encode(category.orEmpty())}"

    fun detail(id: String): String = "detail/${Uri.encode(id)}"

    val topLevel: Set<String> = setOf(HOME, FUNDING, SAVED, SETTINGS)
}
