package com.pizza5ps.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.PizzaCategory
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.warmShadow

/** An icon per filter, so chips are scannable by shape as well as by label. */
private val PizzaCategory.icon: ImageVector
    get() = when (this) {
        PizzaCategory.ALL -> Icons.Default.Restaurant
        PizzaCategory.POPULAR -> Icons.AutoMirrored.Filled.TrendingUp
        PizzaCategory.VEG -> Icons.Default.Eco
        PizzaCategory.NON_VEG -> Icons.Default.SetMeal
        PizzaCategory.CHEESY -> Icons.Default.LocalPizza
        PizzaCategory.SPICY -> Icons.Default.LocalFireDepartment
    }

/**
 * Filter chip.
 *
 * Selection is signalled on four channels at once — fill, text colour, elevation and
 * a slight lift — because a single cue (colour alone) fails for anyone who cannot
 * distinguish the two hues. Everything crossfades on the shared motion tokens so a
 * row of chips settles together rather than flickering.
 */
@Composable
fun CategoryChip(
    category: PizzaCategory,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PizzaTheme.colors.surface else PizzaTheme.colors.textPrimary,
        animationSpec = Motion.mediumTween(),
        label = "chipContent"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PizzaTheme.colors.surface else PizzaTheme.colors.brand,
        animationSpec = Motion.mediumTween(),
        label = "chipIcon"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 10.dp else 0.dp,
        animationSpec = Motion.mediumTween(),
        label = "chipElevation"
    )
    // Selected chips rise a couple of pixels — a physical cue that reads instantly.
    val lift by animateFloatAsState(
        targetValue = if (isSelected) -2f else 0f,
        animationSpec = Motion.SpringSnappy,
        label = "chipLift"
    )
    val shape = RoundedCornerShape(Radius.pill)

    Row(
        modifier = modifier
            .graphicsLayer { translationY = lift }
            .warmShadow(elevation, shape, tint = PizzaTheme.colors.brand)
            .clip(shape)
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(listOf(PizzaTheme.colors.brand, PizzaTheme.colors.accent))
                } else {
                    Brush.horizontalGradient(listOf(PizzaTheme.colors.surface, PizzaTheme.colors.surface))
                }
            )
            .border(1.dp, if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline, shape)
            .bouncyClick(pressedScale = 0.94f, onClick = onSelected)
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xs)
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor
        )
    }
}
