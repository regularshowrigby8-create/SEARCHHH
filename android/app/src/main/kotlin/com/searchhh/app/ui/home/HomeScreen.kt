package com.searchhh.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.searchhh.app.data.model.Entry
import com.searchhh.app.ui.components.CategoryChipRow
import com.searchhh.app.ui.components.EntryCard
import com.searchhh.app.ui.components.ErrorState
import com.searchhh.app.ui.components.QueryChipRow
import com.searchhh.app.ui.components.SearchField
import com.searchhh.app.ui.components.TagPill
import com.searchhh.app.ui.theme.Lime400

/**
 * Home = search entry point + discovery feed.
 * Stateless by design: everything comes from [HomeUiState].
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onSearch: (String, String?) -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = { query ->
            viewModel.onQuerySubmitted(query)
            onSearch(query, null)
        },
        onRefresh = viewModel::loadDiscovery,
        onOpenEntry = onOpenEntry,
        onToggleSave = viewModel::toggleSave,
        onCategoryClick = { category -> onSearch("", category) },
        onQueryChipClick = { query ->
            viewModel.onQuerySubmitted(query)
            onSearch(query, null)
        },
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onToggleSave: (Entry) -> Unit,
    onCategoryClick: (String) -> Unit,
    onQueryChipClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Find it in seconds",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Search 30+ campus services, offline-ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }

                Spacer(Modifier.height(16.dp))
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    onSubmit = { onSubmit(state.query) },
                )
                Spacer(Modifier.height(8.dp))
                BackendBadge(state = state)
            }
        }

        if (state.suggestions.isNotEmpty()) {
            item(key = "suggestions") {
                Column {
                    SectionLabel("Suggestions")
                    QueryChipRow(queries = state.suggestions, onSelect = onQueryChipClick)
                }
            }
        }

        if (state.recent.isNotEmpty()) {
            item(key = "recent") {
                Column {
                    SectionLabel("Recent searches")
                    QueryChipRow(queries = state.recent, onSelect = onQueryChipClick)
                }
            }
        }

        if (state.categories.isNotEmpty()) {
            item(key = "categories") {
                Column {
                    SectionLabel("Browse by category")
                    CategoryChipRow(
                        categories = state.categories,
                        selected = null,
                        onSelect = { category -> if (category != null) onCategoryClick(category) },
                    )
                }
            }
        }

        item(key = "divider") {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        item(key = "trending-header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Lime400,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(6.dp))
                Text("Trending now", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (state.tookMs > 0) "${state.tookMs.toInt()} ms" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val currentError = state.error
        if (currentError != null && state.trending.isEmpty()) {
            item(key = "error") {
                ErrorState(error = currentError, onRetry = onRefresh)
            }
        }

        items(items = state.trending, key = { it.id }) { entry ->
            EntryCard(
                entry = entry,
                onClick = { onOpenEntry(entry.id) },
                onToggleSave = { onToggleSave(entry) },
                isSaved = entry.id in state.savedIds,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (state.trending.isEmpty() && state.isLoading) {
            item(key = "loading") {
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun BackendBadge(state: HomeUiState) {
    val backend = state.backend
    val label = when {
        backend == null -> "Backend: checking…"
        backend.isHealthy -> "Live · ${backend.service} v${backend.version} · ${backend.environment}"
        else -> "Backend degraded"
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            if (state.showingCache) {
                AssistChip(onClick = {}, label = { Text("cached") })
            } else if (state.savedIds.isNotEmpty()) {
                TagPill("${state.savedIds.size} saved")
            }
        }
    }
}
