package com.pizza5ps.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.AvailableToppings
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import com.pizza5ps.app.model.Topping
import com.pizza5ps.app.ui.components.AnimatedRupees
import com.pizza5ps.app.ui.components.CircleIconButton
import com.pizza5ps.app.ui.components.DynamicPriceBar
import com.pizza5ps.app.ui.components.InteractivePizzaHero
import com.pizza5ps.app.ui.components.TagPill
import com.pizza5ps.app.ui.components.VegNonVegBadge
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.warmShadow
import com.pizza5ps.app.viewmodel.PizzaViewModel
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.text.style.TextOverflow
import com.pizza5ps.app.ui.components.PizzaSliceGlyph
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.foundation.layout.fillMaxHeight
import com.pizza5ps.app.data.NutritionBreakdown
import com.pizza5ps.app.data.NutritionCalculator
import com.pizza5ps.app.ui.components.NutritionChatDialog

/**
 * Product page.
 *
 * Built as a hero with a content sheet riding up over it, which is the layout
 * convention for product pages because it puts the thing being sold at the top of the
 * visual hierarchy and lets the controls scroll independently beneath.
 *
 * The customisation controls are wired back into the hero rather than only into the
 * price: choosing a bigger size grows the pizza, adding a topping drops it onto the
 * pizza, and removing one flips the pizza to throw it off. The customiser stops being
 * a form and becomes direct manipulation of the product.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PizzaDetailScreen(
    viewModel: PizzaViewModel,
    onBackClick: () -> Unit,
    onAddedToCart: () -> Unit
) {
    val pizza = viewModel.selectedPizzaForDetail.collectAsState().value ?: return
    val draftSize by viewModel.draftSize.collectAsState()
    val draftCrust by viewModel.draftCrust.collectAsState()
    val draftToppings by viewModel.draftToppings.collectAsState()
    val draftQuantity by viewModel.draftQuantity.collectAsState()
    val draftPrice by viewModel.draftPrice.collectAsState()
    val favorites by viewModel.favoritePizzaIds.collectAsState()

    val isFavourite = favorites.contains(pizza.id)
    val scroll = rememberScrollState()

    // Which customiser step is showing. Local to the screen rather than the ViewModel
    // because it is pure navigation state — nothing outside this page cares about it.
    var step by remember(pizza.id) { mutableStateOf(CustomiserStep.SIZE) }

    // Opening a product always starts at the top of that product. Without this the
    // saved scroll offset from a previous visit is restored, so the page could open
    // with the hero already collapsed and the title cut off.
    LaunchedEffect(pizza.id) {
        scroll.scrollTo(0)
    }

    // The hero is pinned and collapses as the customiser scrolls under it.
    //
    // This matters more than it looks: adding a topping drops it onto the pizza and
    // removing one flips the pizza to throw it off, and neither is worth animating if
    // the pizza has scrolled off screen by the time the customer reaches the topping
    // chips. Keeping a shrunken pizza pinned means every choice is visibly applied to
    // the product being bought.
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAssistantThinking by viewModel.isAssistantThinking.collectAsState()
    val isCloudAssistant by viewModel.isCloudAssistantEnabled.collectAsState()
    var showNutritionChat by remember { mutableStateOf(false) }

    // Recomputed whenever the build changes, so the panel and the chat always
    // describe the pizza as it stands right now.
    val nutrition = remember(pizza.id, draftSize, draftCrust, draftToppings) {
        NutritionCalculator.calculate(
            pizza = pizza,
            size = draftSize,
            crust = draftCrust,
            extras = draftToppings.toList()
        )
    }

    val collapse = (scroll.value / HERO_COLLAPSE_DISTANCE).coerceIn(0f, 1f)
    val heroHeight = lerp(HERO_MAX_HEIGHT, HERO_MIN_HEIGHT, collapse)

    Scaffold(
        bottomBar = {
            DynamicPriceBar(
                quantity = draftQuantity,
                totalPrice = draftPrice,
                onIncrement = { viewModel.incrementDraftQuantity() },
                onDecrement = { viewModel.decrementDraftQuantity() },
                onAddToCart = {
                    viewModel.addDraftToCart()
                    onAddedToCart()
                }
            )
        },
        containerColor = PizzaTheme.colors.ground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                // Reserves the hero's full height; the hero itself is drawn pinned
                // on top of this scrolling column.
                Spacer(modifier = Modifier.height(HERO_MAX_HEIGHT))

                // The sheet rides up over the hero's lower edge.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.gutter)
                ) {
                    Spacer(modifier = Modifier.height(Space.lg))

                    TitleBlock(pizza = pizza)

                    Spacer(modifier = Modifier.height(Space.lg))

                    CustomiserStepper(
                        step = step,
                        onStepChange = { step = it },
                        selectedSize = draftSize,
                        onSelectSize = { viewModel.selectDraftSize(it) },
                        selectedCrust = draftCrust,
                        onSelectCrust = { viewModel.selectDraftCrust(it) },
                        selectedToppings = draftToppings,
                        onToggleTopping = { viewModel.toggleDraftTopping(it) }
                    )

                    Spacer(modifier = Modifier.height(Space.xl))

                    NutritionCard(
                        breakdown = nutrition,
                        onAskAssistant = {
                            viewModel.startNutritionChat()
                            showNutritionChat = true
                        }
                    )

                    Spacer(modifier = Modifier.height(Space.lg))

                    PriceBreakdown(
                        pizza = pizza,
                        size = draftSize,
                        crust = draftCrust,
                        toppings = draftToppings.toList(),
                        quantity = draftQuantity
                    )

                    Spacer(modifier = Modifier.height(Space.xxl))
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
                }
            }

            CollapsingHero(
                pizza = pizza,
                selectedSize = draftSize,
                selectedToppings = draftToppings.toList(),
                collapse = collapse,
                height = heroHeight
            )

            // Floating controls stay pinned above the hero.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Space.lg, vertical = Space.md),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
                CircleIconButton(
                    icon = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                    onClick = { viewModel.toggleFavorite(pizza.id) },
                    tint = if (isFavourite) PizzaTheme.colors.brand else PizzaTheme.colors.textPrimary
                )
            }
        }
    }

    if (showNutritionChat) {
        NutritionChatDialog(
            pizzaName = pizza.name,
            breakdown = nutrition,
            messages = chatMessages,
            isThinking = isAssistantThinking,
            isCloudEnabled = isCloudAssistant,
            onSend = { viewModel.askNutritionQuestion(it) },
            onDismiss = { showNutritionChat = false }
        )
    }
}

/** How many slices each size is cut into. Bigger pizza, more slices. */
val PizzaSize.sliceCount: Int
    get() = when (this) {
        PizzaSize.SMALL -> 4
        PizzaSize.MEDIUM -> 6
        PizzaSize.LARGE -> 8
    }

