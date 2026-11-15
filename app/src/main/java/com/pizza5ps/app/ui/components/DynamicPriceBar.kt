package com.pizza5ps.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.brandGlow
import com.pizza5ps.app.ui.theme.warmShadow

/**
 * Sticky customiser footer: quantity on the left, the live total on the right.
 *
 * The total rolls with [AnimatedRupees] rather than cutting, so a change made higher
 * up the page announces itself down here even when the control that caused it has
 * scrolled out of view.
 */
@Composable
fun DynamicPriceBar(
    quantity: Int,
    totalPrice: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onAddToCart: () -> Unit,
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
                .padding(horizontal = Space.lg, vertical = Space.md),
            horizontalArrangement = Arrangement.spacedBy(Space.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuantityStepper(
                quantity = quantity,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )

            val shape = RoundedCornerShape(Radius.md)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .brandGlow(elevation = 14.dp, shape = shape)
                    .clip(shape)
                    .background(Brush.horizontalGradient(listOf(PizzaTheme.colors.brand, PizzaTheme.colors.brandDark)))
                    .bouncyClick(pressedScale = 0.975f, onClick = onAddToCart)
                    .padding(horizontal = Space.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = PizzaTheme.colors.surface,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add to cart",
                        style = MaterialTheme.typography.labelLarge,
                        color = PizzaTheme.colors.surface
                    )
                }
                AnimatedRupees(
                    amount = totalPrice,
                    style = MaterialTheme.typography.headlineSmall,
                    color = PizzaTheme.colors.surface
                )
            }
        }
    }
}

/** Shared quantity control: minus, a rolling count, plus. */
@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.md)
    Row(
        modifier = modifier
            .warmShadow(2.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surfaceMuted)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .padding(Space.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = Icons.Default.Remove,
            contentDescription = "Decrease quantity",
            onClick = onDecrement
        )
        Box(
            modifier = Modifier.width(30.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedCount(
                count = quantity,
                style = MaterialTheme.typography.titleLarge,
                color = PizzaTheme.colors.textPrimary
            )
        }
        StepperButton(
            icon = Icons.Default.Add,
            contentDescription = "Increase quantity",
            onClick = onIncrement,
            filled = true
        )
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(Radius.xs))
            .background(if (filled) PizzaTheme.colors.brand else PizzaTheme.colors.surface)
            .bouncyClick(pressedScale = 0.85f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) PizzaTheme.colors.surface else PizzaTheme.colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
