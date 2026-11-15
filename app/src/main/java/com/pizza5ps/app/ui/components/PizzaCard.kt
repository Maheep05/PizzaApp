package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.R
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.rememberHaptics
import com.pizza5ps.app.ui.theme.warmShadow
import kotlinx.coroutines.delay
import com.pizza5ps.app.ui.theme.PizzaTheme

@Composable
fun VegNonVegBadge(isVeg: Boolean, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = if (isVeg) R.drawable.ic_veg else R.drawable.ic_nonveg),
        contentDescription = if (isVeg) "Vegetarian" else "Non-Vegetarian",
        tint = Color.Unspecified,
        modifier = modifier.size(15.dp)
    )
}

/**
 * Favourite toggle: the heart springs past its final size and a ring of spokes flies
 * outward on the way in. Favouriting has no other visible consequence in the app, so
 * the animation *is* the confirmation that the tap registered.
 */
@Composable
private fun FavouriteButton(
    isFavourite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavourite) 1.15f else 1f,
        animationSpec = Motion.SpringBouncy,
        label = "heartScale"
    )
    val haptics = rememberHaptics()

    Box(
        modifier = modifier.size(30.dp),
        contentAlignment = Alignment.Center
    ) {
        FavouriteBurst(favourited = isFavourite, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(PizzaTheme.colors.surface.copy(alpha = 0.9f))
                .bouncyClick(pressedScale = 0.85f, hapticOnTap = false) {
                    haptics.confirm()
                    onToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                tint = if (isFavourite) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

/**
 * Quick-add control that morphs to a tick for a beat after being pressed.
 *
 * Quick-add fires from a horizontal carousel where the cart bar may be off-screen,
 * so without local feedback the tap appears to do nothing. Confirming in place, on
 * the control itself, is cheaper for the user to read than a toast.
 */
@Composable
private fun QuickAddButton(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    var justAdded by remember { mutableStateOf(false) }

    LaunchedEffect(justAdded) {
        if (justAdded) {
            delay(1000)
            justAdded = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (justAdded) PizzaTheme.colors.success else PizzaTheme.colors.brand)
            .bouncyClick(pressedScale = 0.85f) {
                justAdded = true
                onAdd()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = justAdded,
            transitionSpec = {
                (scaleIn(initialScale = 0.4f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.4f) + fadeOut())
            },
            label = "quickAdd"
        ) { added ->
            Icon(
                imageVector = if (added) Icons.Default.Check else Icons.Default.Add,
                contentDescription = if (added) "Added to cart" else "Add to cart",
                tint = PizzaTheme.colors.surface,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** Rating and prep-time strip, rendered with icons rather than glyph characters. */
@Composable
private fun MetaRow(
    pizza: Pizza,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xxs)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = PizzaTheme.colors.star,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "${pizza.rating}",
                style = MaterialTheme.typography.titleSmall,
                color = PizzaTheme.colors.textPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xxs)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Preparation time",
                tint = PizzaTheme.colors.textMuted,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${pizza.prepTimeMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )
        }
    }
}

@Composable
fun PopularPizzaCard(
    pizza: Pizza,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.lg)

    Column(
        modifier = modifier
            .width(182.dp)
            .warmShadow(10.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .bouncyClick(pressedScale = 0.97f, onClick = onCardClick)
            .padding(Space.md)
    ) {
        // Artwork sits on a warm radial pool so the pizza reads as lit from above.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(
                    Brush.radialGradient(listOf(PizzaTheme.colors.surface, PizzaTheme.colors.surfaceMuted))
                ),
            contentAlignment = Alignment.Center
        ) {
            PizzaBase(
                pizza = pizza,
                modifier = Modifier.size(104.dp)
            )

            VegNonVegBadge(
                isVeg = pizza.isVeg,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Space.sm)
            )

            FavouriteButton(
                isFavourite = isFavorite,
                onToggle = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Space.xs)
            )
        }

        Spacer(modifier = Modifier.height(Space.sm))

        MetaRow(pizza = pizza)

        Spacer(modifier = Modifier.height(Space.xs))

        Text(
            text = pizza.name,
            style = MaterialTheme.typography.titleLarge,
            color = PizzaTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = pizza.tagline,
            style = MaterialTheme.typography.bodySmall,
            color = PizzaTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(Space.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${pizza.basePrice}",
                style = MaterialTheme.typography.headlineSmall,
                color = PizzaTheme.colors.textPrimary
            )
            QuickAddButton(onAdd = onQuickAdd)
        }
    }
}

@Composable
fun BestSellerPizzaRow(
    pizza: Pizza,
    onCardClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(6.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .bouncyClick(pressedScale = 0.985f, onClick = onCardClick)
            .padding(Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(Brush.radialGradient(listOf(PizzaTheme.colors.surface, PizzaTheme.colors.surfaceMuted))),
            contentAlignment = Alignment.Center
        ) {
            PizzaBase(pizza = pizza, modifier = Modifier.size(64.dp))
            VegNonVegBadge(
                isVeg = pizza.isVeg,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Space.xs)
            )
        }

        Spacer(modifier = Modifier.width(Space.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pizza.name,
                style = MaterialTheme.typography.titleLarge,
                color = PizzaTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pizza.description,
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(Space.xs))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = PizzaTheme.colors.star,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${pizza.rating}",
                    style = MaterialTheme.typography.titleSmall,
                    color = PizzaTheme.colors.textPrimary
                )
                Text(
                    text = "(${pizza.reviewCount}+ reviews)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PizzaTheme.colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(Space.sm))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${pizza.basePrice}",
                style = MaterialTheme.typography.headlineSmall,
                color = PizzaTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(Space.sm))
            QuickAddButton(onAdd = onQuickAdd, size = 30.dp)
        }
    }
}
