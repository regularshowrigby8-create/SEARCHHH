package info.plateaukao.einkbro.searchhh

import info.plateaukao.einkbro.searchhh.local.LocalPolicy
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress

class LocalPolicyTest {
    @Test fun blocksPrivateAndBotTargets() {
        listOf("127.0.0.1", "10.0.0.1", "169.254.169.254", "100.64.0.1", "198.18.0.1", "192.0.2.1", "203.0.113.1", "2001:db8::1", "2002:7f00:1::1", "::1", "fc00::1").forEach { assertFalse(it, LocalPolicy.publicAddress(InetAddress.getByName(it))) }
        listOf("http://127.0.0.1", "https://evil.bot", "https://evil.bot.", "https://example.onion", "file:///etc/passwd").forEach { assertNull(it, LocalPolicy.canonical(it)) }
    }
    @Test fun parsesSourceDatesWithoutInventingMissingTimes() {
        assertEquals("2024-05-10T08:30:00Z", LocalPolicy.publicationDate("2024-05-10 10:30:00+0200"))
        assertEquals("2024-05-10", LocalPolicy.publicationDate("2024-05-10 10:30:00"))
        assertEquals("2024-05-10", LocalPolicy.publicationDate("2024-05-10"))
        assertNull(LocalPolicy.publicationDate("unknown"))
        assertNull(LocalPolicy.publicationDate("2999-01-01T00:00:00Z"))
    }
    @Test fun relayParsesOnlyForwardEvents() {
        val parser = info.plateaukao.einkbro.searchhh.local.RelayEvent
        assertEquals("https://example.lhr.life", parser.httpsUrl("""{"event":"tcpip-forward","address":"example.lhr.life"}"""))
        assertEquals("https://example.localhost.run", parser.httpsUrl("""{"event":"tcpip-forward","address":"https://example.localhost.run"}"""))
        assertNull(parser.httpsUrl("""{"event":"authn","message":"Visit https://admin.localhost.run"}"""))
        assertNull(parser.httpsUrl("""{"event":"tcpip-forward","address":"evil.example"}"""))
        assertNull(parser.httpsUrl("""{"event":"tcpip-forward","address":"example.lhr.life.evil.example"}"""))
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
