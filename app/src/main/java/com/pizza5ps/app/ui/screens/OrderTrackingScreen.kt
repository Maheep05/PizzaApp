package com.pizza5ps.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.OrderStatus
import com.pizza5ps.app.ui.components.ConfettiBurst
import com.pizza5ps.app.ui.components.DeliveryMapCard
import com.pizza5ps.app.ui.components.OrderTimelineStepper
import com.pizza5ps.app.ui.components.PizzaBase
import com.pizza5ps.app.ui.components.PrimaryActionButton
import com.pizza5ps.app.ui.components.SoftCard
import com.pizza5ps.app.ui.components.TagPill
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.viewmodel.PizzaViewModel

/**
 * Order tracking.
 *
 * Reads top to bottom as one narrative: where the order *is* (ETA), where the rider
 * is (map), what has already happened (timeline), who is bringing it (rider), and
 * what was ordered (summary). Everything the customer is anxious about is answered
 * before anything they merely might want to check.
 */
@Composable
fun OrderTrackingScreen(
    viewModel: PizzaViewModel,
    onBackToHome: () -> Unit
) {
    val activeOrder by viewModel.activeOrder.collectAsState()
    val status = activeOrder?.status ?: OrderStatus.CONFIRMED
    val deliveryRoute by viewModel.deliveryRoute.collectAsState()
    val isRouteLoading by viewModel.isRouteLoading.collectAsState()
    val destination by viewModel.deliveryDestination.collectAsState()

    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(5) }
    var isAutoPlaying by remember { mutableStateOf(true) }

    val isDelivered = status == OrderStatus.DELIVERED

    Scaffold(
        topBar = {
            TrackingTopBar(
                orderId = activeOrder?.id ?: "P5P-10284",
                isAutoPlaying = isAutoPlaying,
                onBack = onBackToHome,
                onToggleAutoPlay = {
                    isAutoPlaying = !isAutoPlaying
                    if (isAutoPlaying) {
                        viewModel.startTrackingDemoProgression()
                    } else {
                        viewModel.stopTrackingDemo()
                    }
                }
            )
        },
        bottomBar = {
            TrackingBottomBar(
                isDelivered = isDelivered,
                onRate = { showRatingDialog = true },
                onHome = {
                    viewModel.clearActiveOrder()
                    onBackToHome()
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.gutter),
                verticalArrangement = Arrangement.spacedBy(Space.lg)
            ) {
                EtaHero(status = status)

                DeliveryMapCard(
                    route = deliveryRoute,
                    isLoading = isRouteLoading,
                    status = status,
                    destination = destination
                )

                OrderTimelineStepper(currentStatus = status)

                RiderCard(isDelivered = isDelivered)

                activeOrder?.let { order ->
                    OrderSummaryCard(order = order)
                }

                DemoControls(
                    status = status,
                    onStep = { viewModel.setTrackingStage(it) }
                )

                Spacer(modifier = Modifier.height(Space.sm))
            }

            // The delivery moment is the one point in the flow where motion can be
            // purely celebratory — the order is complete and nothing is left to do.
            ConfettiBurst(
                active = isDelivered,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            rating = selectedRating,
            onRatingChange = { selectedRating = it },
            onDismiss = { showRatingDialog = false },
            onSubmit = {
                showRatingDialog = false
                viewModel.clearActiveOrder()
                onBackToHome()
            }
        )
    }
}

@Composable
private fun TrackingTopBar(
    orderId: String,
    isAutoPlaying: Boolean,
    onBack: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Space.lg, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.pizza5ps.app.ui.components.CircleIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to home",
            onClick = onBack
        )

        Spacer(modifier = Modifier.width(Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Order tracking",
                style = MaterialTheme.typography.headlineSmall,
                color = PizzaTheme.colors.textPrimary
            )
            Text(
                text = "#$orderId",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )
        }

        // Demo control: pauses the automatic stage progression so a presenter can
        // hold on one stage and talk over it.
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.pill))
                .background(
                    if (isAutoPlaying) {
                        PizzaTheme.colors.brand.copy(alpha = 0.12f)
                    } else {
                        PizzaTheme.colors.surface
                    }
                )
                .bouncyClick(pressedScale = 0.92f, onClick = onToggleAutoPlay)
                .padding(horizontal = Space.md, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            Icon(
                imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isAutoPlaying) "Pause demo" else "Play demo",
                tint = if (isAutoPlaying) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (isAutoPlaying) "Auto" else "Manual",
                style = MaterialTheme.typography.labelMedium,
                color = if (isAutoPlaying) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted
            )
        }
    }
}

