package com.pizza5ps.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.Topping
import com.pizza5ps.app.ui.theme.BasilLeaf
import com.pizza5ps.app.ui.theme.CapsicumGreen
import com.pizza5ps.app.ui.theme.CheesePale
import com.pizza5ps.app.ui.theme.CheeseYellow
import com.pizza5ps.app.ui.theme.ChickenBrown
import com.pizza5ps.app.ui.theme.CornYellow
import com.pizza5ps.app.ui.theme.CrustEdge
import com.pizza5ps.app.ui.theme.CrustEdgeDeep
import com.pizza5ps.app.ui.theme.DoughBase
import com.pizza5ps.app.ui.theme.DoughShade
import com.pizza5ps.app.ui.theme.CaramelisedOnion
import com.pizza5ps.app.ui.theme.GarlicCream
import com.pizza5ps.app.ui.theme.GarlicToasted
import com.pizza5ps.app.ui.theme.SundriedTomato
import com.pizza5ps.app.ui.theme.SundriedTomatoDeep
import com.pizza5ps.app.ui.theme.JalapenoGreen
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.theme.MushroomTan
import com.pizza5ps.app.ui.theme.OliveDark
import com.pizza5ps.app.ui.theme.OnionPurple
import com.pizza5ps.app.ui.theme.PaneerCream
import com.pizza5ps.app.ui.theme.PepperoniDeep
import com.pizza5ps.app.ui.theme.PepperoniRed
import com.pizza5ps.app.ui.theme.SauceRed
import com.pizza5ps.app.ui.theme.SauceRedDeep
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Every pizza in the app is drawn, not photographed.
 *
 * A single circular dough base is rendered on a Canvas — baked crust ring, oven char,
 * sauce, molten cheese and slice cuts — and *everything else is a topping*. Each
 * pizza's signature toppings are what identify it on the menu, and the customer's
 * extras drop onto that same base on the product page.
 *
 * The reason for drawing rather than shooting: a photograph is fixed. It cannot show
 * the pizza the customer is actually building, so extras have to be composited over
 * someone else's pizza and never quite line up. Drawing the base means the artwork
 * and the order are the same object — toppings land on the real product, the disc
 * scales to any size, it costs nothing to download, and it sits on the app's own
 * palette under one consistent light direction.
 */

private enum class Sprite {
    CHEESE, JALAPENO, MUSHROOM, CORN, PANEER, OLIVE, CAPSICUM, CHICKEN,
    BASIL, PEPPERONI, TOMATO, ONION, CHILLI,
    GARLIC, SUNDRIED_TOMATO, CARAMELISED_ONION
}

/** The base sauce differs by pizza: red marinara, white Alfredo, or smoky BBQ. */
private enum class SauceStyle { MARINARA, ALFREDO, BBQ }

/** What a pizza looks like before the customer adds anything. */
private data class Recipe(
    val sprites: List<Sprite>,
    val sauce: SauceStyle = SauceStyle.MARINARA,
    val count: Int = 11
)

/** Each pizza's signature toppings mirror its menu description. */
private val signatureRecipes: Map<String, Recipe> = mapOf(
    "p_margherita" to Recipe(
        listOf(Sprite.BASIL, Sprite.TOMATO, Sprite.CHEESE),
        count = 9
    ),
    "p_farmhouse" to Recipe(
        listOf(Sprite.CAPSICUM, Sprite.ONION, Sprite.MUSHROOM, Sprite.TOMATO),
        count = 13
    ),
    "p_pepperoni" to Recipe(
        listOf(Sprite.PEPPERONI, Sprite.PEPPERONI, Sprite.CHEESE),
        count = 11
    ),
    "p_paneer_tikka" to Recipe(
        listOf(Sprite.PANEER, Sprite.CAPSICUM, Sprite.CHILLI),
        count = 12
    ),
    "p_bbq_chicken" to Recipe(
        listOf(Sprite.CHICKEN, Sprite.ONION, Sprite.CHEESE),
        sauce = SauceStyle.BBQ,
        count = 12
    ),
    "p_veggie_supreme" to Recipe(
        listOf(Sprite.OLIVE, Sprite.CORN, Sprite.CAPSICUM, Sprite.JALAPENO),
        count = 14
    ),
    "p_four_cheese" to Recipe(
        listOf(Sprite.CHEESE, Sprite.CHEESE, Sprite.BASIL),
        sauce = SauceStyle.ALFREDO,
        count = 10
    ),
    "p_fiery_peri_peri" to Recipe(
        listOf(Sprite.CHICKEN, Sprite.JALAPENO, Sprite.CHILLI),
        count = 13
    ),
    "p_ys_special" to Recipe(
        listOf(
            Sprite.BASIL,
            Sprite.GARLIC,
            Sprite.SUNDRIED_TOMATO,
            Sprite.MUSHROOM,
            Sprite.CARAMELISED_ONION
        ),
        count = 15
    )
)

