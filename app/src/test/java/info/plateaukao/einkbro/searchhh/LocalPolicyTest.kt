package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.local.LocalPolicy
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress

class LocalPolicyTest {
    @Test fun blocksPrivateAndBotTargets() {
        listOf("127.0.0.1", "10.0.0.1", "169.254.169.254", "100.64.0.1", "::1", "fc00::1").forEach { assertFalse(it, LocalPolicy.publicAddress(InetAddress.getByName(it))) }
        listOf("http://127.0.0.1", "https://evil.bot", "https://example.onion", "file:///etc/passwd").forEach { assertNull(it, LocalPolicy.canonical(it)) }
    }
    @Test fun preservesFormIdentityAndFiltersNoise() {
        assertEquals("https://forms.gle/abc", LocalPolicy.canonical("https://forms.gle/abc?utm_source=x#foo"))
        assertNotEquals(LocalPolicy.canonical("https://forms.office.com/r/a?id=1"), LocalPolicy.canonical("https://forms.office.com/r/a?id=2"))
        assertNull(LocalPolicy.result("https://example.org/", "Ordinary page", "", listOf("test"), "opportunities"))
        val form = LocalPolicy.result("https://forms.gle/abc", "Apply", "", listOf("test"), "opportunities")!!
        assertEquals("Application form", form.kind); assertNull(form.published); assertFalse(form.verified)
        assertNull(LocalPolicy.result("https://example.org/", "Captcha", "", listOf("test"), "links"))
    }
}