/**
 * The headline answer: how long until it arrives.
 *
 * Rendered on the inverse surface so it is the highest-contrast thing on the screen —
 * this is the single fact the customer opened the app to see. The progress bar is
 * segmented into one block per stage rather than a continuous sweep, because the
 * order genuinely moves in four discrete steps.
 */
@Composable
private fun EtaHero(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val isDelivered = status == OrderStatus.DELIVERED
    val headline = when (status) {
        OrderStatus.CONFIRMED -> "Arriving in 25 min"
        OrderStatus.PREPARING -> "Arriving in 18 min"
        OrderStatus.OUT_FOR_DELIVERY -> "Arriving in 7 min"
        OrderStatus.DELIVERED -> "Delivered. Enjoy!"
    }
    val accent by animateColorAsState(
        targetValue = if (isDelivered) PizzaTheme.colors.success else PizzaTheme.colors.brand,
        animationSpec = Motion.mediumTween(),
        label = "etaAccent"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .background(PizzaTheme.colors.inverse)
            .padding(Space.xl)
    ) {
        Text(
            text = if (isDelivered) "ORDER COMPLETED" else "LIVE DELIVERY STATUS",
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )

        Spacer(modifier = Modifier.height(Space.sm))

        // The headline slides as the stage changes, so the number visibly counts down.
        AnimatedContent(
            targetState = headline,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith
                    (slideOutVertically { -it } + fadeOut())
            },
            label = "eta"
        ) { text ->
            Text(
                text = text,
                fontFamily = PlayfairFamily,
                style = MaterialTheme.typography.displaySmall,
                color = PizzaTheme.colors.onInverse
            )
        }

        Spacer(modifier = Modifier.height(Space.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            OrderStatus.values().forEach { stage ->
                val isReached = stage.stepNumber <= status.stepNumber
                val fill by animateFloatAsState(
                    targetValue = if (isReached) 1f else 0f,
                    animationSpec = Motion.gentleSpring(),
                    label = "segment${stage.stepNumber}"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.onInverse.copy(alpha = 0.18f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .graphicsLayer {
                                // Each segment wipes in from its left edge.
                                scaleX = fill
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                            }
                            .clip(RoundedCornerShape(Radius.pill))
                            .background(accent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.sm))

        Text(
            text = status.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = PizzaTheme.colors.onInverse.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun RiderCard(
    isDelivered: Boolean,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Space.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PizzaTheme.colors.brand.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.brand
                )
            }

            Spacer(modifier = Modifier.width(Space.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vikram S.",
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaTheme.colors.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PizzaTheme.colors.star,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "4.9 • Your delivery partner",
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.textMuted
                    )
                }
            }

            // Contact actions disappear once the order is complete — there is nothing
            // left to call about, and leaving dead buttons on screen is worse than
            // removing them.
            if (!isDelivered) {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    ContactButton(icon = Icons.Default.Call, label = "Call rider")
                    ContactButton(icon = Icons.AutoMirrored.Filled.Message, label = "Message rider")
                }
            } else {
                TagPill(
                    text = "Completed",
                    containerColor = PizzaTheme.colors.successSoft,
                    contentColor = PizzaTheme.colors.success
                )
            }
        }
    }
}

@Composable
private fun ContactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(PizzaTheme.colors.surfaceMuted)
            // A demo build has no telephony behind these, so they acknowledge the
            // press and do nothing further.
            .bouncyClick(pressedScale = 0.88f) {},
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = PizzaTheme.colors.textPrimary,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun OrderSummaryCard(
    order: com.pizza5ps.app.model.Order,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Space.lg)) {
            Text(
                text = "YOUR ORDER",
                style = MaterialTheme.typography.labelSmall,
                color = PizzaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(Space.md))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PizzaBase(
                        pizza = item.pizza,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.quantity}× ${item.pizza.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PizzaTheme.colors.textPrimary
                        )
                        Text(
                            text = "${item.size.label} • ${item.crust.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PizzaTheme.colors.textMuted
                        )
                    }
                    Text(
                        text = "₹${item.totalPrice}",
                        style = MaterialTheme.typography.titleMedium,
                        color = PizzaTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Space.md))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PizzaTheme.colors.hairline)
            )
            Spacer(modifier = Modifier.height(Space.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Paid with ${order.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.textMuted
                    )
                    Text(
                        text = "Total paid",
                        style = MaterialTheme.typography.titleMedium,
                        color = PizzaTheme.colors.textPrimary
                    )
                }
                Text(
                    text = "₹${order.grandTotal}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PizzaTheme.colors.brand
                )
            }
        }
    }
}