private val fallbackRecipe = Recipe(listOf(Sprite.CHEESE, Sprite.TOMATO, Sprite.BASIL))

private fun spriteFor(toppingId: String): Sprite = when (toppingId) {
    "top_cheese" -> Sprite.CHEESE
    "top_jalapeno" -> Sprite.JALAPENO
    "top_mushroom" -> Sprite.MUSHROOM
    "top_corn" -> Sprite.CORN
    "top_paneer" -> Sprite.PANEER
    "top_olives" -> Sprite.OLIVE
    "top_capsicum" -> Sprite.CAPSICUM
    "top_chicken" -> Sprite.CHICKEN
    "top_basil" -> Sprite.BASIL
    "top_garlic" -> Sprite.GARLIC
    "top_sundried_tomato" -> Sprite.SUNDRIED_TOMATO
    // Sautéed and grilled mushrooms are the same shape, different preparation.
    "top_sauteed_mushroom" -> Sprite.MUSHROOM
    "top_caramelised_onion" -> Sprite.CARAMELISED_ONION
    else -> Sprite.BASIL
}

/** Where one topping piece sits, in fractions of the pizza's radius. */
private data class SpritePlacement(
    val sprite: Sprite,
    val angle: Float,
    val radiusFraction: Float,
    val sizeFraction: Float,
    val spin: Float
)

/**
 * Scatters [count] pieces across the disc, seeded from [seed].
 *
 * The seed gives every pizza and every topping its own angular phase. Without it two
 * topping sets would both start at 0° and step by the same amount, so they would pile
 * into identical positions instead of interleaving across the pizza.
 */
private fun scatter(
    seed: Int,
    sprites: List<Sprite>,
    count: Int,
    minSize: Float,
    maxSize: Float
): List<SpritePlacement> {
    val random = Random(seed)
    val phase = random.nextFloat() * 360f
    return List(count) { index ->
        val step = 360f / count
        SpritePlacement(
            sprite = sprites[index % sprites.size],
            angle = phase + index * step + random.nextFloat() * step * 0.5f,
            // Held inside the sauce so nothing lands on the bare crust ring.
            radiusFraction = 0.22f + random.nextFloat() * 0.44f,
            sizeFraction = minSize + random.nextFloat() * (maxSize - minSize),
            spin = random.nextFloat() * 360f
        )
    }
}

/** Irregular cheese-melt patches, so the bake looks uneven rather than printed. */
private fun meltPatches(seed: Int): List<Triple<Float, Float, Float>> {
    val random = Random(seed * 31)
    return List(7) {
        Triple(
            random.nextFloat() * 360f,
            0.16f + random.nextFloat() * 0.44f,
            0.11f + random.nextFloat() * 0.09f
        )
    }
}

/**
 * The pizza as it appears on the menu: dough base plus its signature toppings.
 *
 * [sliceCount] is how many ways the pizza is cut — a bigger pizza comes in more
 * slices, so the product page drives this from the selected size. [cutReveal] draws
 * the cuts partially, which lets the caller animate them being sliced outward from
 * the centre when the count changes.
 *
 * Static by default, so it is cheap enough for list thumbnails.
 */
