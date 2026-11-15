package com.pizza5ps.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCategory
import com.pizza5ps.app.ui.components.BestSellerPizzaRow
import com.pizza5ps.app.ui.components.BottomNavBar
import com.pizza5ps.app.ui.components.BottomNavTab
import com.pizza5ps.app.ui.components.CategoryChip
import com.pizza5ps.app.ui.components.FloatingCartBar
import com.pizza5ps.app.ui.components.HomeTopBar
import com.pizza5ps.app.ui.components.PopularPizzaCard
import com.pizza5ps.app.ui.components.SectionHeader
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.StaggeredEntry
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.warmShadow
import com.pizza5ps.app.viewmodel.PizzaViewModel
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pizza5ps.app.ui.components.AddressPickerDialog

@Composable
fun HomeScreen(
    viewModel: PizzaViewModel,
    onNavigateToDetails: (Pizza) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val billSummary by viewModel.billSummary.collectAsState()
    val favorites by viewModel.favoritePizzaIds.collectAsState()
    val account by viewModel.account.collectAsState()
    val address by viewModel.deliveryDestination.collectAsState()

    var showAddressPicker by remember { mutableStateOf(false) }

    val popularPizzas = viewModel.allPizzas.filter { it.isPopular }
    val bestSellers = viewModel.allPizzas.filter { it.isBestSeller }

    val listState = rememberLazyListState()
    // Frosts the header once content starts passing underneath it.
    val collapseFraction = if (listState.firstVisibleItemIndex > 0) {
        1f
    } else {
        (listState.firstVisibleItemScrollOffset / 140f).coerceIn(0f, 1f)
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onProfileClick = onNavigateToProfile,
                collapseFraction = collapseFraction,
                onLocationClick = { showAddressPicker = true },
                addressLabel = address.label,
                initials = account?.initials ?: "?"
            )
        },
        bottomBar = {
            Column {
                FloatingCartBar(
                    cartItems = cartItems,
                    totalAmount = billSummary.subtotal,
                    onViewCartClicked = onNavigateToCart
                )
                BottomNavBar(
                    currentRoute = BottomNavTab.HOME.route,
                    cartItemCount = cartItems.sumOf { it.quantity },
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.HOME -> {}
                            BottomNavTab.SEARCH -> onNavigateToSearch()
                            BottomNavTab.CART -> onNavigateToCart()
                            BottomNavTab.PROFILE -> onNavigateToProfile()
                        }
                    }
                )
            }
        },
        // Transparent so the app-wide ambient ground shows through.
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = Space.xxl)
        ) {
            item { Greeting(firstName = account?.firstName) }

            item { SearchEntryPoint(onClick = onNavigateToSearch) }

            item {
                PromoCarousel(
                    onOrderNow = { code ->
                        viewModel.applyCoupon(code)
                        popularPizzas.firstOrNull()?.let { pizza ->
                            viewModel.openPizzaDetail(pizza)
                            onNavigateToDetails(pizza)
                        }
                    }
                )
            }

            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.sm),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    contentPadding = PaddingValues(horizontal = Space.gutter)
                ) {
                    items(PizzaCategory.values()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onSelected = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Popular right now",
                    actionLabel = "See all (${popularPizzas.size})",
                    onActionClick = { viewModel.onCategorySelected(PizzaCategory.POPULAR) }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                    contentPadding = PaddingValues(
                        horizontal = Space.gutter,
                        vertical = Space.sm
                    )
                ) {
                    itemsIndexed(popularPizzas) { index, pizza ->
                        // Cards cascade in rather than all landing at once, which
                        // gives the row a direction to read along.
                        StaggeredEntry(index = index) {
                            PopularPizzaCard(
                                pizza = pizza,
                                isFavorite = favorites.contains(pizza.id),
                                onCardClick = {
                                    viewModel.openPizzaDetail(pizza)
                                    onNavigateToDetails(pizza)
                                },
                                onQuickAdd = { viewModel.quickAdd(pizza) },
                                onToggleFavorite = { viewModel.toggleFavorite(pizza.id) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Space.md))
                SectionHeader(title = "Chef's best sellers")
            }

            itemsIndexed(bestSellers) { index, pizza ->
                StaggeredEntry(index = index) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = Space.gutter,
                            vertical = Space.xs
                        )
                    ) {
                        BestSellerPizzaRow(
                            pizza = pizza,
                            onCardClick = {
                                viewModel.openPizzaDetail(pizza)
                                onNavigateToDetails(pizza)
                            },
                            onQuickAdd = { viewModel.quickAdd(pizza) }
                        )
                    }
                }
            }
        }
    }

    if (showAddressPicker) {
        AddressPickerDialog(
            addresses = viewModel.savedAddresses,
            selected = address,
            onSelect = { viewModel.selectDeliveryAddress(it) },
            onDismiss = { showAddressPicker = false }
        )
    }
}