/**
 * Manual stage stepping, kept for presenting the flow. Labelled as demo controls so
 * it is never mistaken for a real customer-facing feature.
 */
@Composable
private fun DemoControls(
    status: OrderStatus,
    onStep: (OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val stages = OrderStatus.values()

    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Space.lg)) {
            Text(
                text = "DEMO CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                color = PizzaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(Space.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepButton(
                    icon = Icons.Default.FastRewind,
                    label = "Previous stage",
                    enabled = status.ordinal > 0,
                    onClick = { onStep(stages[status.ordinal - 1]) }
                )

                Text(
                    text = "Stage ${status.stepNumber} / ${stages.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = PizzaTheme.colors.textPrimary
                )

                StepButton(
                    icon = Icons.Default.FastForward,
                    label = "Next stage",
                    enabled = status.ordinal < stages.lastIndex,
                    onClick = { onStep(stages[status.ordinal + 1]) },
                    filled = true
                )
            }
        }
    }
}

@Composable
private fun StepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.35f }
            .clip(CircleShape)
            .background(
                if (filled) PizzaTheme.colors.brand else PizzaTheme.colors.surfaceMuted
            )
            .bouncyClick(enabled = enabled, pressedScale = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (filled) PizzaTheme.colors.onBrand else PizzaTheme.colors.textPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun TrackingBottomBar(
    isDelivered: Boolean,
    onRate: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PizzaTheme.colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PizzaTheme.colors.hairline)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Space.gutter, vertical = Space.md),
            horizontalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            if (isDelivered) {
                Box(modifier = Modifier.weight(1f)) {
                    SecondaryButton(label = "Back to home", onClick = onHome)
                }
                Box(modifier = Modifier.weight(1f)) {
                    PrimaryActionButton(
                        label = "Rate order",
                        onClick = onRate,
                        trailingIcon = Icons.Default.Star
                    )
                }
            } else {
                SecondaryButton(label = "Back to home", onClick = onHome)
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(PizzaTheme.colors.surfaceMuted)
            .bouncyClick(pressedScale = 0.98f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = PizzaTheme.colors.textPrimary
        )
    }
}

@Composable
private fun RatingDialog(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How was it?",
                fontFamily = PlayfairFamily,
                style = MaterialTheme.typography.displaySmall,
                color = PizzaTheme.colors.textPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Rate your stone-baked pizza from Pizza 5 P's",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PizzaTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Space.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    (1..5).forEach { star ->
                        val isLit = star <= rating
                        // Lit stars grow a little, so the chosen score reads at a glance.
                        val scale by animateFloatAsState(
                            targetValue = if (isLit) 1.1f else 0.9f,
                            animationSpec = Motion.SpringBouncy,
                            label = "star$star"
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rate $star",
                            tint = if (isLit) PizzaTheme.colors.star else PizzaTheme.colors.border,
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .bouncyClick(pressedScale = 0.8f) { onRatingChange(star) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Text(
                text = "Submit",
                style = MaterialTheme.typography.labelLarge,
                color = PizzaTheme.colors.brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .bouncyClick(pressedScale = 0.9f, onClick = onSubmit)
                    .padding(horizontal = Space.md, vertical = Space.sm)
            )
        },
        dismissButton = {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = PizzaTheme.colors.textMuted,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .bouncyClick(pressedScale = 0.9f, onClick = onDismiss)
                    .padding(horizontal = Space.md, vertical = Space.sm)
            )
        },
        containerColor = PizzaTheme.colors.surface,
        shape = RoundedCornerShape(Radius.lg)
    )
}