/** Hero geometry. The collapsed height leaves room for a shrunken pizza plus its
 *  name, which is all that needs to stay pinned. */
private val HERO_MAX_HEIGHT = 330.dp
private val HERO_MIN_HEIGHT = 150.dp
private const val HERO_COLLAPSE_DISTANCE = 260f

/** Width the collapsed pizza keeps clear for the floating back button. */
private val BACK_BUTTON_CLEARANCE = 50.dp

/**
 * Pinned, collapsing hero.
 *
 * At rest it is a full-bleed gradient with the pizza centred. As the customiser
 * scrolls underneath, the panel shrinks and the pizza slides to the left and scales
 * down, ending as a compact bar with the pizza beside its name — so the product stays
 * on screen while its options are being chosen.
 */
@Composable
private fun CollapsingHero(
    pizza: Pizza,
    selectedSize: PizzaSize,
    selectedToppings: List<Topping>,
    collapse: Float,
    height: Dp,
    modifier: Modifier = Modifier
) {
    // Size choice physically resizes the pizza, so the control has a visible
    // consequence beyond a number changing.
    val expandedDiameter by animateDpAsState(
        targetValue = when (selectedSize) {
            PizzaSize.SMALL -> 176.dp
            PizzaSize.MEDIUM -> 202.dp
            PizzaSize.LARGE -> 228.dp
        },
        animationSpec = Motion.gentleSpring(),
        label = "pizzaDiameter"
    )
    val diameter = lerp(expandedDiameter, 84.dp, collapse)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = Radius.xxl, bottomEnd = Radius.xxl))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PizzaTheme.colors.brandDark,
                        PizzaTheme.colors.brand,
                        PizzaTheme.colors.accent
                    ),
                    start = Offset.Zero,
                    end = Offset(900f, 1400f)
                )
            )
    ) {
        // Off-centre warm glow gives the gradient a light source.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PizzaTheme.colors.accentSoft.copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        center = Offset(260f, 260f),
                        radius = 900f
                    )
                )
        )

        // Travel from centred to the left as the panel collapses, stopping clear
        // of the floating back button rather than sliding underneath it.
        val centredX = (maxWidth - diameter) / 2
        val collapsedX = Space.gutter + BACK_BUTTON_CLEARANCE
        val pizzaX = lerp(centredX, collapsedX, collapse)

        Box(
            modifier = Modifier
                .offset(x = pizzaX)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            // Plate behind the pizza, so it reads as served rather than floating.
            Box(
                modifier = Modifier
                    .size(diameter + lerp(30.dp, 10.dp, collapse))
                    .clip(CircleShape)
                    .background(PizzaTheme.colors.surface.copy(alpha = 0.16f))
            )
            Box(
                modifier = Modifier
                    .size(diameter)
                    .warmShadow(
                        elevation = lerp(24.dp, 8.dp, collapse),
                        shape = CircleShape,
                        tint = PizzaTheme.colors.brandDark
                    )
            )
            InteractivePizzaHero(
                pizza = pizza,
                selectedToppings = selectedToppings,
                diameter = diameter,
                sliceCount = selectedSize.sliceCount
            )
        }

        // Name and size, revealed beside the pizza only once collapsed — at full
        // height they would duplicate the title in the sheet just below.
        if (collapse > 0.35f) {
            val labelAlpha = ((collapse - 0.35f) / 0.65f).coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = pizzaX + diameter + Space.md, end = Space.gutter)
                    .graphicsLayer { alpha = labelAlpha }
            ) {
                Text(
                    text = pizza.name,
                    fontFamily = PlayfairFamily,
                    style = MaterialTheme.typography.displaySmall,
                    color = PizzaTheme.colors.onBrand,
                    maxLines = 1
                )
                Text(
                    text = "${selectedSize.label} • ${selectedSize.inches}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.onBrand.copy(alpha = 0.85f)
                )
            }
        }

        // Size read-out, only while expanded (the collapsed bar shows it above).
        if (collapse < 0.4f) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = Space.gutter, bottom = 46.dp)
                    .graphicsLayer { alpha = 1f - (collapse / 0.4f) }
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.onBrand.copy(alpha = 0.22f))
                    .padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedSize.label} • ${selectedSize.inches}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.onBrand
                )
            }
        }
    }
}