@Composable
fun PizzaBase(
    pizza: Pizza,
    modifier: Modifier = Modifier,
    sliceCount: Int = 8,
    cutReveal: Float = 1f
) {
    val recipe = signatureRecipes[pizza.id] ?: fallbackRecipe
    val seed = remember(pizza.id) { pizza.id.hashCode() }
    val placements = remember(pizza.id) {
        scatter(seed, recipe.sprites, recipe.count, 0.095f, 0.135f)
    }
    val melts = remember(pizza.id) { meltPatches(seed) }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val centre = Offset(size.width / 2f, size.height / 2f)
        drawDoughBase(
            centre = centre,
            radius = radius,
            sauce = recipe.sauce,
            melts = melts,
            seed = seed,
            sliceCount = sliceCount,
            cutReveal = cutReveal
        )
        placements.forEach { placement ->
            drawSprite(
                sprite = placement.sprite,
                centre = pointOn(
                    centre,
                    radius * SAUCE_FRACTION * placement.radiusFraction,
                    placement.angle
                ),
                size = radius * placement.sizeFraction,
                spin = placement.spin
            )
        }
    }
}

/**
 * A plain dough disc with slice cuts, drawn at whatever size it is given.
 *
 * Used by the size selector, where three of these at true relative scale communicate
 * 8" / 10" / 12" far better than three coloured dots — the customer sees the actual
 * difference they are choosing between.
 */
@Composable
fun PizzaSliceGlyph(
    modifier: Modifier = Modifier,
    slices: Int = 6
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val centre = Offset(size.width / 2f, size.height / 2f)
        drawDoughBase(
            centre = centre,
            radius = radius,
            sauce = SauceStyle.MARINARA,
            melts = emptyList(),
            seed = 7,
            sliceCount = slices,
            drawChar = false
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Base rendering
// ─────────────────────────────────────────────────────────────────────────────

/** How far the sauce reaches, as a fraction of the disc's radius. */
private const val SAUCE_FRACTION = 0.78f

private fun DrawScope.drawDoughBase(
    centre: Offset,
    radius: Float,
    sauce: SauceStyle,
    melts: List<Triple<Float, Float, Float>>,
    seed: Int,
    sliceCount: Int = 8,
    drawChar: Boolean = true,
    cutReveal: Float = 1f
) {
    // Light comes from the upper-left, matching the app's ambient ground layer.
    val lightSource = Offset(centre.x - radius * 0.35f, centre.y - radius * 0.4f)

    // 1. Contact shadow, so the disc sits on the surface rather than floating.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(CrustEdgeDeep.copy(alpha = 0.24f), Color.Transparent),
            center = Offset(centre.x, centre.y + radius * 0.08f),
            radius = radius * 1.02f
        ),
        radius = radius,
        center = Offset(centre.x, centre.y + radius * 0.06f)
    )

    // 2. The dough itself — baked crust, lighter where the light strikes it.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(DoughBase, DoughShade, CrustEdge),
            center = lightSource,
            radius = radius * 1.5f
        ),
        radius = radius * 0.96f,
        center = centre
    )

    // 3. Oven char: irregular darker bites around the rim.
    if (drawChar) {
        val charRandom = Random(seed * 977)
        repeat(9) {
            drawCircle(
                color = CrustEdgeDeep.copy(alpha = 0.15f + charRandom.nextFloat() * 0.12f),
                radius = radius * (0.045f + charRandom.nextFloat() * 0.04f),
                center = pointOn(centre, radius * 0.90f, charRandom.nextFloat() * 360f)
            )
        }
    }

    // 4. Sauce, inset to leave a bare crust ring.
    val sauceRadius = radius * SAUCE_FRACTION
    drawCircle(
        brush = Brush.radialGradient(
            colors = when (sauce) {
                SauceStyle.MARINARA -> listOf(SauceRed, SauceRedDeep)
                SauceStyle.ALFREDO -> listOf(CheesePale, Color(0xFFE8CE98))
                SauceStyle.BBQ -> listOf(Color(0xFF8C4A2F), Color(0xFF5F2E1B))
            },
            center = lightSource,
            radius = radius * 1.3f
        ),
        radius = sauceRadius,
        center = centre
    )

    // 5. Molten cheese: a translucent wash plus irregular patches, so the sauce still
    //    shows through at the edges the way it does on a real bake.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(CheeseYellow.copy(alpha = 0.96f), CheeseYellow.copy(alpha = 0.74f)),
            center = lightSource,
            radius = radius * 1.25f
        ),
        radius = sauceRadius * 0.88f,
        center = centre
    )
    melts.forEach { (angle, radiusFraction, sizeFraction) ->
        drawCircle(
            color = CheesePale.copy(alpha = 0.30f),
            radius = radius * sizeFraction * 0.85f,
            center = pointOn(centre, sauceRadius * radiusFraction, angle)
        )
    }

    // 6. Raised crust rim: a soft light stroke just outside the sauce gives the bare
    //    dough edge some volume instead of leaving it flat.
    drawCircle(
        color = DoughBase.copy(alpha = 0.55f),
        radius = radius * 0.87f,
        center = centre,
        style = Stroke(width = radius * 0.07f)
    )

    // 7. Slice cuts, so the disc reads as a cut pizza rather than a flat circle.
    val reveal = cutReveal.coerceIn(0f, 1f)
    if (reveal > 0f) {
        repeat(sliceCount) { index ->
            val angle = index * (360f / sliceCount)
            drawLine(
                color = CrustEdgeDeep.copy(alpha = 0.18f * reveal),
                start = centre,
                // Each cut runs outward from the centre as the reveal progresses,
                // so a change of size reads as the pizza being re-sliced.
                end = pointOn(centre, sauceRadius * 0.98f * reveal, angle),
                strokeWidth = (radius * 0.018f).coerceAtLeast(1f)
            )
        }
    }

    // 8. A soft specular highlight sells the "just out of the oven" gloss.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
            center = lightSource,
            radius = radius * 0.55f
        ),
        radius = radius * 0.54f,
        center = lightSource
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated extras
// ─────────────────────────────────────────────────────────────────────────────

