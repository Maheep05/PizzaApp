package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pizza5ps.app.data.DeliveryGeo
import com.pizza5ps.app.data.DeliveryRoute
import com.pizza5ps.app.data.DeliveryStop
import com.pizza5ps.app.model.OrderStatus
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.warmShadow
import com.pizza5ps.app.ui.theme.PizzaTheme

/**
 * How far along the route the rider should be for a given stage.
 *
 * The rider creeps forward while the pizza is still in the kitchen (it has not left
 * yet), then covers the bulk of the route during the out-for-delivery stage, which
 * is where the animation is actually worth watching.
 */
private fun OrderStatus.riderFraction(): Float = when (this) {
    OrderStatus.CONFIRMED -> 0f
    OrderStatus.PREPARING -> 0.04f
    OrderStatus.OUT_FOR_DELIVERY -> 0.88f
    OrderStatus.DELIVERED -> 1f
}

/** The out-for-delivery leg is paced to the 5s demo stage so the rider is visibly
 *  driving for the whole stage rather than teleporting and then waiting. */
private fun OrderStatus.legDurationMs(): Int = when (this) {
    OrderStatus.OUT_FOR_DELIVERY -> 5_000
    OrderStatus.DELIVERED -> 1_200
    else -> 800
}

/**
 * Live map card for the tracking screen: a real OpenStreetMap view with the road
 * route drawn on it, the rider advancing along that route as the order progresses,
 * and the trip's real distance and duration underneath.
 */
@Composable
fun DeliveryMapCard(
    route: DeliveryRoute?,
    isLoading: Boolean,
    status: OrderStatus,
    destination: DeliveryStop,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val riderFraction by animateFloatAsState(
        targetValue = status.riderFraction(),
        animationSpec = tween(
            durationMillis = status.legDurationMs(),
            easing = androidx.compose.animation.core.LinearEasing
        ),
        label = "riderFraction"
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .warmShadow(10.dp, RoundedCornerShape(Radius.lg))
                .clip(RoundedCornerShape(Radius.lg))
                .background(PizzaTheme.colors.surface)
        ) {
            if (route == null) {
                MapLoadingState(isLoading = isLoading)
            } else {
                DeliveryMapView(
                    route = route,
                    riderFraction = riderFraction,
                    destination = destination,
                    modifier = Modifier.fillMaxSize(),
                    interactive = false,
                    onTapToExpand = { expanded = true }
                )
            }

            // Live badge, top-left.
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Space.md)
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.surface.copy(alpha = 0.94f))
                    .padding(horizontal = Space.sm, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
                    if (status == OrderStatus.OUT_FOR_DELIVERY) {
                        PulseRings(color = PizzaTheme.colors.brand, ringCount = 2)
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (status == OrderStatus.DELIVERED) PizzaTheme.colors.success else PizzaTheme.colors.brand)
                    )
                }
                Text(
                    text = if (status == OrderStatus.DELIVERED) "ARRIVED" else "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = PizzaTheme.colors.textPrimary
                )
            }

            // Expand affordance, top-right.
            if (route != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Space.md)
                        .size(32.dp)
                        .clip(RoundedCornerShape(Radius.xs))
                        .background(PizzaTheme.colors.surface.copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand map",
                        tint = PizzaTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Attribution. OpenStreetMap's licence requires crediting the data.
            Text(
                text = "© OpenStreetMap contributors",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textPrimary.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Space.xs)
                    .clip(RoundedCornerShape(Radius.xs))
                    .background(PizzaTheme.colors.surface.copy(alpha = 0.7f))
                    .padding(horizontal = Space.xs)
            )
        }

        Spacer(modifier = Modifier.height(Space.sm))

        RouteFacts(route = route, destination = destination)
    }

    if (expanded && route != null) {
        FullScreenMap(
            route = route,
            riderFraction = riderFraction,
            destination = destination,
            onDismiss = { expanded = false }
        )
    }
}

/** Distance, duration and drop-off, read off the resolved route. */
@Composable
private fun RouteFacts(
    route: DeliveryRoute?,
    destination: DeliveryStop,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(PizzaTheme.colors.surface)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DROPPING AT",
                style = MaterialTheme.typography.labelSmall,
                color = PizzaTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(Space.xxs))
            Text(
                text = destination.label,
                style = MaterialTheme.typography.titleMedium,
                color = PizzaTheme.colors.textPrimary
            )
            Text(
                text = destination.address,
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.width(Space.md))

        Column(horizontalAlignment = Alignment.End) {
            if (route == null) {
                ShimmerBox(modifier = Modifier.size(width = 64.dp, height = 18.dp))
            } else {
                TagPill(
                    text = route.distanceLabel,
                    leadingIcon = Icons.Default.Route,
                    containerColor = PizzaTheme.colors.brand.copy(alpha = 0.10f),
                    contentColor = PizzaTheme.colors.brand
                )
                Spacer(modifier = Modifier.height(Space.xs))
                TagPill(
                    text = route.durationLabel,
                    leadingIcon = Icons.Default.Schedule,
                    containerColor = PizzaTheme.colors.accent.copy(alpha = 0.14f),
                    contentColor = PizzaTheme.colors.accent
                )
                if (route.isApproximate) {
                    Spacer(modifier = Modifier.height(Space.xs))
                    // Says so plainly rather than passing a synthesised line off as
                    // a real road route.
                    TagPill(
                        text = "Offline estimate",
                        leadingIcon = Icons.Default.WifiOff,
                        containerColor = PizzaTheme.colors.textMuted.copy(alpha = 0.12f),
                        contentColor = PizzaTheme.colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLoadingState(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShimmerBox(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(Radius.lg))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Route,
                contentDescription = null,
                tint = PizzaTheme.colors.brand,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(Space.sm))
            Text(
                text = if (isLoading) "Finding the fastest route…" else "Route unavailable",
                style = MaterialTheme.typography.titleSmall,
                color = PizzaTheme.colors.textPrimary
            )
        }
    }
}

/** Interactive full-screen map — pan and zoom live here, not on the inline card. */
@Composable
private fun FullScreenMap(
    route: DeliveryRoute,
    riderFraction: Float,
    destination: DeliveryStop,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DeliveryMapView(
                route = route,
                riderFraction = riderFraction,
                destination = destination,
                modifier = Modifier.fillMaxSize(),
                interactive = true
            )

            // Gradient scrim so the controls stay legible over any tile.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(Space.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close map",
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(Space.md))
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.md))
                        .background(PizzaTheme.colors.surface.copy(alpha = 0.94f))
                        .padding(horizontal = Space.md, vertical = Space.sm)
                ) {
                    Text(
                        text = "${DeliveryGeo.store.label} → ${destination.label}",
                        style = MaterialTheme.typography.titleSmall,
                        color = PizzaTheme.colors.textPrimary
                    )
                    Text(
                        text = "${route.distanceLabel} • ${route.durationLabel}" +
                            if (route.isApproximate) " • offline estimate" else " • shortest road route",
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.textMuted
                    )
                }
            }
        }
    }
}