@Composable
private fun Greeting(
    firstName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = Space.gutter, vertical = Space.sm)
    ) {
        Text(
            // Falls back to the generic greeting only if somehow not signed in.
            text = if (firstName != null) "Hello, $firstName" else "Hello, Pizza Lover",
            style = MaterialTheme.typography.titleMedium,
            color = PizzaTheme.colors.textMuted
        )
        Text(
            text = "What are you craving?",
            fontFamily = PlayfairFamily,
            style = MaterialTheme.typography.displayMedium,
            color = PizzaTheme.colors.textPrimary
        )
    }
}

/** Tapping anywhere on the field opens the dedicated search screen. */
@Composable
private fun SearchEntryPoint(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.md)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Space.gutter, vertical = Space.sm)
            .warmShadow(6.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .bouncyClick(pressedScale = 0.99f, onClick = onClick)
            .padding(horizontal = Space.lg, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = PizzaTheme.colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "Search pizza, toppings, cheesy crust…",
            style = MaterialTheme.typography.bodyLarge,
            color = PizzaTheme.colors.textMuted
        )
    }
}

private data class Promo(
    val kicker: String,
    val headline: String,
    val detail: String,
    val code: String
)

private val promos = listOf(
    Promo("PIZZA NIGHT", "40% OFF", "On every hand-tossed classic", "PIZZA40"),
    Promo("FIRST ORDER", "20% OFF", "New here? This one's on us", "PIZZA20"),
    Promo("FREE DELIVERY", "OVER ₹500", "No delivery fee on bigger orders", "PIZZA20")
)

/**
 * Auto-advancing promotional carousel.
 *
 * Three offers rotate through one slot instead of stacking three banners down the
 * page, which keeps the menu — the thing people came for — near the top. It advances
 * on its own so the other offers are discoverable without a swipe, and the dots make
 * it clear that more than one exists.
 */
@Composable
private fun PromoCarousel(
    onOrderNow: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { promos.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(4500)
            val next = (pagerState.currentPage + 1) % promos.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = modifier.padding(vertical = Space.sm)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Space.gutter),
            pageSpacing = Space.md
        ) { page ->
            PromoCard(promo = promos[page], onOrderNow = onOrderNow)
        }

        Spacer(modifier = Modifier.height(Space.md))

        // Page dots: the active one widens into a bar rather than only changing
        // colour, so position is readable at a glance.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            promos.indices.forEach { index ->
                val isActive = pagerState.currentPage == index
                val dotWidth by animateDpAsState(
                    targetValue = if (isActive) 22.dp else 7.dp,
                    animationSpec = Motion.gentleSpring(),
                    label = "promoDot"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = dotWidth, height = 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                PizzaTheme.colors.brand
                            } else {
                                PizzaTheme.colors.textMuted.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun PromoCard(
    promo: Promo,
    onOrderNow: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.xl)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(16.dp, shape, tint = PizzaTheme.colors.brand)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PizzaTheme.colors.brandDark,
                        PizzaTheme.colors.brand,
                        PizzaTheme.colors.accent
                    ),
                    start = Offset.Zero,
                    end = Offset(700f, 500f)
                )
            )
    ) {
        // Off-centre glow keeps the gradient from reading as a flat swatch.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PizzaTheme.colors.accentSoft.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(620f, 40f),
                        radius = 520f
                    )
                )
        )

        Column(modifier = Modifier.padding(Space.xl)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.onBrand.copy(alpha = 0.22f))
                    .padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = PizzaTheme.colors.onBrand,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = promo.kicker,
                    style = MaterialTheme.typography.labelSmall,
                    color = PizzaTheme.colors.onBrand
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            Text(
                text = promo.headline,
                fontFamily = PlayfairFamily,
                style = MaterialTheme.typography.displayLarge,
                color = PizzaTheme.colors.onBrand
            )
            Text(
                text = promo.detail,
                style = MaterialTheme.typography.bodyLarge,
                color = PizzaTheme.colors.onBrand.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(Space.lg))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.surface)
                    .bouncyClick(pressedScale = 0.94f) { onOrderNow(promo.code) }
                    .padding(horizontal = Space.lg, vertical = Space.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Text(
                    text = "Order with ${promo.code}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.brand
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PizzaTheme.colors.brand,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
