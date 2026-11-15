package com.pizza5ps.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.warmShadow
import com.pizza5ps.app.ui.theme.PizzaTheme

/**
 * Home header.
 *
 * The app draws edge to edge behind the system bars, so every top bar owns its own
 * [statusBarsPadding] — without it the first line of content sits underneath the
 * clock, which is exactly what was happening before.
 *
 * [collapseFraction] (0 at rest, 1 once scrolled) frosts the bar over content as it
 * passes beneath: the background fades in and a hairline appears. The bar stays
 * legible over scrolling cards without needing an opaque block that would waste
 * vertical space at rest.
 */
@Composable
fun HomeTopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f,
    onLocationClick: () -> Unit = {},
    addressLabel: String = "Choose an address",
    initials: String = "?"
) {
    val frost = collapseFraction.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PizzaTheme.colors.surface.copy(alpha = frost * 0.94f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Space.gutter, vertical = Space.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .bouncyClick(pressedScale = 0.96f, onClick = onLocationClick)
                    .padding(Space.xxs)
            ) {
                Text(
                    text = "DELIVERING TO",
                    style = MaterialTheme.typography.labelSmall,
                    color = PizzaTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PizzaTheme.colors.brand,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = addressLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = PizzaTheme.colors.textPrimary,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Change location",
                        tint = PizzaTheme.colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            ProfileAvatar(initials = initials, onClick = onProfileClick)
        }

        // Hairline that only exists once content is passing underneath.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = frost }
                .background(PizzaTheme.colors.hairline)
        )
    }
}

/** Initials avatar inside a warm gradient ring. */
@Composable
private fun ProfileAvatar(
    initials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .warmShadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(PizzaTheme.colors.brand, PizzaTheme.colors.accent)))
            .bouncyClick(pressedScale = 0.9f, onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PizzaTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                color = PizzaTheme.colors.brand
            )
        }
    }
}

/**
 * Header for every screen below Home. [scrolled] raises a hairline and background
 * once content moves under it, matching the Home bar's behaviour.
 */
@Composable
fun StandardTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    scrolled: Boolean = false,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PizzaTheme.colors.surface.copy(alpha = if (scrolled) 0.94f else 0f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick
            )

            Spacer(modifier = Modifier.width(Space.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PizzaTheme.colors.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.textMuted
                    )
                }
            }

            trailingAction?.invoke()
        }

        if (scrolled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(PizzaTheme.colors.hairline)
            )
        }
    }
}
