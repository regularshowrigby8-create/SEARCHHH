package info.plateaukao.einkbro.searchhh

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import info.plateaukao.einkbro.searchhh.ai.AiVault
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AiVaultTest {
    @Test fun consentAndRemovalAreRequiredAndNoPlaintextStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vault = AiVault(context)
        val key = "test-only-not-a-real-provider-secret-123456789"
        try {
            assertTrue(runCatching { vault.save("zai", key, "glm-4.5-flash", false) }.isFailure)
            assertTrue(runCatching { vault.save("zai", key, "glm-4.5", true) }.isFailure)
            val before = vault.revision()
            vault.save("zai", key, "glm-4.5-flash", true)
            assertTrue(vault.hasKey("zai"))
            assertNotEquals(before, vault.revision())
            assertEquals("glm-4.5-flash", AiVault(context).model("zai"))
            // Android EncryptedSharedPreferences commits data asynchronously; either
            // version on disk must never expose the secret as plaintext.
            val files =
                java.io
                    .File(context.applicationInfo.dataDir, "shared_prefs")
                    .listFiles()
                    .orEmpty()
            assertTrue(files.all { !it.readText().contains(key) })
            vault.remove("zai")
            assertFalse(AiVault(context).hasKey("zai"))
        } finally {
            vault.remove("zai")
            vault.setEnabled(false)
        }
    }
}
