package info.plateaukao.einkbro.searchhh

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KtorAndroidCompatibilityTest {
    @Test
    fun absentDesktopManagementApisLeaveKtorDebuggerDetectionDisabled() {
        // If Android adds these APIs, review/remove the matching compatibility rules.
        assertThrows(ClassNotFoundException::class.java) {
            Class.forName("java.lang.management.ManagementFactory")
        }
        assertThrows(ClassNotFoundException::class.java) {
            Class.forName("java.lang.management.RuntimeMXBean")
        }

        // Exercise the actual dependency implementation, not a local replacement.
        // Reflection is necessary because Ktor's Kotlin declaration is internal.
        val detector = Class.forName("io.ktor.util.debug.IntellijIdeaDebugDetector")
        val instance = detector.getField("INSTANCE").get(null)
        assertFalse(detector.getMethod("isDebuggerConnected").invoke(instance) as Boolean)
    }
}
