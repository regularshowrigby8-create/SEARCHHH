package info.plateaukao.einkbro.searchhh

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** One source of truth for the four destinations that currently have real screens. */
data class SearchhhMenuItem(
    val route: SearchhhRoute,
    val icon: ImageVector,
    val showInBottomBar: Boolean = true,
)

val searchhhMenuRegistry =
    listOf(
        SearchhhMenuItem(SearchhhRoute.DISCOVER, Icons.Outlined.Search),
        SearchhhMenuItem(SearchhhRoute.SOURCES, Icons.Outlined.Hub),
        SearchhhMenuItem(SearchhhRoute.SAVED, Icons.Outlined.BookmarkBorder),
        SearchhhMenuItem(SearchhhRoute.SETTINGS, Icons.Outlined.Settings),
    )
