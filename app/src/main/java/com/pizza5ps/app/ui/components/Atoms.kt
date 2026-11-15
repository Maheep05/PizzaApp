package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.brandGlow
import com.pizza5ps.app.ui.theme.warmShadow

/** Consistent section heading with an optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter, vertical = Space.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = PizzaTheme.colors.textPrimary
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = PizzaTheme.colors.brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .bouncyClick(pressedScale = 0.9f, onClick = onActionClick)
                    .padding(horizontal = Space.sm, vertical = Space.xs)
            )
        }
    }
}

/** Small tinted label — dietary badges, discounts, prep time, coupon codes. */
@Composable
fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = PizzaTheme.colors.brand.copy(alpha = 0.10f),
    contentColor: Color = PizzaTheme.colors.brand,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(containerColor)
            .padding(horizontal = Space.sm, vertical = Space.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * The app's one primary action.
 *
 * It carries a warm gradient plus a red glow rather than a flat fill, so the single
 * most important control on any screen is unmistakably the brightest thing on it —
 * a clear visual hierarchy the user can find without reading.
 */
@Composable
fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    height: Dp = 56.dp
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = Motion.mediumTween(),
        label = "ctaGlow"
    )
    val shape = RoundedCornerShape(Radius.md)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .brandGlow(elevation = (14 * glowAlpha).dp, shape = shape)
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(PizzaTheme.colors.brand, PizzaTheme.colors.brandDark))
                } else {
                    Brush.horizontalGradient(listOf(PizzaTheme.colors.textMuted, PizzaTheme.colors.textMuted))
                }
            )
            .bouncyClick(enabled = enabled, pressedScale = 0.975f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = PizzaTheme.colors.surface
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(Space.sm))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = PizzaTheme.colors.surface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** Where a primary action can be idle, working, or done. */
enum class ActionState { IDLE, LOADING, SUCCESS }

/**
 * A primary button that morphs through its own progress instead of handing the user
 * off to a blocking spinner dialog. Keeping the feedback inside the control the user
 * just pressed preserves the sense of direct manipulation.
 */
@Composable
fun MorphingActionButton(
    label: String,
    state: ActionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    successLabel: String = "Confirmed"
) {
    val shape = RoundedCornerShape(Radius.md)
    val container by animateColorAsState(
        targetValue = when (state) {
            ActionState.SUCCESS -> PizzaTheme.colors.success
            else -> PizzaTheme.colors.brand
        },
        animationSpec = Motion.mediumTween(),
        label = "morphColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .brandGlow(elevation = 14.dp, shape = shape, color = container)
            .clip(shape)
            .background(container)
            .bouncyClick(
                enabled = state == ActionState.IDLE,
                pressedScale = 0.975f,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (scaleIn(initialScale = 0.7f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.7f) + fadeOut())
            },
            label = "ctaState"
        ) { current ->
            when (current) {
                ActionState.IDLE -> Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = PizzaTheme.colors.surface
                )

                ActionState.LOADING -> CircularProgressIndicator(
                    color = PizzaTheme.colors.surface,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )

                ActionState.SUCCESS -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PizzaTheme.colors.surface,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = successLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = PizzaTheme.colors.surface
                    )
                }
            }
        }
    }
}

/** Circular icon button on a white chip — used for back, close and overflow. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = PizzaTheme.colors.textPrimary,
    container: Color = PizzaTheme.colors.surface
) {
    Box(
        modifier = modifier
            .size(size)
            .warmShadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, PizzaTheme.colors.hairline, CircleShape)
            .bouncyClick(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}

/** White card surface with the app's standard warm elevation and hairline edge. */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Radius.lg),
    elevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .warmShadow(elevation, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
    ) {
        content()
    }
}

/**
 * Warm gradient used across promotional surfaces. A function rather than a constant
 * because its colours come from the active appearance.
 */
@Composable
fun promoGradient(): Brush = Brush.linearGradient(
    listOf(PizzaTheme.colors.brand, PizzaTheme.colors.accent)
)
