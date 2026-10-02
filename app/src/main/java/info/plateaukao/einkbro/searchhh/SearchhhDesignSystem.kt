package info.plateaukao.einkbro.searchhh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Shared Searchhh visual tokens. Keep product colours out of individual screens. */
object SearchhhDesignTokens {
    val accent = Color(0xFFC5F178)
    val darkBackground = Color(0xFF101412)
    val darkSurface = Color(0xFF1A211C)
    val darkSecondaryText = Color(0xFFACB9AD)
    val warning = Color(0xFFFFC28A)
    val lightBackground = Color(0xFFF7FAF5)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSecondaryText = Color(0xFF465148)
}

/** Material 2-compatible theme boundary while the existing app migrates incrementally. */
@Composable
fun SearchhhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        darkColors(
            primary = SearchhhDesignTokens.accent,
            background = SearchhhDesignTokens.darkBackground,
            surface = SearchhhDesignTokens.darkSurface,
            onPrimary = Color(0xFF14200D),
            onBackground = Color(0xFFE8F0E8),
            onSurface = Color(0xFFE8F0E8),
            error = Color(0xFFFFB4AB),
        )
    } else {
        lightColors(
            primary = Color(0xFF496A18),
            background = SearchhhDesignTokens.lightBackground,
            surface = SearchhhDesignTokens.lightSurface,
            onPrimary = Color.White,
            onBackground = Color(0xFF182019),
            onSurface = Color(0xFF182019),
            error = Color(0xFFBA1A1A),
        )
    }
    MaterialTheme(
        colors = colors,
        typography = Typography().copy(h4 = Typography().h4.copy(fontWeight = FontWeight.Bold)),
        shapes = MaterialTheme.shapes.copy(
            small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        ),
        content = content,
    )
}
