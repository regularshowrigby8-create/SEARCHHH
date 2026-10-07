package info.plateaukao.einkbro.searchhh.ai

import android.content.Context
import info.plateaukao.einkbro.searchhh.ConnectionSettings
import java.util.UUID

/** Reuses the app's Keystore-backed encrypted preferences; no keys in Room/MCP. */
class AiVault(
    context: Context,
) {
    private val prefs = ConnectionSettings.prefs(context.applicationContext)

    fun revision(): String = prefs.getString("ai_revision", "").orEmpty()

    fun hasKey(provider: String) = !key(provider).isNullOrBlank()

    internal fun key(provider: String): String? = prefs.getString("ai_key_$provider", null)

    fun model(provider: String): String = prefs.getString("ai_model_$provider", "").orEmpty()

    fun enabled() = prefs.getBoolean("ai_enabled", false)

    fun setEnabled(enabled: Boolean) {
        prefs
            .edit()
            .putBoolean("ai_enabled", enabled)
            .putString("ai_revision", UUID.randomUUID().toString())
            .apply()
    }

    fun save(
        provider: String,
        key: String,
        model: String,
        consent: Boolean,
    ) {
        AiPolicy.provider(provider)
        require(consent) { "Provider-specific evidence-sharing consent is required" }
        require(key.length in 12..512 && key.none { it.isWhitespace() || it.code < 32 }) { "Invalid API key format" }
        require(
            if (provider ==
                "zai"
            ) {
                model in AiPolicy.zaiModels
            } else {
                model.endsWith(":free")
            },
        ) { "Only free model entries may be selected" }
        prefs
            .edit()
            .putString("ai_key_$provider", key)
            .putString("ai_model_$provider", model)
            .putString("ai_revision", UUID.randomUUID().toString())
            .apply()
    }

    fun remove(provider: String) {
        prefs
            .edit()
            .remove("ai_key_$provider")
            .remove("ai_model_$provider")
            .putString("ai_revision", UUID.randomUUID().toString())
            .apply()
    }
}
