package com.searchhh.app.ui.funding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.model.FundingMatch
import com.searchhh.app.data.model.FundingStage
import com.searchhh.app.ui.components.ErrorState
import com.searchhh.app.ui.components.FullScreenLoading
import kotlin.math.roundToInt

@Composable
fun FundingRoute(
    viewModel: FundingViewModel,
    onOpenUrl: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FundingScreen(
        state = state,
        onAgeChange = viewModel::setAge,
        onStageChange = viewModel::setStage,
        onRegisteredChange = viewModel::setRegistered,
        onTrainingChange = viewModel::setTrainingCompleted,
        onRetry = viewModel::refresh,
        onOpenUrl = onOpenUrl,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundingScreen(
    state: FundingUiState,
    onAgeChange: (Int) -> Unit,
    onStageChange: (FundingStage) -> Unit,
    onRegisteredChange: (Boolean) -> Unit,
    onTrainingChange: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Funding", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${state.eligibleCount} you qualify for today · ${state.rulesReviewedLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        when {
            error != null && state.matches.isEmpty() ->
                ErrorState(error = error, onRetry = onRetry, modifier = Modifier.padding(padding))
            state.isLoading && state.matches.isEmpty() -> FullScreenLoading(label = "Checking your eligibility…")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "inputs") { EligibilityInputs(state, onAgeChange, onStageChange, onRegisteredChange, onTrainingChange) }

                if (state.nextActions.isNotEmpty()) {
                    item(key = "actions") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Your next actions", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                state.nextActions.forEach { action ->
                                    Text("• $action", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                state.eligible.takeIf { it.isNotEmpty() }?.let { eligible ->
                    item(key = "eligible-header") { SectionHeader("You qualify", eligible.size) }
                    items(eligible, key = { it.id }) { match -> FundingCard(match, onOpenUrl) }
                }

                state.needsSteps.takeIf { it.isNotEmpty() }?.let { steps ->
                    item(key = "steps-header") { SectionHeader("Finish these steps first", steps.size) }
                    items(steps, key = { it.id }) { match -> FundingCard(match, onOpenUrl) }
                }

                val blocked = state.matches.filter { !it.isEligible && !it.needsSteps }
                if (blocked.isNotEmpty()) {
                    item(key = "blocked-header") { SectionHeader("Not for you yet", blocked.size) }
                    items(blocked, key = { it.id }) { match -> FundingCard(match, onOpenUrl) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EligibilityInputs(
    state: FundingUiState,
    onAgeChange: (Int) -> Unit,
    onStageChange: (FundingStage) -> Unit,
    onRegisteredChange: (Boolean) -> Unit,
    onTrainingChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Tell us where you are", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Text("Age: ${state.age}", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = state.age.toFloat(),
                onValueChange = { onAgeChange(it.roundToInt()) },
                valueRange = 16f..45f,
                steps = 28,
            )

            Spacer(Modifier.height(4.dp))
            Text("Stage", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FundingStage.entries.take(3).forEach { stage ->
                    FilterChip(
                        selected = state.stage == stage,
                        onClick = { onStageChange(stage) },
                        label = { Text(stage.label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FundingStage.entries.drop(3).forEach { stage ->
                    FilterChip(
                        selected = state.stage == stage,
                        onClick = { onStageChange(stage) },
                        label = { Text(stage.label, style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            ToggleRow("Registered with CIPC", state.registered, onRegisteredChange)
            ToggleRow("NYDA training done", state.trainingCompleted, onTrainingChange)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FundingCard(match: FundingMatch, onOpenUrl: (String) -> Unit) {
    val (icon, tint) = when {
        match.isEligible -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
        match.needsSteps -> Icons.Filled.HourglassEmpty to MaterialTheme.colorScheme.tertiary
        else -> Icons.Filled.Lock to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(match.name, style = MaterialTheme.typography.titleSmall)
                    Text(match.provider, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = match.amountLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            match.steps.forEach { step ->
                Spacer(Modifier.height(4.dp))
                Text("→ $step", style = MaterialTheme.typography.bodySmall)
            }
            match.blockers.take(2).forEach { blocker ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "✕ $blocker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (match.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(match.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (match.url.isNotBlank()) {
                TextButton(onClick = { onOpenUrl(match.url) }) { Text("Open official page") }
            }
            if (match.source.isNotBlank()) {
                Text("Source: ${match.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
