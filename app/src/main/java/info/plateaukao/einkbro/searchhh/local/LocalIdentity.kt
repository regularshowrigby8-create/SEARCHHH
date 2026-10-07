package info.plateaukao.einkbro.searchhh.local

import android.content.Context
import info.plateaukao.einkbro.searchhh.ConnectionSettings
import java.security.SecureRandom
import java.util.UUID

/** Installation-scoped identity. Display names are never used as credentials or IDs. */
data class LocalIdentity(
    val id: String,
    val name: String,
    val token: String,
) {
    companion object {
        fun randomToken(): String = ByteArray(32).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

        @Synchronized fun create(
            context: Context,
            name: String,
        ): LocalIdentity {
            require(name.trim().length in 1..60) { "Enter a name (1–60 characters)" }
            val p = ConnectionSettings.prefs(context)
            val id = p.getString("installation_id", null) ?: UUID.randomUUID().toString()
            val token = p.getString("mcp_token", null) ?: randomToken()
            p
                .edit()
                .putString(
                    "installation_id",
                    id,
                ).putString("display_name", name.trim())
                .putString("mcp_token", token)
                .putString("backend_mode", "internal")
                .apply()
            return LocalIdentity(id, name.trim(), token)
        }

        fun load(context: Context): LocalIdentity? {
            val p = ConnectionSettings.prefs(context)
            return LocalIdentity(
                p.getString("installation_id", null) ?: return null,
                p.getString("display_name", null) ?: return null,
                p.getString("mcp_token", null) ?: return null,
            )
        }

        fun rotate(context: Context) {
            ConnectionSettings
                .prefs(context)
                .edit()
                .putString("mcp_token", randomToken())
                .apply()
        }
    }
}
