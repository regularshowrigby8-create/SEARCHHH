package com.searchhh.app.ui.detail

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.Insight
import com.searchhh.app.ui.components.ErrorState
import com.searchhh.app.ui.components.FullScreenLoading
import com.searchhh.app.ui.components.RatingPill
import com.searchhh.app.ui.components.TagPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRoute(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    val entry = state.entry
    val shareText = entry?.let { "${it.title} — ${it.subtitle} (via SEARCHHH)" }.orEmpty()

    DetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onToggleSave = viewModel::toggleSave,
        onRetryInsight = viewModel::loadInsight,
        onRetry = viewModel::load,
        onShare = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share"))
        },
        onOpenLink = { url ->
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
        },
        onFeedbackClick = { showFeedbackDialog = true },
    )

    if (showFeedbackDialog) {
        FeedbackDialog(
            isSending = state.isSendingFeedback,
            onDismiss = { showFeedbackDialog = false },
            onSend = { message, rating ->
                viewModel.sendFeedback(message, rating)
                showFeedbackDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onRetryInsight: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onOpenLink: (String) -> Unit,
    onFeedbackClick: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.entry?.category?.replaceFirstChar { it.uppercase() } ?: "Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            imageVector = if (state.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (state.isSaved) "Remove bookmark" else "Save",
                            tint = if (state.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val error = state.error
        val entry = state.entry
        when {
            state.isLoading && entry == null -> FullScreenLoading(label = "Loading entry…")
            error != null && entry == null -> ErrorState(error = error, onRetry = onRetry)
            entry != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (entry.subtitle.isNotBlank()) {
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.priceLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(12.dp))
                    RatingPill(entry.rating)
                }
                if (!entry.location.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = entry.location.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                InsightCard(
                    insight = state.insight,
                    isLoading = state.isInsightLoading,
                    error = state.insightError,
                    onRetry = onRetryInsight,
                )

                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyLarge,
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.forEach { TagPill(it) }
                }

                if (!entry.url.isNullOrBlank()) {
                    Button(
                        onClick = { onOpenLink(entry.url.orEmpty()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open website")
                    }
                }

                TextButton(onClick = onFeedbackClick) {
                    Text("Give feedback on this result")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InsightCard(
    insight: Insight?,
    isLoading: Boolean,
    error: com.searchhh.app.core.AppError?,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AI insight",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.weight(1f))
                insight?.let {
                    Text(
                        text = it.provider,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                isLoading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Thinking…", style = MaterialTheme.typography.bodyMedium)
                }
                insight != null -> Text(
                    text = insight.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                error != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Insight unavailable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    TextButton(onClick = onRetry) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
private fun FeedbackDialog(
    isSending: Boolean,
    onDismiss: () -> Unit,
    onSend: (String, Int) -> Unit,
) {
    var message by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send feedback") },
        text = {
            Column {
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("What should we improve?") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rating", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    (1..5).forEach { star ->
                        TextButton(onClick = { rating = star }) {
                            Text(if (star <= rating) "★" else "☆")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSend(message, rating) },
                enabled = message.isNotBlank() && !isSending,
            ) { Text(if (isSending) "Sending…" else "Send") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
