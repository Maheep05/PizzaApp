package com.pizza5ps.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.ui.theme.CheeseYellow
import com.pizza5ps.app.ui.theme.CrustGold
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.shimmerBrush
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.pizza5ps.app.ui.theme.PizzaTheme

/** Skeleton placeholder with a travelling highlight. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.sm)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

/**
 * A one-shot confetti burst for the delivery moment.
 *
 * Celebration is the one place in an ordering flow where motion can be purely
 * expressive — the order is complete, there is nothing left to do, and the
 * animation is the reward. Particles are launched in an upward fan with gravity and
 * per-particle drift and spin, so the burst never looks like a repeating loop.
 */
@Composable
fun ConfettiBurst(
    active: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 90
) {
    val progress = remember { Animatable(0f) }
    // Read outside remember — a CompositionLocal cannot be read from remember's
    // calculation — and keyed in, so the palette follows a change of appearance.
    val palette = listOf(
        PizzaTheme.colors.brand,
        PizzaTheme.colors.accent,
        CrustGold,
        CheeseYellow,
        PizzaTheme.colors.success
    )
    val particles = remember(particleCount, palette) {
        val random = Random(7)
        List(particleCount) {
            ConfettiParticle(
                // Launched upward in a wide fan (-160°..-20°).
                angle = -160f + random.nextFloat() * 140f,
                speed = 0.55f + random.nextFloat() * 0.75f,
                spin = (random.nextFloat() - 0.5f) * 1400f,
                size = 6f + random.nextFloat() * 9f,
                drift = (random.nextFloat() - 0.5f) * 0.5f,
                startFraction = random.nextFloat(),
                color = palette[random.nextInt(palette.size)],
                isRound = random.nextBoolean()
            )
        }
    }

    LaunchedEffect(active) {
        if (active) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(2400, easing = LinearEasing))
        }
    }

    if (!active) return

    Canvas(modifier = modifier) {
        val time = progress.value
        // Fade the whole burst out over the last third of its life.
        val fade = (1f - ((time - 0.65f) / 0.35f)).coerceIn(0f, 1f)
        if (fade <= 0f) return@Canvas

        particles.forEach { particle ->
            val origin = Offset(size.width * particle.startFraction, size.height * 0.42f)
            val radians = Math.toRadians(particle.angle.toDouble())
            val travel = particle.speed * size.height

            val x = origin.x + cos(radians).toFloat() * travel * time +
                particle.drift * size.width * time * time
            // Ballistic arc: linear rise minus a quadratic gravity term.
            val y = origin.y + sin(radians).toFloat() * travel * time +
                1.9f * size.height * time * time

            rotate(degrees = particle.spin * time, pivot = Offset(x, y)) {
                if (particle.isRound) {
                    drawCircle(
                        color = particle.color.copy(alpha = fade),
                        radius = particle.size / 2f,
                        center = Offset(x, y)
                    )
                } else {
                    drawRect(
                        color = particle.color.copy(alpha = fade),
                        topLeft = Offset(x - particle.size / 2f, y - particle.size / 2f),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val spin: Float,
    val size: Float,
    val drift: Float,
    val startFraction: Float,
    val color: Color,
    val isRound: Boolean
)

/**
 * Expanding rings that mark "this stage is happening right now" on the delivery
 * tracker. A pulse reads as ongoing activity in a way a static dot cannot.
 */
@Composable
fun PulseRings(
    modifier: Modifier = Modifier,
    color: Color = PizzaTheme.colors.brand,
    ringCount: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing)
        ),
        label = "pulseProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2f
        repeat(ringCount) { index ->
            // Offset each ring's phase so they emerge in sequence.
            val phase = (progress + index / ringCount.toFloat()) % 1f
            drawCircle(
                color = color.copy(alpha = (1f - phase) * 0.45f),
                radius = maxRadius * (0.4f + phase * 0.6f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * A heart that bursts when favourited: the icon overshoots while a ring of short
 * spokes flies outward, then everything settles. Purely expressive, and the kind of
 * micro-reward that makes a list feel responsive to touch.
 */
@Composable
fun FavouriteBurst(
    favourited: Boolean,
    modifier: Modifier = Modifier,
    color: Color = PizzaTheme.colors.brand
) {
    val burst = remember { Animatable(0f) }
    // Skips the burst on first composition for items that are already favourites.
    val firstPass = remember { booleanArrayOf(true) }

    LaunchedEffect(favourited) {
        if (firstPass[0]) {
            firstPass[0] = false
            return@LaunchedEffect
        }
        if (favourited) {
            burst.snapTo(0f)
            burst.animateTo(1f, tween(Motion.DurationSlow, easing = Motion.EaseEmphasised))
        }
    }

    // The progress read happens inside the draw scope, not in composition, so the
    // burst repaints per frame without recomposing the card around it.
    Canvas(modifier = modifier) {
        val time = burst.value
        if (time <= 0f || time >= 1f) return@Canvas
        val fade = 1f - time
        val centre = Offset(size.width / 2f, size.height / 2f)
        val spokeCount = 8
        repeat(spokeCount) { index ->
            val angle = Math.toRadians((index * 360f / spokeCount).toDouble())
            val inner = size.minDimension * (0.28f + time * 0.34f)
            val outer = inner + size.minDimension * 0.16f * fade
            drawLine(
                color = color.copy(alpha = fade * 0.9f),
                start = Offset(
                    centre.x + cos(angle).toFloat() * inner,
                    centre.y + sin(angle).toFloat() * inner
                ),
                end = Offset(
                    centre.x + cos(angle).toFloat() * outer,
                    centre.y + sin(angle).toFloat() * outer
                ),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
