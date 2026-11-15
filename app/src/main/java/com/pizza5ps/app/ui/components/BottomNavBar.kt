package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick

enum class BottomNavTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("search", "Search", Icons.Filled.Search, Icons.Outlined.Search),
    CART("cart", "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Bottom navigation.
 *
 * A single pill slides between tabs rather than each tab fading its own highlight in
 * and out. One moving object is easier to follow than four cross-fading ones, and the
 * travel itself shows the relationship between where you were and where you landed.
 *
 * The bar carries [navigationBarsPadding] because the app draws edge to edge; without
 * it the tabs sit underneath the system gesture bar.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    cartItemCount: Int,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = BottomNavTab.values()
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Space.sm, vertical = Space.sm)
        ) {
            // Tabs share the width equally, so the pill's position is simply the
            // selected index times one slot — no coordinate plumbing needed.
            val slotWidth = maxWidth / tabs.size
            val pillOffset by animateDpAsState(
                targetValue = slotWidth * selectedIndex,
                animationSpec = Motion.gentleSpring(),
                label = "navPillOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .size(width = slotWidth, height = 52.dp)
                    .padding(horizontal = Space.sm)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PizzaTheme.colors.brand.copy(alpha = 0.16f),
                                PizzaTheme.colors.brand.copy(alpha = 0.06f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    NavTab(
                        tab = tab,
                        isSelected = tab.route == currentRoute,
                        badgeCount = if (tab == BottomNavTab.CART) cartItemCount else 0,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.size(width = slotWidth, height = 52.dp)
                    )
                }
            }
        }
    }
}

/**
 * Cart count badge. Springs in on the first item and its number rolls thereafter, so
 * adding to the cart is acknowledged even when the cart tab is not the active one.
 *
 * Kept as its own composable so [AnimatedVisibility] resolves against a single
 * unambiguous layout scope.
 */
@Composable
private fun CartBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(Motion.bouncySpring()) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .clip(CircleShape)
                .background(PizzaTheme.colors.brand),
            contentAlignment = Alignment.Center
        ) {
            AnimatedCount(
                count = count.coerceAtMost(9),
                style = MaterialTheme.typography.labelSmall,
                color = PizzaTheme.colors.onBrand
            )
        }
    }
}

@Composable
private fun NavTab(
    tab: BottomNavTab,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted,
        animationSpec = Motion.mediumTween(),
        label = "tabColor"
    )
    // The selected icon grows very slightly — enough to feel active without
    // disturbing the row's rhythm.
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = Motion.SpringBouncy,
        label = "tabIconScale"
    )

    Box(
        modifier = modifier.bouncyClick(pressedScale = 0.9f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.title,
                    tint = contentColor,
                    modifier = Modifier
                        .size(23.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )

                CartBadge(
                    count = badgeCount,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 9.dp, y = (-6).dp)
                )
            }

            Text(
                text = tab.title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}
