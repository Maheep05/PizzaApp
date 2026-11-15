package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.OrderStatus
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space

/** One icon per stage, so the timeline is scannable without reading it. */
private val OrderStatus.icon: ImageVector
    get() = when (this) {
        OrderStatus.CONFIRMED -> Icons.AutoMirrored.Filled.ReceiptLong
        OrderStatus.PREPARING -> Icons.Default.LocalFireDepartment
        OrderStatus.OUT_FOR_DELIVERY -> Icons.Default.DeliveryDining
        OrderStatus.DELIVERED -> Icons.Default.Redeem
    }

/** Rough minutes-from-order for each stage, shown at the right of each row. */
private val OrderStatus.expectedAt: String
    get() = when (this) {
        OrderStatus.CONFIRMED -> "now"
        OrderStatus.PREPARING -> "~5 min"
        OrderStatus.OUT_FOR_DELIVERY -> "~12 min"
        OrderStatus.DELIVERED -> "~25 min"
    }

/**
 * Vertical order timeline.
 *
 * The connector between two stages *fills* rather than switching colour, so progress
 * reads as something advancing down the list instead of discrete lights turning on.
 * The active stage carries a pulse, which is the clearest way to distinguish "this is
 * happening now" from "this is done".
 */
@Composable
fun OrderTimelineStepper(
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val statuses = OrderStatus.values()

    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Space.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live timeline",
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaTheme.colors.textPrimary
                )
                Text(
                    text = "Step ${currentStatus.stepNumber} of ${statuses.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(Space.lg))

            statuses.forEachIndexed { index, status ->
                TimelineStepRow(
                    status = status,
                    currentStatus = currentStatus,
                    isLast = index == statuses.lastIndex
                )
            }
        }
    }
}

@Composable
private fun TimelineStepRow(
    status: OrderStatus,
    currentStatus: OrderStatus,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val isPassed = status.stepNumber < currentStatus.stepNumber
    val isCurrent = status == currentStatus
    val isPending = status.stepNumber > currentStatus.stepNumber

    val markerColor by animateColorAsState(
        targetValue = when {
            isPassed -> PizzaTheme.colors.success
            isCurrent -> PizzaTheme.colors.brand
            else -> PizzaTheme.colors.surfaceMuted
        },
        animationSpec = Motion.mediumTween(),
        label = "markerColor"
    )
    // The connector below a completed stage is full; below the active stage it is
    // half filled, which hints that this leg is under way.
    val connectorFill by animateFloatAsState(
        targetValue = when {
            isPassed -> 1f
            isCurrent -> 0.5f
            else -> 0f
        },
        animationSpec = Motion.gentleSpring(),
        label = "connectorFill"
    )
    val markerScale by animateFloatAsState(
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = Motion.SpringBouncy,
        label = "markerScale"
    )

    Row(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent) {
                    PulseRings(color = PizzaTheme.colors.brand, ringCount = 2)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer {
                            scaleX = markerScale
                            scaleY = markerScale
                        }
                        .clip(CircleShape)
                        .background(markerColor)
                        .border(
                            width = 1.dp,
                            color = if (isPending) PizzaTheme.colors.border else markerColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Default.Check else status.icon,
                        contentDescription = null,
                        tint = if (isPending) {
                            PizzaTheme.colors.textMuted
                        } else {
                            PizzaTheme.colors.onBrand
                        },
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            if (!isLast) {
                // Track, plus a fill that grows downward through it.
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.surfaceMuted)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(connectorFill)
                            .clip(RoundedCornerShape(Radius.pill))
                            .background(
                                if (isPassed) {
                                    PizzaTheme.colors.success
                                } else {
                                    PizzaTheme.colors.brand
                                }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(Space.md))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = Space.sm, bottom = if (isLast) 0.dp else Space.xl)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = status.title,
                    style = if (isCurrent) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    color = if (isPending) {
                        PizzaTheme.colors.textMuted
                    } else {
                        PizzaTheme.colors.textPrimary
                    }
                )
                Text(
                    text = status.expectedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = PizzaTheme.colors.textMuted
                )
            }
            Text(
                text = status.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPending) {
                    PizzaTheme.colors.textMuted.copy(alpha = 0.7f)
                } else {
                    PizzaTheme.colors.textMuted
                }
            )

            // Only the active stage carries a badge, so the eye lands on it first.
            if (isCurrent) {
                Spacer(modifier = Modifier.height(Space.sm))
                AnimatedContent(
                    targetState = status,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 2 } + fadeOut())
                    },
                    label = "activeBadge"
                ) { active ->
                    TagPill(
                        text = if (active == OrderStatus.DELIVERED) "Completed" else "In progress",
                        containerColor = if (active == OrderStatus.DELIVERED) {
                            PizzaTheme.colors.successSoft
                        } else {
                            PizzaTheme.colors.brand.copy(alpha = 0.12f)
                        },
                        contentColor = if (active == OrderStatus.DELIVERED) {
                            PizzaTheme.colors.success
                        } else {
                            PizzaTheme.colors.brand
                        }
                    )
                }
            }
        }
    }
}
