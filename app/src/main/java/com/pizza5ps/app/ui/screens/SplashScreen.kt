package com.pizza5ps.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.R
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.warmShadow
import kotlinx.coroutines.delay

/**
 * Splash.
 *
 * Choreographed as a short sequence rather than one fade: the emblem springs in, the
 * wordmark and tagline follow, and only then does the app hand over. Staging it means
 * the brand lands as a beat instead of a flash, and the whole thing is still under a
 * second and a half so it never feels like it is holding the user up.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val emblem = remember { Animatable(0f) }
    val wordmark = remember { Animatable(0f) }
    val tagline = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Overshoots on the way in, which is what gives the emblem its pop.
        emblem.animateTo(1f, Motion.bouncySpring())
        wordmark.animateTo(1f, Motion.gentleSpring())
        tagline.animateTo(1f, Motion.gentleSpring())
        delay(420)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PizzaTheme.colors.ground)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PizzaTheme.colors.brandSoft,
                        PizzaTheme.colors.ground.copy(alpha = 0f)
                    ),
                    center = Offset(540f, 900f),
                    radius = 1100f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Space.xxl)
        ) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer {
                        val progress = emblem.value
                        scaleX = progress
                        scaleY = progress
                        alpha = progress.coerceIn(0f, 1f)
                        // A quarter-turn unwinding as it settles, like a tossed base.
                        rotationZ = (1f - progress) * -90f
                    }
                    .warmShadow(20.dp, CircleShape, tint = PizzaTheme.colors.brand)
                    .clip(CircleShape)
                    .background(PizzaTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pizza_logo),
                    contentDescription = "Pizza 5 P's",
                    modifier = Modifier.size(108.dp)
                )
            }

            Spacer(modifier = Modifier.height(Space.xxl))

            Text(
                text = "PIZZA 5 P'S",
                fontFamily = PlayfairFamily,
                style = MaterialTheme.typography.displayMedium,
                color = PizzaTheme.colors.brand,
                modifier = Modifier.graphicsLayer {
                    alpha = wordmark.value
                    translationY = (1f - wordmark.value) * 22f
                }
            )

            Spacer(modifier = Modifier.height(Space.xs))

            Text(
                text = "Perfect Pizza. Perfect Moments.",
                style = MaterialTheme.typography.titleMedium,
                color = PizzaTheme.colors.textMuted,
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value
                    translationY = (1f - tagline.value) * 16f
                }
            )

            Spacer(modifier = Modifier.height(Space.huge))

            LoadingDots(
                modifier = Modifier.graphicsLayer { alpha = tagline.value }
            )
        }
    }
}

/**
 * Three dots breathing in sequence. Each is phase-shifted so the group reads as a
 * travelling wave rather than three lights blinking together.
 */
@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "dots")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPhase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val offset = (phase + index / dotCount.toFloat()) % 1f
            // Triangle wave: rises to 1 at the halfway point, then falls back.
            val intensity = 1f - kotlin.math.abs(offset - 0.5f) * 2f
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer {
                        val scale = 0.65f + intensity * 0.45f
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(
                        Color(
                            red = PizzaTheme.colors.brand.red,
                            green = PizzaTheme.colors.brand.green,
                            blue = PizzaTheme.colors.brand.blue,
                            alpha = 0.35f + intensity * 0.65f
                        )
                    )
            )
        }
    }
}