/**
 * One topping's worth of pieces, dropping onto the pizza.
 *
 * Each piece falls in from above the frame under a bouncy spring with its own short
 * stagger, so a topping arrives as a scatter of pieces rather than one rigid block
 * appearing. The spring's overshoot is what makes them look like they have weight.
 */
@Composable
private fun FallingToppingLayer(
    topping: Topping,
    piecesPerTopping: Int = 5,
    modifier: Modifier = Modifier
) {
    val sprite = remember(topping.id) { spriteFor(topping.id) }
    val placements = remember(topping.id) {
        scatter(topping.id.hashCode(), listOf(sprite), piecesPerTopping, 0.062f, 0.084f)
    }

    placements.forEachIndexed { index, placement ->
        key(topping.id, index) {
            val drop = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(index * 60L)
                drop.animateTo(1f, Motion.SpringBouncy)
            }

            Canvas(
                modifier = modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Falls in from above the pizza and fades up as it lands.
                        translationY = (1f - drop.value) * -size.height * 0.9f
                        alpha = drop.value.coerceIn(0f, 1f)
                        rotationZ = (1f - drop.value) * 40f
                    }
            ) {
                val radius = this.size.minDimension / 2f
                val centre = Offset(this.size.width / 2f, this.size.height / 2f)
                val position = pointOn(
                    centre,
                    radius * SAUCE_FRACTION * placement.radiusFraction,
                    placement.angle
                )
                val pieceSize = radius * placement.sizeFraction

                // A soft contact shadow sits the piece *on* the cheese instead of
                // leaving it hovering above the base.
                drawCircle(
                    color = Color.Black.copy(alpha = 0.20f),
                    radius = pieceSize * 1.05f,
                    center = Offset(
                        position.x + pieceSize * 0.12f,
                        position.y + pieceSize * 0.18f
                    )
                )
                drawSprite(placement.sprite, position, pieceSize, placement.spin)
            }
        }
    }
}

/**
 * The interactive product hero: the pizza's own base with the customer's extras
 * dropping onto it, and being flung off with a flip when removed.
 *
 * The flip is the mechanism that makes removal legible. When a topping is taken off,
 * the disc turns on its vertical axis and the pieces are swapped out at the halfway
 * point — the moment it is edge-on and effectively invisible — so the topping appears
 * thrown clear rather than blinking out of existence.
 */
