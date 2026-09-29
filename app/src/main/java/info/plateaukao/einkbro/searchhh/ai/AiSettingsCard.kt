package info.plateaukao.einkbro.searchhh.ai

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Keys stay in a non-saveable draft until explicit, provider-specific consent. */
@Composable
fun AiSettingsCard(ai: AiReviewer) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(ai.vault.enabled()) }
    var active by remember { mutableStateOf<AiPolicy.Provider?>(null) }
    var revision by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    fun external(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { message = "No external browser available for provider authorization" }
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("External AI reviewers", style = MaterialTheme.typography.h6)
            Text("Crawlers find links. AI only reviews the query and collected public excerpts. No model is hosted on your phone; no paid model fallback.")
            Row {
                Switch(enabled, { enabled = it; ai.vault.setEnabled(it); ai.settingsChanged() })
                Text("Enable evidence review", Modifier.padding(start = 8.dp, top = 12.dp))
            }
            Text("Free API plans have quotas and may change. Keys normally remain valid across quota resets. An open-source license is not a free hosting plan.", style = MaterialTheme.typography.caption)
            AiPolicy.providers.forEach { provider ->
                val connected = remember(revision) { ai.vault.hasKey(provider.id) }
                Text(provider.name)
                Text(if (connected) "Connected · ${ai.vault.model(provider.id)}" else "Not connected", style = MaterialTheme.typography.caption)
                Row {
                    TextButton(onClick = { active = provider }) { Text(if (connected) "Replace connection" else "Connect") }
                    if (connected) TextButton(onClick = {
                        ai.vault.remove(provider.id); ai.settingsChanged(); revision++
                        message = "Key removed from this device. Revoke it on the provider dashboard if needed."
                    }) { Text("Remove") }
                }
            }
            if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.caption)
        }
    }
    active?.let { provider ->
        key(provider.id) {
            var secret by remember { mutableStateOf("") }
            var consent by remember { mutableStateOf(false) }
            var model by remember { mutableStateOf(ai.vault.model(provider.id)) }
            var models by remember { mutableStateOf(if (provider.id == "zai") AiPolicy.zaiModels else ai.freeModels) }
            var expanded by remember { mutableStateOf(false) }
            var loading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf("") }
            AlertDialog(onDismissRequest = { active = null }, title = { Text("Connect ${provider.name}") }, text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Sign in/create your account on the official website and create your own API key. Searchhh cannot bypass email verification or issue arbitrary provider keys.")
                    TextButton(onClick = { external(provider.keys) }) { Text("Open official API-key dashboard ↗") }
                    Row {
                        TextButton(onClick = { external(provider.pricing) }) { Text("Free-plan terms ↗") }
                        TextButton(onClick = { external(provider.privacy) }) { Text("Privacy ↗") }
                    }
                    Text("Your query and up to four public result excerpts per request will be sent to this provider, and OpenRouter may forward them to its model provider. No profile, browser cookies, or other providers' keys are sent. Provider retention policies apply.")
                    if (provider.id == "openrouter") TextButton(enabled = !loading, onClick = {
                        loading = true
                        scope.launch {
                            try { models = ai.refreshCatalog(); error = "${models.size} zero-priced text variants listed (not an inference test)" }
                            catch (e: CancellationException) { throw e }
                            catch (_: Exception) { error = "Catalog unavailable. Try again later; no paid catalog substituted." }
                            finally { loading = false }
                        }
                    }) { Text(if (loading) "Loading free catalog…" else "Load current free-model choices") }
                    if (provider.id == "zai") Text("Three Flash models listed free in Z.AI pricing on 2026-09-29. No web-search tool or paid FlashX models are used.", style = MaterialTheme.typography.caption)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, enabled = models.isNotEmpty()) { Text(model.ifBlank { "Choose a free model" }) }
                        DropdownMenu(expanded, { expanded = false }) {
                            models.forEach { choice -> DropdownMenuItem(onClick = { model = choice; expanded = false }) { Text(choice) } }
                        }
                    }
                    OutlinedTextField(secret, { secret = it.trim() }, label = { Text("Provider API key") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                    Row { Checkbox(consent, { consent = it }); Text("I authorize this provider to receive my search query and public evidence under its free-plan and privacy terms.") }
                    if (error.isNotBlank()) Text(error, style = MaterialTheme.typography.caption)
                }
            }, confirmButton = {
                TextButton(enabled = consent && secret.length >= 12 && model in models, onClick = {
                    try {
                        ai.vault.save(provider.id, secret, model, consent); ai.settingsChanged(); revision++
                        secret = ""; active = null; message = "Connection saved encrypted. Enable review to use it; key not yet tested."
                    } catch (_: Exception) { error = "Check the API key format, model and consent." }
                }) { Text("Store encrypted key") }
            }, dismissButton = { TextButton(onClick = { secret = ""; active = null }) { Text("Cancel") } })
        }
    }
}
