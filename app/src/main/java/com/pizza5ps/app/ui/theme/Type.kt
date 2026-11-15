package com.pizza5ps.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.R

/**
 * Type system.
 *
 * Two bundled families, each with one job:
 *
 *  - **Playfair Display** for display text. A high-contrast transitional serif that
 *    gives the menu an editorial, printed-carte voice.
 *  - **Inter** for every piece of functional UI. Designed specifically for screens,
 *    with tall x-height and unambiguous letterforms at small sizes.
 *
 * Both are bundled rather than pulled from Google Fonts at runtime, so the app never
 * flashes a fallback face and works with no network — and both are SIL Open Font
 * Licensed (see THIRD_PARTY_LICENSES.txt at the project root).
 *
 * They ship as *variable* fonts: one file per family covering the whole weight axis.
 * Declaring a [FontVariation.weight] for each weight means real interpolated weights
 * rather than the OS faking bold by smearing the regular outline.
 *
 * Optical sizing is expressed the traditional way: large sizes get negative tracking
 * so headlines read tight, while small caps-y labels get positive tracking so they
 * stay legible. The two move in opposite directions as size drops.
 */

@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

@OptIn(ExperimentalTextApi::class)
private fun playfairFont(weight: Int) = Font(
    resId = R.font.playfair_display_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

val InterFamily = FontFamily(
    interFont(400),
    interFont(500),
    interFont(600),
    interFont(700),
    interFont(800)
)

val PlayfairFamily = FontFamily(
    playfairFont(500),
    playfairFont(600),
    playfairFont(700),
    playfairFont(800)
)

private val Display = PlayfairFamily
private val Ui = InterFamily

/** Keeps vertical rhythm exact by not trimming the font's own leading. */
private val PreciseLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 45.sp,
        letterSpacing = (-0.8).sp,
        lineHeightStyle = PreciseLineHeight
    ),
    displayMedium = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.5).sp,
        lineHeightStyle = PreciseLineHeight
    ),
    displaySmall = TextStyle(
        fontFamily = Display,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 31.sp,
        letterSpacing = (-0.3).sp,
        lineHeightStyle = PreciseLineHeight
    ),
    headlineLarge = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    ),
    // Overline-style micro label: small, bold, widely tracked.
    labelSmall = TextStyle(
        fontFamily = Ui,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.1.sp
    )
)