/**
 * Nutrition for the current build, with the way into the assistant.
 *
 * The numbers are on the page rather than only inside the chat: most people want the
 * calorie count, not a conversation, and making them ask for it would be hostile.
 * The chat is there for the questions a table cannot answer.
 */
@Composable
private fun NutritionCard(
    breakdown: NutritionBreakdown,
    onAskAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.lg)
    val whole = breakdown.whole

    Column(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(6.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .padding(Space.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NUTRITION",
                    style = MaterialTheme.typography.labelSmall,
                    color = PizzaTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${whole.calories}",
                        fontFamily = PlayfairFamily,
                        style = MaterialTheme.typography.displaySmall,
                        color = PizzaTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        text = "kcal · ${breakdown.perSlice.calories} a slice",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PizzaTheme.colors.textMuted,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.md))

        MacroBar(breakdown = breakdown)

        Spacer(modifier = Modifier.height(Space.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            MacroChip("Protein", "${whole.proteinGrams.toInt()}g", Modifier.weight(1f))
            MacroChip("Carbs", "${whole.carbsGrams.toInt()}g", Modifier.weight(1f))
            MacroChip("Fat", "${whole.fatGrams.toInt()}g", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(Space.lg))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(PizzaTheme.colors.brand.copy(alpha = 0.10f))
                .bouncyClick(pressedScale = 0.98f, onClick = onAskAssistant)
                .padding(vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PizzaTheme.colors.brand,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(Space.sm))
            Text(
                text = "Ask the nutrition assistant",
                style = MaterialTheme.typography.labelMedium,
                color = PizzaTheme.colors.brand
            )
        }
    }
}

