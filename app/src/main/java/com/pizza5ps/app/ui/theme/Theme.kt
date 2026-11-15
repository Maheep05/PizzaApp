package com.pizza5ps.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Which appearance the user has chosen. SYSTEM follows the device setting. */
enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Semantic colour roles.
 *
 * Screens reference *roles* — "surface", "textMuted", "border" — never raw hues.
 * That indirection is what makes two appearances possible without touching a single
 * screen: swap the role table and every surface follows. It also stops the palette
 * drifting, because there is exactly one place where a role's value is decided.
 */
@Immutable
data class PizzaColors(
    val ground: Color,
    val groundElevated: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val hairline: Color,
    val brand: Color,
    val brandDark: Color,
    val brandSoft: Color,
    val onBrand: Color,
    val accent: Color,
    val accentSoft: Color,
    val success: Color,
    val successSoft: Color,
    val star: Color,
    /** High-contrast bar that deliberately opposes the ground — the floating
     *  cart bar and the active-order banner. */
    val inverse: Color,
    val onInverse: Color,
    val shadowTint: Color,
    val scrim: Color,
    val isDark: Boolean
)

private val LightPizzaColors = PizzaColors(
    ground = WarmCream,
    groundElevated = WarmCreamDark,
    surface = SurfaceWhite,
    surfaceMuted = LightBeige,
    textPrimary = CharcoalDark,
    textSecondary = CharcoalMedium,
    textMuted = TextMuted,
    border = MutedBorder,
    hairline = HairlineBorder,
    brand = TomatoRed,
    brandDark = TomatoRedDark,
    brandSoft = TomatoRedSoft,
    onBrand = SurfaceWhite,
    accent = CrustAmber,
    accentSoft = CrustHoney,
    success = BasilGreen,
    successSoft = BasilGreenLight,
    star = StarYellow,
    inverse = CharcoalDark,
    onInverse = WarmCream,
    shadowTint = ShadowWarm,
    scrim = CharcoalDark.copy(alpha = 0.45f),
    isDark = false
)

/**
 * The dark appearance is a wood-fired oven interior, not a grey inversion: grounds
 * are warm espresso browns so the brand red still reads as heat rather than as an
 * error state. The red itself is lightened, because #D62828 on a near-black ground
 * fails contrast for text and looks muddy for fills.
 */
private val DarkPizzaColors = PizzaColors(
    ground = EspressoBase,
    groundElevated = EspressoRaised,
    surface = EspressoSurface,
    surfaceMuted = EspressoElevated,
    textPrimary = CreamText,
    textSecondary = CreamTextSecondary,
    textMuted = CreamTextMuted,
    border = EspressoBorder,
    hairline = EspressoHairline,
    brand = TomatoOnDark,
    brandDark = TomatoOnDarkDeep,
    brandSoft = TomatoOnDarkSoft,
    onBrand = EspressoBase,
    accent = CrustAmberOnDark,
    accentSoft = CrustHoneyOnDark,
    success = BasilGreenOnDark,
    successSoft = BasilGreenOnDarkSoft,
    star = StarYellowOnDark,
    inverse = CreamText,
    onInverse = EspressoBase,
    shadowTint = Color.Black,
    scrim = Color.Black.copy(alpha = 0.6f),
    isDark = true
)

/**
 * Static because the whole table is swapped at once when the appearance changes;
 * there is no benefit to tracking reads of individual roles.
 */
val LocalPizzaColors = staticCompositionLocalOf { LightPizzaColors }

/** Entry point for screens: `PizzaTheme.colors.surface`. */
object PizzaTheme {
    val colors: PizzaColors
        @Composable
        get() = LocalPizzaColors.current
}

private fun PizzaColors.toMaterialScheme() = if (isDark) {
    darkColorScheme(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandSoft,
        onPrimaryContainer = brand,
        secondary = accent,
        onSecondary = onBrand,
        background = ground,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceMuted,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = hairline,
        error = brand,
        onError = onBrand
    )
} else {
    lightColorScheme(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = brandSoft,
        onPrimaryContainer = TomatoRedDeep,
        secondary = accent,
        onSecondary = onBrand,
        tertiary = success,
        onTertiary = onBrand,
        tertiaryContainer = successSoft,
        onTertiaryContainer = BasilGreenDeep,
        background = ground,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceMuted,
        onSurfaceVariant = textSecondary,
        surfaceTint = brand,
        outline = border,
        outlineVariant = hairline,
        error = brandDark,
        onError = onBrand
    )
}

@Composable
fun Pizza5PsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (useDark) DarkPizzaColors else LightPizzaColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Content draws behind the bars; the icons invert with the appearance.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDark
                isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalPizzaColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = Typography,
            shapes = PizzeriaShapes,
            content = content
        )
    }
}
