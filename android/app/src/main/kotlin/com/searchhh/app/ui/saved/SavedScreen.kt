package com.searchhh.app.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.model.Entry
import com.searchhh.app.ui.components.EmptyState
import com.searchhh.app.ui.components.EntryCard
import com.searchhh.app.ui.components.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedRoute(
    viewModel: SavedViewModel,
    onOpenEntry: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SavedScreen(
        state = state,
        onOpenEntry = onOpenEntry,
        onRemove = viewModel::remove,
        onClearAll = viewModel::clearAll,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    state: SavedUiState,
    onOpenEntry: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved · ${state.entries.size}") },
                actions = {
                    if (state.entries.isNotEmpty()) {
                        TextButton(onClick = onClearAll) { Text("Clear") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        when {
            state.isLoading -> FullScreenLoading(label = "Opening your library…")
            state.entries.isEmpty() -> EmptyState(
                icon = Icons.Filled.BookmarkBorder,
                title = "No saved items yet",
                body = "Tap the bookmark on any result to keep it here. Saved items open with no network.",
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.entries, key = Entry::id) { entry ->
                    EntryCard(
                        entry = entry,
                        onClick = { onOpenEntry(entry.id) },
                        onToggleSave = { onRemove(entry.id) },
                        isSaved = true,
                    )
                }
            }
        }
    }
}