/** Where the energy actually comes from, as one proportional bar. */
@Composable
private fun MacroBar(
    breakdown: NutritionBreakdown,
    modifier: Modifier = Modifier
) {
    val whole = breakdown.whole
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(Radius.pill))
            .background(PizzaTheme.colors.surfaceMuted)
    ) {
        if (whole.calories > 0) {
            Box(
                modifier = Modifier
                    .weight(whole.proteinShare.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(PizzaTheme.colors.success)
            )
            Box(
                modifier = Modifier
                    .weight(whole.carbShare.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(PizzaTheme.colors.accent)
            )
            Box(
                modifier = Modifier
                    .weight(whole.fatShare.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(PizzaTheme.colors.brand)
            )
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(PizzaTheme.colors.surfaceMuted)
            .padding(horizontal = Space.sm, vertical = Space.xs),
        horizontalArrangement = Arrangement.spacedBy(Space.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PizzaTheme.colors.textMuted,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = PizzaTheme.colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun TitleBlock(
    pizza: Pizza,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            VegNonVegBadge(isVeg = pizza.isVeg)
            Text(
                text = if (pizza.isVeg) "PURE VEG" else "CONTAINS MEAT",
                style = MaterialTheme.typography.labelSmall,
                color = if (pizza.isVeg) PizzaTheme.colors.success else PizzaTheme.colors.brand
            )
        }

        Spacer(modifier = Modifier.height(Space.sm))

        Text(
            text = pizza.name,
            fontFamily = PlayfairFamily,
            style = MaterialTheme.typography.displayMedium,
            color = PizzaTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(Space.sm))

        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
            TagPill(
                text = "${pizza.rating} (${pizza.reviewCount}+)",
                leadingIcon = Icons.Default.Star,
                containerColor = PizzaTheme.colors.star.copy(alpha = 0.16f),
                contentColor = PizzaTheme.colors.textPrimary
            )
            TagPill(
                text = "${pizza.prepTimeMinutes} min",
                leadingIcon = Icons.Default.Schedule,
                containerColor = PizzaTheme.colors.textMuted.copy(alpha = 0.12f),
                contentColor = PizzaTheme.colors.textMuted
            )
            if (pizza.isBestSeller) {
                TagPill(
                    text = "Best seller",
                    leadingIcon = Icons.Default.LocalFireDepartment,
                    containerColor = PizzaTheme.colors.brand.copy(alpha = 0.10f),
                    contentColor = PizzaTheme.colors.brand
                )
            }
        }

        Spacer(modifier = Modifier.height(Space.sm))

        Text(
            text = pizza.description,
            style = MaterialTheme.typography.bodyMedium,
            color = PizzaTheme.colors.textMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** The three ordered decisions that build a pizza. */
enum class CustomiserStep(val label: String, val title: String, val hint: String) {
    SIZE("Size", "Choose your size", "Bigger pizzas come cut into more slices"),
    CRUST("Crust", "Pick a crust", "Every base is stretched and tossed to order"),
    TOPPINGS("Toppings", "Load it up", "Tap to drop a topping onto your pizza — all free")
}

/**
 * The customiser as a three-step form.
 *
 * Showing one decision at a time is what makes the page usable: stacked, all three
 * selectors ran well past the fold, so the topping chips — the step with the most
 * interesting feedback attached — could only be reached by scrolling away from the
 * pizza they affect. One step at a time keeps the whole decision beside the pinned
 * pizza, and the numbered header makes the remaining work obvious.
 *
 * Steps stay tappable rather than being locked behind the one before, because every
 * step already has a sensible default. Nothing needs to be answered in order, so
 * forcing it would only add friction.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomiserStepper(
    step: CustomiserStep,
    onStepChange: (CustomiserStep) -> Unit,
    selectedSize: PizzaSize,
    onSelectSize: (PizzaSize) -> Unit,
    selectedCrust: PizzaCrust,
    onSelectCrust: (PizzaCrust) -> Unit,
    selectedToppings: Set<Topping>,
    onToggleTopping: (Topping) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = CustomiserStep.values()
    val shape = RoundedCornerShape(Radius.lg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(8.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .padding(Space.lg)
    ) {
        StepIndicator(
            steps = steps,
            current = step,
            onStepClick = onStepChange
        )

        Spacer(modifier = Modifier.height(Space.lg))

        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = PizzaTheme.colors.textPrimary
        )
        Text(
            text = step.hint,
            style = MaterialTheme.typography.bodySmall,
            color = PizzaTheme.colors.textMuted
        )

        Spacer(modifier = Modifier.height(Space.lg))

        // Steps slide in the direction of travel, so moving forward and going back
        // feel like different actions rather than the same crossfade twice.
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val direction = if (forward) 1 else -1
                (slideInHorizontally(Motion.mediumTween()) { width -> direction * width / 3 } +
                    fadeIn(Motion.mediumTween())) togetherWith
                    (slideOutHorizontally(Motion.mediumTween()) { width -> -direction * width / 3 } +
                        fadeOut(Motion.fastTween()))
            },
            label = "customiserStep"
        ) { current ->
            when (current) {
                CustomiserStep.SIZE -> SizeSelector(
                    selected = selectedSize,
                    onSelect = onSelectSize
                )

                CustomiserStep.CRUST -> CrustSelector(
                    selected = selectedCrust,
                    onSelect = onSelectCrust
                )

                CustomiserStep.TOPPINGS -> FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    AvailableToppings.all.forEach { topping ->
                        ToppingChip(
                            topping = topping,
                            isSelected = selectedToppings.contains(topping),
                            onToggle = { onToggleTopping(topping) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Space.lg))

        StepNavigation(
            step = step,
            steps = steps,
            onStepChange = onStepChange
        )
    }
}

/**
 * Numbered step header. Completed steps collapse to a tick so the eye lands on the
 * one still open, and the connector fills as the form progresses.
 */
@Composable
private fun StepIndicator(
    steps: Array<CustomiserStep>,
    current: CustomiserStep,
    onStepClick: (CustomiserStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, entry ->
            val isCurrent = entry == current
            val isDone = entry.ordinal < current.ordinal

            val markerColor by animateColorAsState(
                targetValue = when {
                    isCurrent -> PizzaTheme.colors.brand
                    isDone -> PizzaTheme.colors.success
                    else -> PizzaTheme.colors.surfaceMuted
                },
                animationSpec = Motion.mediumTween(),
                label = "stepMarker"
            )
            val markerScale by animateFloatAsState(
                targetValue = if (isCurrent) 1.1f else 1f,
                animationSpec = Motion.SpringBouncy,
                label = "stepMarkerScale"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .bouncyClick(pressedScale = 0.92f) { onStepClick(entry) }
                    .padding(Space.xxs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = markerScale
                            scaleY = markerScale
                        }
                        .clip(CircleShape)
                        .background(markerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PizzaTheme.colors.onBrand,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isCurrent) {
                                PizzaTheme.colors.onBrand
                            } else {
                                PizzaTheme.colors.textMuted
                            }
                        )
                    }
                }

                // Only the open step carries its label; three labels at once would
                // crowd the row and flatten the hierarchy.
                if (isCurrent) {
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = PizzaTheme.colors.brand
                    )
                }
            }

            if (index != steps.lastIndex) {
                val fill by animateFloatAsState(
                    targetValue = if (entry.ordinal < current.ordinal) 1f else 0f,
                    animationSpec = Motion.gentleSpring(),
                    label = "stepConnector"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.surfaceMuted)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fill)
                            .height(2.dp)
                            .background(PizzaTheme.colors.success)
                    )
                }
            }
        }
    }
}

