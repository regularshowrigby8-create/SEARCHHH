package com.searchhh.app.ui.settings

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.searchhh.app.data.model.HealthInfo

@Composable
fun SettingsRoute(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    SettingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onDarkMode = viewModel::setDarkMode,
        onTelemetry = viewModel::setTelemetry,
        onNotifications = viewModel::setNotifications,
        onApiBaseUrl = viewModel::setApiBaseUrl,
        onRefreshBackend = viewModel::refreshBackend,
        onClearCaches = viewModel::clearCaches,
        onClearSaved = viewModel::clearSaved,
        onTestCrash = viewModel::sendTestCrash,
        onLoadPushToken = viewModel::loadPushToken,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onDarkMode: (Boolean) -> Unit,
    onTelemetry: (Boolean) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onApiBaseUrl: (String) -> Unit,
    onRefreshBackend: () -> Unit,
    onClearCaches: () -> Unit,
    onClearSaved: () -> Unit,
    onTestCrash: () -> Unit,
    onLoadPushToken: () -> Unit,
) {
    var showUrlDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "backend") {
                BackendCard(
                    backend = state.backend,
                    error = state.backendError,
                    isChecking = state.isCheckingBackend,
                    baseUrl = state.effectiveBaseUrl,
                    onRefresh = onRefreshBackend,
                    onEditUrl = { showUrlDialog = true },
                )
            }

            item(key = "appearance") {
                SettingsCard(title = "Appearance") {
                    SwitchRow(
                        title = "Dark mode",
                        subtitle = "Overrides the system setting",
                        checked = state.settings.darkMode == true,
                        onCheckedChange = onDarkMode,
                    )
                }
            }

            item(key = "privacy") {
                SettingsCard(title = "Privacy & telemetry") {
                    SwitchRow(
                        title = "Share crash reports",
                        subtitle = "Sentry + Crashlytics, no personal data (POPIA-friendly)",
                        checked = state.settings.telemetryEnabled,
                        onCheckedChange = onTelemetry,
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    SwitchRow(
                        title = "Notifications",
                        subtitle = "Saved-search alerts and price drops",
                        checked = state.settings.notificationsEnabled,
                        onCheckedChange = onNotifications,
                    )
                }
            }

            item(key = "data") {
                SettingsCard(title = "Local data") {
                    Text(
                        text = "${state.savedCount} saved item(s) stored in Room",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onClearCaches) { Text("Clear cache") }
                        OutlinedButton(onClick = onClearSaved) { Text("Clear saved") }
                    }
                }
            }

            item(key = "diagnostics") {
                SettingsCard(title = "Diagnostics") {
                    Text(
                        text = "Push token",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = state.pushToken ?: "Tap to fetch from Firebase",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onLoadPushToken) { Text("Fetch token") }
                        OutlinedButton(onClick = onTestCrash) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.width(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Test error")
                        }
                    }
                }
            }

            item(key = "about") {
                SettingsCard(title = "About") {
                    LabeledValue("Version", state.appVersion)
                    LabeledValue("Git SHA", state.gitSha)
                    LabeledValue("API", state.effectiveBaseUrl)
                    LabeledValue("Firebase", if (com.searchhh.app.BuildConfig.FIREBASE_CONFIGURED) "configured" else "not configured")
                    LabeledValue("Environment", if (com.searchhh.app.BuildConfig.DEBUG) "debug" else "release")
                }
            }
        }
    }

    if (showUrlDialog) {
        var draft by remember { mutableStateOf(state.effectiveBaseUrl) }
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("API base URL") },
            text = {
                Column {
                    Text(
                        "Point the app at your backend (emulator loopback is 10.0.2.2).",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        singleLine = true,
                        label = { Text("http://host:8000/") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onApiBaseUrl(draft)
                    showUrlDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun BackendCard(
    backend: HealthInfo?,
    error: String?,
    isChecking: Boolean,
    baseUrl: String,
    onRefresh: () -> Unit,
    onEditUrl: () -> Unit,
) {
    val healthy = backend?.isHealthy == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (healthy) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (healthy) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when {
                        isChecking -> "Checking backend…"
                        healthy -> "Backend healthy"
                        else -> "Backend unreachable"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Recheck")
                }
            }
            Text(baseUrl, style = MaterialTheme.typography.bodySmall)
            backend?.let {
                Text(
                    text = "${it.service} v${it.version} · ${it.environment} · uptime ${it.uptimeSeconds.toInt()}s",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (it.checks.isNotEmpty()) {
                    Text(
                        text = it.checks.entries.joinToString(" · ") { (k, v) -> "$k: $v" },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (error != null) {
                Text(
                    text = "Last error: $error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onEditUrl) { Text("Change URL") }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp),
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}
