package com.searchhh.app.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.SortOption
import com.searchhh.app.ui.components.EmptyState
import com.searchhh.app.ui.components.EntryCard
import com.searchhh.app.ui.components.ErrorState
import com.searchhh.app.ui.components.FullScreenLoading
import com.searchhh.app.ui.components.InfoBanner
import com.searchhh.app.ui.components.SortChipRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsRoute(
    viewModel: ResultsViewModel,
    onBack: () -> Unit,
    onOpenEntry: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ResultsScreen(
        state = state,
        onBack = onBack,
        onOpenEntry = onOpenEntry,
        onToggleSave = viewModel::toggleSave,
        onSortChange = viewModel::changeSort,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    state: ResultsUiState,
    onBack: () -> Unit,
    onOpenEntry: (String) -> Unit,
    onToggleSave: (Entry) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.query.ifBlank { state.category?.replaceFirstChar { it.uppercase() } ?: "Results" },
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = buildString {
                                append("${state.total} result")
                                if (state.total != 1) append("s")
                                if (state.tookMs > 0) append(" · ${state.tookMs.toInt()} ms")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SortChipRow(selected = state.sort, onSelect = onSortChange)
            if (state.fromCache) {
                InfoBanner("Offline: showing your last results")
            }
            val error = state.error
            when {
                state.isLoading -> FullScreenLoading()
                error != null -> ErrorState(error = error, onRetry = onRetry)
                state.isEmpty -> EmptyState(
                    title = "Nothing matched",
                    body = "Try a shorter query, or pick another category.",
                    actionLabel = "Try again",
                    onAction = onRetry,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = state.results, key = { it.id }) { entry ->
                        EntryCard(
                            entry = entry,
                            onClick = { onOpenEntry(entry.id) },
                            onToggleSave = { onToggleSave(entry) },
                            isSaved = entry.id in state.savedIds,
                            showScore = state.query.isNotBlank(),
                        )
                    }
                    if (state.hasMore) {
                        item(key = "more") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Spacer(Modifier.weight(1f))
                                if (state.isLoadingMore) {
                                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                                } else {
                                    TextButton(onClick = onLoadMore) { Text("Load more") }
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