/**
 * Back / Next within the form. Deliberately secondary in weight: the page's one
 * primary action is Add to cart in the sticky footer, which stays reachable at every
 * step so a customer happy with the defaults never has to walk the form at all.
 */
@Composable
private fun StepNavigation(
    step: CustomiserStep,
    steps: Array<CustomiserStep>,
    onStepChange: (CustomiserStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step.ordinal > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.surfaceMuted)
                    .bouncyClick(pressedScale = 0.94f) {
                        onStepChange(steps[step.ordinal - 1])
                    }
                    .padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = PizzaTheme.colors.textMuted,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = steps[step.ordinal - 1].label,
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (step.ordinal < steps.lastIndex) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.brand.copy(alpha = 0.12f))
                    .bouncyClick(pressedScale = 0.94f) {
                        onStepChange(steps[step.ordinal + 1])
                    }
                    .padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                Text(
                    text = "Next: ${steps[step.ordinal + 1].label}",
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
        } else {
            Text(
                text = "All set — add it to your cart",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted
            )
        }
    }
}

/** Three size cards, each showing a proportionally sized disc. */
@Composable
private fun SizeSelector(
    selected: PizzaSize,
    onSelect: (PizzaSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.md)
    ) {
        PizzaSize.values().forEach { size ->
            val isSelected = selected == size
            val container by animateColorAsState(
                targetValue = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.surface,
                animationSpec = Motion.mediumTween(),
                label = "sizeContainer"
            )
            val content by animateColorAsState(
                targetValue = if (isSelected) PizzaTheme.colors.surface else PizzaTheme.colors.textPrimary,
                animationSpec = Motion.mediumTween(),
                label = "sizeContent"
            )
            val lift by animateFloatAsState(
                targetValue = if (isSelected) -4f else 0f,
                animationSpec = Motion.SpringSnappy,
                label = "sizeLift"
            )
            val shape = RoundedCornerShape(Radius.md)
            // A real sliced dough disc at true relative scale, so the 8" / 10" / 12"
            // difference is something the customer can see rather than only read.
            val discSize = when (size) {
                PizzaSize.SMALL -> 34.dp
                PizzaSize.MEDIUM -> 43.dp
                PizzaSize.LARGE -> 52.dp
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationY = lift }
                    .warmShadow(if (isSelected) 12.dp else 4.dp, shape, tint = PizzaTheme.colors.brand)
                    .clip(shape)
                    .background(container)
                    .border(1.dp, if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline, shape)
                    .bouncyClick(pressedScale = 0.95f) { onSelect(size) }
                    .padding(vertical = Space.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.height(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PizzaSliceGlyph(
                        modifier = Modifier.size(discSize),
                        slices = size.sliceCount
                    )
                }
                Spacer(modifier = Modifier.height(Space.xs))
                Text(
                    text = size.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = content
                )
                Text(
                    text = size.inches,
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = "${size.sliceCount} slices",
                    style = MaterialTheme.typography.bodySmall,
                    color = content.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(Space.xxs))
                Text(
                    text = if (size.priceOffset > 0) "+₹${size.priceOffset}" else "Included",
                    style = MaterialTheme.typography.labelMedium,
                    color = content
                )
            }
        }
    }
}

