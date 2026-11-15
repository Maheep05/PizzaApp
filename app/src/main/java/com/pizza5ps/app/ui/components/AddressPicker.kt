package com.pizza5ps.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pizza5ps.app.data.DeliveryGeo
import com.pizza5ps.app.data.DeliveryStop
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.warmShadow

/**
 * Delivery address chooser.
 *
 * Only the destination is selectable. The outlet is stated at the top as fixed
 * information rather than offered as a choice, because Pizza 5 P's is a single
 * pizzeria — every order comes out of the Indiranagar kitchen, and implying otherwise
 * with a second picker would be a lie about the business.
 */
@Composable
fun AddressPickerDialog(
    addresses: List<DeliveryStop>,
    selected: DeliveryStop,
    onSelect: (DeliveryStop) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.xl))
                .background(PizzaTheme.colors.surface)
                .padding(Space.xl)
        ) {
            Text(
                text = "Deliver to",
                style = MaterialTheme.typography.headlineSmall,
                color = PizzaTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(Space.xs))
            Text(
                text = "Pick where this order should go",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(Space.lg))

            OutletNotice()

            Spacer(modifier = Modifier.height(Space.lg))

            addresses.forEach { stop ->
                AddressRow(
                    stop = stop,
                    isSelected = stop.address == selected.address,
                    onClick = {
                        onSelect(stop)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(Space.sm))
            }

            Spacer(modifier = Modifier.height(Space.sm))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(PizzaTheme.colors.surfaceMuted)
                    .bouncyClick(pressedScale = 0.98f, onClick = onDismiss)
                    .padding(vertical = Space.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge,
                    color = PizzaTheme.colors.textPrimary
                )
            }
        }
    }
}

/** States the fixed outlet, so it is clearly information and not an option. */
@Composable
private fun OutletNotice(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(PizzaTheme.colors.brand.copy(alpha = 0.08f))
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            tint = PizzaTheme.colors.brand,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Space.md))
        Column {
            Text(
                text = "COOKED AT",
                style = MaterialTheme.typography.labelSmall,
                color = PizzaTheme.colors.brand
            )
            Text(
                text = DeliveryGeo.store.label,
                style = MaterialTheme.typography.titleSmall,
                color = PizzaTheme.colors.textPrimary
            )
            Text(
                text = "Our only kitchen — all orders leave from here",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun AddressRow(
    stop: DeliveryStop,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.md)
    val tickScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = Motion.SpringBouncy,
        label = "addressTick"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(if (isSelected) 6.dp else 0.dp, shape)
            .clip(shape)
            .background(
                if (isSelected) {
                    PizzaTheme.colors.brand.copy(alpha = 0.06f)
                } else {
                    PizzaTheme.colors.surface
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline,
                shape = shape
            )
            .bouncyClick(pressedScale = 0.985f, onClick = onClick)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) PizzaTheme.colors.brand else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = PizzaTheme.colors.onBrand,
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        scaleX = tickScale
                        scaleY = tickScale
                    }
            )
        }

        Spacer(modifier = Modifier.width(Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stop.label,
                style = MaterialTheme.typography.titleMedium,
                color = PizzaTheme.colors.textPrimary
            )
            Text(
                text = stop.address,
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
