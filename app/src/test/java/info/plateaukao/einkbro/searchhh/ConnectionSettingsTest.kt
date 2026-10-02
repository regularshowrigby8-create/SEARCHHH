package info.plateaukao.einkbro.searchhh

import org.junit.Assert.*
import org.junit.Test

class ConnectionSettingsTest {
    @Test fun backendRequiresHttpsAndNoEmbeddedCredentials() {
        assertTrue(ConnectionSettings.validServerUrl("https://search.example.org/"))
        assertFalse(ConnectionSettings.validServerUrl("http://search.example.org/"))
        assertFalse(ConnectionSettings.validServerUrl("https://user:pass@search.example.org/"))
        assertFalse(ConnectionSettings.validServerUrl("https://search.example.org/?token=secret"))
        assertFalse(ConnectionSettings.validServerUrl("javascript:alert(1)"))
    }
}