/** Crust rows with an animated tick rather than a stock radio button. */
@Composable
private fun CrustSelector(
    selected: PizzaCrust,
    onSelect: (PizzaCrust) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        PizzaCrust.values().forEach { crust ->
            val isSelected = selected == crust
            val shape = RoundedCornerShape(Radius.md)
            val tickScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = Motion.SpringBouncy,
                label = "crustTick"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .warmShadow(if (isSelected) 8.dp else 2.dp, shape)
                    .clip(shape)
                    .background(if (isSelected) PizzaTheme.colors.brand.copy(alpha = 0.06f) else PizzaTheme.colors.surface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline,
                        shape = shape
                    )
                    .bouncyClick(pressedScale = 0.985f) { onSelect(crust) }
                    .padding(horizontal = Space.md, vertical = Space.md),
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
                        tint = PizzaTheme.colors.surface,
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
                        text = crust.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = PizzaTheme.colors.textPrimary
                    )
                    Text(
                        text = crust.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.textMuted
                    )
                }

                Text(
                    text = if (crust.priceOffset > 0) "+₹${crust.priceOffset}" else "Included",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun ToppingChip(
    topping: Topping,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.pill)
    val container by animateColorAsState(
        targetValue = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.surface,
        animationSpec = Motion.mediumTween(),
        label = "toppingContainer"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) PizzaTheme.colors.surface else PizzaTheme.colors.textPrimary,
        animationSpec = Motion.mediumTween(),
        label = "toppingContent"
    )

    Row(
        modifier = modifier
            .warmShadow(if (isSelected) 8.dp else 2.dp, shape, tint = PizzaTheme.colors.brand)
            .clip(shape)
            .background(container)
            .border(1.dp, if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.hairline, shape)
            .bouncyClick(pressedScale = 0.93f, onClick = onToggle)
            .padding(horizontal = Space.sm, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.xxs)
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = topping.shortName,
            style = MaterialTheme.typography.titleSmall,
            color = content
        )
        // A price only appears if there is one to show; the chips stay narrow enough
        // for two per row while every topping is complimentary.
        if (topping.price > 0) {
            Text(
                text = "+₹${topping.price}",
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Itemised running total.
 *
 * The bottom bar shows only a final figure, which leaves the customer guessing which
 * choice moved it. Breaking the price into its parts makes the cost of each option
 * explicit, and the total rolls so the direction of change is visible.
 */
@Composable
private fun PriceBreakdown(
    pizza: Pizza,
    size: PizzaSize,
    crust: PizzaCrust,
    toppings: List<Topping>,
    quantity: Int,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radius.lg)
    val toppingsTotal = toppings.sumOf { it.price }
    val unitPrice = pizza.basePrice + size.priceOffset + crust.priceOffset + toppingsTotal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .warmShadow(6.dp, shape)
            .clip(shape)
            .background(PizzaTheme.colors.surface)
            .border(1.dp, PizzaTheme.colors.hairline, shape)
            .padding(Space.lg)
    ) {
        Text(
            text = "YOUR BUILD",
            style = MaterialTheme.typography.labelSmall,
            color = PizzaTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(Space.md))

        BreakdownRow(label = "${pizza.name} (base)", amount = "₹${pizza.basePrice}")
        if (size.priceOffset > 0) {
            BreakdownRow(label = "${size.label} size", amount = "+₹${size.priceOffset}")
        }
        if (crust.priceOffset > 0) {
            BreakdownRow(label = crust.label, amount = "+₹${crust.priceOffset}")
        }
        toppings.forEach { topping ->
            BreakdownRow(
                label = topping.name,
                amount = if (topping.price > 0) "+₹${topping.price}" else "Free"
            )
        }
        if (quantity > 1) {
            BreakdownRow(label = "Quantity ×$quantity", amount = "₹$unitPrice each")
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleLarge,
                color = PizzaTheme.colors.textPrimary
            )
            AnimatedRupees(
                amount = unitPrice * quantity,
                style = MaterialTheme.typography.headlineMedium,
                color = PizzaTheme.colors.brand
            )
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.xxs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PizzaTheme.colors.textMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            color = PizzaTheme.colors.textPrimary,
            textAlign = TextAlign.End
        )
    }
}