@Composable
fun InteractivePizzaHero(
    pizza: Pizza,
    selectedToppings: List<Topping>,
    modifier: Modifier = Modifier,
    diameter: Dp = 260.dp,
    sliceCount: Int = 8
) {
    // Lags [selectedToppings] during a removal so the flip can hide the swap.
    var displayedToppings by remember { mutableStateOf(selectedToppings) }
    val flip = remember { Animatable(0f) }

    // Slice count is a whole number, so it cannot be tweened. Instead the cuts are
    // re-drawn outward from the centre whenever the count changes, which reads as the
    // pizza being re-sliced for the new size rather than the lines simply popping.
    val cutReveal = remember { Animatable(1f) }
    LaunchedEffect(sliceCount) {
        cutReveal.snapTo(0f)
        cutReveal.animateTo(1f, Motion.gentleSpring())
    }

    LaunchedEffect(selectedToppings) {
        val isRemoval = selectedToppings.size < displayedToppings.size
        if (isRemoval) {
            // Swapped at 90°, where the disc is edge-on. Swapping at 180° would look
            // wrong: the pizza is fully visible there (merely mirrored), and the
            // topping would visibly pop out of existence.
            flip.animateTo(90f, tween(170, easing = Motion.EaseStandard))
            displayedToppings = selectedToppings
            flip.animateTo(360f, tween(430, easing = Motion.EaseEmphasised))
            flip.snapTo(0f)
        } else {
            displayedToppings = selectedToppings
        }
    }

    Box(
        modifier = modifier
            .size(diameter)
            .graphicsLayer {
                rotationY = flip.value
                // Without a generous camera distance the perspective at 90° is
                // extreme enough to look like a glitch rather than a flip.
                cameraDistance = 16f * density
            },
        contentAlignment = Alignment.Center
    ) {
        PizzaBase(
            pizza = pizza,
            modifier = Modifier.fillMaxSize(),
            sliceCount = sliceCount,
            cutReveal = cutReveal.value
        )

        displayedToppings.forEach { topping ->
            key(topping.id) {
                FallingToppingLayer(topping = topping)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sprite drawing
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawSprite(
    sprite: Sprite,
    centre: Offset,
    size: Float,
    spin: Float
) {
    when (sprite) {
        Sprite.CHEESE -> {
            drawCircle(CheesePale, radius = size * 0.95f, center = centre)
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = size * 0.4f,
                center = Offset(centre.x - size * 0.2f, centre.y - size * 0.2f)
            )
        }

        Sprite.JALAPENO -> {
            drawCircle(JalapenoGreen, radius = size * 0.8f, center = centre)
            drawCircle(CheesePale.copy(alpha = 0.9f), radius = size * 0.34f, center = centre)
        }

        Sprite.MUSHROOM -> rotate(spin, centre) {
            drawArc(
                color = MushroomTan,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(centre.x - size, centre.y - size * 0.8f),
                size = Size(size * 2, size * 1.6f)
            )
            drawRect(
                color = MushroomTan.copy(alpha = 0.85f),
                topLeft = Offset(centre.x - size * 0.28f, centre.y),
                size = Size(size * 0.56f, size * 0.72f)
            )
        }

        Sprite.CORN -> rotate(spin, centre) {
            drawOval(
                color = CornYellow,
                topLeft = Offset(centre.x - size * 0.5f, centre.y - size * 0.72f),
                size = Size(size, size * 1.44f)
            )
        }

        Sprite.PANEER -> rotate(spin, centre) {
            drawRect(
                color = PaneerCream,
                topLeft = Offset(centre.x - size * 0.8f, centre.y - size * 0.8f),
                size = Size(size * 1.6f, size * 1.6f)
            )
            // Tandoori char along one edge.
            drawRect(
                color = PepperoniDeep.copy(alpha = 0.35f),
                topLeft = Offset(centre.x - size * 0.8f, centre.y + size * 0.4f),
                size = Size(size * 1.6f, size * 0.4f)
            )
        }

        Sprite.OLIVE -> {
            drawCircle(OliveDark, radius = size * 0.84f, center = centre)
            drawCircle(SauceRedDeep.copy(alpha = 0.85f), radius = size * 0.34f, center = centre)
        }

        Sprite.CAPSICUM -> rotate(spin, centre) {
            // An open "C" — a sliced bell-pepper ring.
            drawArc(
                color = CapsicumGreen,
                startAngle = 30f,
                sweepAngle = 300f,
                useCenter = false,
                topLeft = Offset(centre.x - size, centre.y - size),
                size = Size(size * 2, size * 2),
                style = Stroke(width = size * 0.45f)
            )
        }

        Sprite.CHICKEN -> rotate(spin, centre) {
            drawOval(
                color = ChickenBrown,
                topLeft = Offset(centre.x - size * 0.9f, centre.y - size * 0.6f),
                size = Size(size * 1.8f, size * 1.2f)
            )
            drawOval(
                color = ChickenBrown.copy(alpha = 0.55f),
                topLeft = Offset(centre.x - size * 0.4f, centre.y - size * 0.35f),
                size = Size(size * 0.8f, size * 0.5f)
            )
        }

        Sprite.BASIL -> rotate(spin, centre) {
            drawPath(leafPath(centre, size), BasilLeaf)
        }

        Sprite.PEPPERONI -> {
            drawCircle(PepperoniDeep, radius = size, center = centre)
            drawCircle(PepperoniRed, radius = size * 0.86f, center = centre)
            // Rendered fat flecks — the detail that reads as "pepperoni" not "red dot".
            val random = Random((centre.x * 100 + centre.y).toInt())
            repeat(3) {
                drawCircle(
                    color = PepperoniDeep.copy(alpha = 0.55f),
                    radius = size * 0.14f,
                    center = Offset(
                        centre.x + (random.nextFloat() - 0.5f) * size,
                        centre.y + (random.nextFloat() - 0.5f) * size
                    )
                )
            }
        }

        Sprite.TOMATO -> {
            drawCircle(SauceRed, radius = size * 0.85f, center = centre)
            drawCircle(PepperoniRed.copy(alpha = 0.5f), radius = size * 0.45f, center = centre)
        }

        Sprite.ONION -> rotate(spin, centre) {
            drawArc(
                color = OnionPurple,
                startAngle = 20f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = Offset(centre.x - size, centre.y - size),
                size = Size(size * 2, size * 2),
                style = Stroke(width = size * 0.26f)
            )
        }

        Sprite.CHILLI -> rotate(spin, centre) {
            drawOval(
                color = PepperoniRed,
                topLeft = Offset(centre.x - size * 0.25f, centre.y - size),
                size = Size(size * 0.5f, size * 2f)
            )
        }

        // A roasted clove: a pale teardrop with a browned tip.
        Sprite.GARLIC -> rotate(spin, centre) {
            drawOval(
                color = GarlicCream,
                topLeft = Offset(centre.x - size * 0.55f, centre.y - size * 0.95f),
                size = Size(size * 1.1f, size * 1.9f)
            )
            drawOval(
                color = GarlicToasted,
                topLeft = Offset(centre.x - size * 0.4f, centre.y + size * 0.35f),
                size = Size(size * 0.8f, size * 0.6f)
            )
        }

        // Sun-dried: a small, dark, shrivelled piece — deliberately irregular so it
        // never reads as the plump fresh tomato.
        Sprite.SUNDRIED_TOMATO -> rotate(spin, centre) {
            drawOval(
                color = SundriedTomato,
                topLeft = Offset(centre.x - size * 0.85f, centre.y - size * 0.6f),
                size = Size(size * 1.7f, size * 1.2f)
            )
            drawOval(
                color = SundriedTomatoDeep,
                topLeft = Offset(centre.x - size * 0.35f, centre.y - size * 0.25f),
                size = Size(size * 0.7f, size * 0.5f)
            )
        }

        // Caramelised, so amber rather than the raw onion's purple.
        Sprite.CARAMELISED_ONION -> rotate(spin, centre) {
            drawArc(
                color = CaramelisedOnion,
                startAngle = 15f,
                sweepAngle = 250f,
                useCenter = false,
                topLeft = Offset(centre.x - size, centre.y - size),
                size = Size(size * 2, size * 2),
                style = Stroke(width = size * 0.32f)
            )
        }
    }
}

/** A simple two-curve leaf, built from cubics for maximum API compatibility. */
private fun leafPath(centre: Offset, size: Float): Path = Path().apply {
    val tip = Offset(centre.x, centre.y - size * 1.2f)
    val tail = Offset(centre.x, centre.y + size * 1.2f)
    moveTo(tip.x, tip.y)
    cubicTo(
        centre.x + size, centre.y - size * 0.6f,
        centre.x + size * 0.8f, centre.y + size * 0.6f,
        tail.x, tail.y
    )
    cubicTo(
        centre.x - size * 0.8f, centre.y + size * 0.6f,
        centre.x - size, centre.y - size * 0.6f,
        tip.x, tip.y
    )
    close()
}

private fun pointOn(centre: Offset, radius: Float, angleDegrees: Float): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        centre.x + radius * cos(radians).toFloat(),
        centre.y + radius * sin(radians).toFloat()
    )
}
