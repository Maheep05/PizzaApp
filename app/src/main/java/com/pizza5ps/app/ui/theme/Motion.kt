package com.pizza5ps.app.ui.theme

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Motion tokens.
 *
 * Every animation in the app draws its spec from here rather than inventing its own
 * duration. A shared, small set of springs is what makes a UI feel like one object
 * that responds consistently, instead of a collection of screens that each bounce
 * differently. Physical interactions (press, drag, reveal) use springs; colour and
 * opacity crossfades use short eased tweens.
 */
object Motion {

    // Generic factories, so the same physics can drive Float, Dp, Color or Offset.
    // The Float aliases below exist purely because most call sites animate a Float.

    /** Quick, barely-overshooting. Presses, taps, icon state changes. */
    fun <T> snappySpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.85f,
        stiffness = 900f
    )

    /** Playful overshoot. Reserved for moments of delight — add-to-cart, favourites. */
    fun <T> bouncySpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.42f,
        stiffness = 520f
    )

    /** Soft settle for larger travel — sheets, reveals, hero entrances. */
    fun <T> gentleSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.78f,
        stiffness = 260f
    )

    /** No overshoot at all, for anything that must not look wobbly (progress lines). */
    fun <T> smoothSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val SpringSnappy: FiniteAnimationSpec<Float> = snappySpring()
    val SpringBouncy: FiniteAnimationSpec<Float> = bouncySpring()
    val SpringGentle: FiniteAnimationSpec<Float> = gentleSpring()
    val SpringSmooth: FiniteAnimationSpec<Float> = smoothSpring()

    /** Material's emphasised-decelerate curve: enters fast, settles slowly. */
    val EaseEmphasised: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EaseStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EaseOutBack: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

    const val DurationFast = 150
    const val DurationMedium = 260
    const val DurationSlow = 420
    const val DurationDeliberate = 700

    /** Delay between consecutive items in a staggered list entrance. */
    const val StaggerStepMs = 55L

    fun <T> fastTween() = tween<T>(DurationFast, easing = EaseStandard)
    fun <T> mediumTween() = tween<T>(DurationMedium, easing = EaseStandard)
    fun <T> emphasisedTween() = tween<T>(DurationSlow, easing = EaseEmphasised)
}

/** Light tactile ticks. Kept in one place so feedback weight stays consistent. */
class Haptics(private val view: View) {
    fun tap() = view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    fun select() = view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    fun confirm() = view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/**
 * Springs the element down while pressed. Applied through [graphicsLayer] so it
 * never triggers a re-layout of anything around it.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.SpringSnappy,
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * The app's standard tap affordance: spring scale-down, a light haptic tick, and no
 * Material ripple (the scale *is* the feedback, and ripples read as dated here).
 */
@Composable
fun Modifier.bouncyClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    hapticOnTap: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberHaptics()
    return this
        .pressScale(interactionSource, pressedScale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            if (hapticOnTap) haptics.tap()
            onClick()
        }
}

/**
 * Soft, tinted elevation.
 *
 * Black shadows turn a cream ground muddy grey, so in the light appearance depth is
 * tinted with roasted brown to stay on the palette's warm axis. The dark appearance
 * supplies plain black instead, because a warm shadow over an espresso ground reads
 * as a smudge rather than as depth.
 */
@Composable
fun Modifier.warmShadow(
    elevation: Dp,
    shape: Shape,
    tint: Color = PizzaTheme.colors.shadowTint
): Modifier {
    val isDark = PizzaTheme.colors.isDark
    return shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = tint.copy(alpha = if (isDark) 0.30f else 0.10f),
        spotColor = tint.copy(alpha = if (isDark) 0.55f else 0.22f)
    )
}

/** Coloured glow used sparingly on primary CTAs to make them feel lit. */
@Composable
fun Modifier.brandGlow(
    elevation: Dp = 14.dp,
    shape: Shape,
    color: Color = PizzaTheme.colors.brand
): Modifier = shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = color.copy(alpha = 0.20f),
    spotColor = color.copy(alpha = 0.42f)
)

/** Travelling highlight for skeleton placeholders. */
@Composable
fun shimmerBrush(
    base: Color = PizzaTheme.colors.surfaceMuted,
    highlight: Color = PizzaTheme.colors.surface
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "shimmerProgress"
    )
    val sweep = 900f
    val x = -sweep + progress * (sweep * 3f)
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x, 0f),
        end = Offset(x + sweep, sweep)
    )
}

/**
 * Fades and lifts content into place, offset by [index] so a list arrives as a
 * cascade rather than all at once. Runs exactly once per composition key.
 */
@Composable
fun StaggeredEntry(
    index: Int,
    modifier: Modifier = Modifier,
    riseFrom: Float = 34f,
    stepMs: Long = Motion.StaggerStepMs,
    content: @Composable () -> Unit
) {
    val progress = remember(index) { Animatable(0f) }
    LaunchedEffectOnce(index) {
        delay(index * stepMs)
        progress.animateTo(1f, Motion.SpringGentle)
    }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * riseFrom
        }
    ) {
        content()
    }
}

/** Small readability wrapper so entrance effects read declaratively at call sites. */
@Composable
private fun LaunchedEffectOnce(key: Any?, block: suspend () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(key) { block() }
}
