package com.pizza5ps.app.data

import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import com.pizza5ps.app.model.Topping
import kotlin.math.roundToInt

/**
 * Nutrition for a build, computed — never guessed.
 *
 * Every figure in the app comes from this file: a per-ingredient composition table
 * multiplied by the grams actually on the pizza. Nothing is written by a language
 * model, and nothing is stored as a per-pizza "calories" constant that could drift
 * away from what the pizza is made of.
 *
 * That matters because these are health numbers. A chatbot asked "how many calories"
 * will happily produce a confident figure; if that figure is invented, the app is
 * lying to someone who may be counting. So the model is kept out of the arithmetic
 * entirely — it is handed these computed values and may only phrase them.
 *
 * **Accuracy caveat:** the per-ingredient values below are standard published
 * food-composition figures for typical preparations, and the gram weights are
 * typical for a pizzeria of this style. They are good estimates for a menu, not
 * laboratory measurements of a specific kitchen's output. The UI says so.
 */
data class NutritionFacts(
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val saturatedFatGrams: Double,
    val fibreGrams: Double,
    val sugarGrams: Double,
    val sodiumMilligrams: Int
) {
    operator fun plus(other: NutritionFacts) = NutritionFacts(
        calories = calories + other.calories,
        proteinGrams = proteinGrams + other.proteinGrams,
        carbsGrams = carbsGrams + other.carbsGrams,
        fatGrams = fatGrams + other.fatGrams,
        saturatedFatGrams = saturatedFatGrams + other.saturatedFatGrams,
        fibreGrams = fibreGrams + other.fibreGrams,
        sugarGrams = sugarGrams + other.sugarGrams,
        sodiumMilligrams = sodiumMilligrams + other.sodiumMilligrams
    )

    operator fun times(factor: Double) = NutritionFacts(
        calories = (calories * factor).roundToInt(),
        proteinGrams = proteinGrams * factor,
        carbsGrams = carbsGrams * factor,
        fatGrams = fatGrams * factor,
        saturatedFatGrams = saturatedFatGrams * factor,
        fibreGrams = fibreGrams * factor,
        sugarGrams = sugarGrams * factor,
        sodiumMilligrams = (sodiumMilligrams * factor).roundToInt()
    )

    /** Energy from each macro, as a fraction of total calories. */
    val proteinShare: Float get() = share(proteinGrams * 4)
    val carbShare: Float get() = share(carbsGrams * 4)
    val fatShare: Float get() = share(fatGrams * 9)

    private fun share(macroCalories: Double): Float =
        if (calories <= 0) 0f else (macroCalories / calories).toFloat().coerceIn(0f, 1f)

    companion object {
        val ZERO = NutritionFacts(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0)
    }
}

/**
 * One ingredient's composition per 100 g.
 *
 * Values are standard published figures for the stated preparation (raw, grilled,
 * pickled, etc.) — preparation matters enormously here: pickled jalapeños carry
 * roughly 200× the sodium of fresh ones, and caramelised onions carry three times
 * the calories of raw, because of the oil and the sugar concentration.
 */
private data class Ingredient(
    val name: String,
    val kcal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val saturatedFat: Double,
    val fibre: Double,
    val sugar: Double,
    val sodium: Int
) {
    /** Scales this ingredient's composition to an actual weight on the pizza. */
    fun forGrams(grams: Double): NutritionFacts {
        val f = grams / 100.0
        return NutritionFacts(
            calories = (kcal * f).roundToInt(),
            proteinGrams = protein * f,
            carbsGrams = carbs * f,
            fatGrams = fat * f,
            saturatedFatGrams = saturatedFat * f,
            fibreGrams = fibre * f,
            sugarGrams = sugar * f,
            sodiumMilligrams = (sodium * f).roundToInt()
        )
    }
}

private object Ingredients {
    //                           name                kcal  prot  carb   fat   sat  fibre  sugar  Na(mg)
    val Dough = Ingredient("pizza dough", 268, 8.9, 49.7, 3.6, 0.6, 2.2, 2.5, 480)
    val Marinara = Ingredient("tomato sauce", 35, 1.5, 6.1, 0.8, 0.1, 1.5, 4.2, 400)
    val Alfredo = Ingredient("alfredo sauce", 195, 3.5, 5.0, 18.0, 11.0, 0.3, 2.0, 420)
    val BbqSauce = Ingredient("bbq sauce", 172, 0.8, 41.0, 0.5, 0.1, 0.9, 33.0, 820)
    val Mozzarella = Ingredient("mozzarella", 300, 22.2, 2.4, 22.4, 13.2, 0.0, 1.2, 620)
    val Cheddar = Ingredient("cheddar", 403, 24.9, 1.3, 33.1, 21.0, 0.0, 0.5, 621)
    val OliveOil = Ingredient("olive oil", 884, 0.0, 0.0, 100.0, 13.8, 0.0, 0.0, 2)

    val Pepperoni = Ingredient("pepperoni", 504, 19.3, 1.2, 46.3, 17.0, 0.0, 0.0, 1761)
    val Chicken = Ingredient("grilled chicken", 165, 31.0, 0.0, 3.6, 1.0, 0.0, 0.0, 380)
    val Paneer = Ingredient("paneer", 265, 18.3, 3.4, 20.8, 13.0, 0.0, 2.6, 22)

    val Mushroom = Ingredient("mushrooms", 22, 3.1, 3.3, 0.3, 0.1, 1.0, 2.0, 5)
    val Capsicum = Ingredient("bell peppers", 26, 1.0, 6.0, 0.3, 0.1, 2.1, 4.2, 4)
    val Onion = Ingredient("red onion", 40, 1.1, 9.3, 0.1, 0.0, 1.7, 4.2, 4)
    val CaramelisedOnion = Ingredient("caramelised onion", 112, 1.0, 14.2, 6.1, 0.9, 1.5, 9.0, 120)
    val Olives = Ingredient("black olives", 115, 0.8, 6.3, 10.9, 1.4, 3.2, 0.5, 735)
    val Jalapeno = Ingredient("pickled jalapeños", 29, 0.9, 6.5, 0.4, 0.0, 2.8, 3.0, 1100)
    val SweetCorn = Ingredient("sweet corn", 86, 3.3, 19.0, 1.4, 0.2, 2.7, 3.2, 15)
    val FreshTomato = Ingredient("fresh tomato", 18, 0.9, 3.9, 0.2, 0.0, 1.2, 2.6, 5)
    val SunDriedTomato = Ingredient("sun-dried tomatoes", 258, 14.1, 55.8, 3.0, 0.4, 12.3, 37.6, 2095)
    val Basil = Ingredient("fresh basil", 23, 3.2, 2.6, 0.6, 0.0, 1.6, 0.3, 4)
    val RoastedGarlic = Ingredient("roasted garlic", 149, 6.4, 33.1, 0.5, 0.1, 2.1, 1.0, 17)
    val Chilli = Ingredient("red chilli", 40, 1.9, 8.8, 0.4, 0.0, 1.5, 5.3, 9)
}

/** An ingredient on a pizza, at a weight. */
private data class Portion(val ingredient: Ingredient, val grams: Double)

/**
 * What each pizza is actually made of, at the reference 10" medium size.
 *
 * Weights are typical for this style of pizzeria. Everything else — other sizes,
 * crusts and added extras — is derived from these by scaling, so a pizza's figures
 * can never disagree with its recipe.
 */
private val recipes: Map<String, List<Portion>> = mapOf(
    "p_margherita" to listOf(
        Portion(Ingredients.Marinara, 85.0),
        Portion(Ingredients.Mozzarella, 110.0),
        Portion(Ingredients.FreshTomato, 40.0),
        Portion(Ingredients.Basil, 5.0),
        Portion(Ingredients.OliveOil, 6.0)
    ),
    "p_farmhouse" to listOf(
        Portion(Ingredients.Marinara, 85.0),
        Portion(Ingredients.Mozzarella, 100.0),
        Portion(Ingredients.Capsicum, 35.0),
        Portion(Ingredients.Onion, 30.0),
        Portion(Ingredients.Mushroom, 40.0),
        Portion(Ingredients.FreshTomato, 30.0)
    ),
    "p_pepperoni" to listOf(
        Portion(Ingredients.Marinara, 85.0),
        Portion(Ingredients.Mozzarella, 115.0),
        Portion(Ingredients.Pepperoni, 55.0)
    ),
    "p_paneer_tikka" to listOf(
        Portion(Ingredients.Marinara, 80.0),
        Portion(Ingredients.Mozzarella, 100.0),
        Portion(Ingredients.Paneer, 70.0),
        Portion(Ingredients.Capsicum, 30.0),
        Portion(Ingredients.Chilli, 8.0)
    ),
    "p_bbq_chicken" to listOf(
        Portion(Ingredients.BbqSauce, 70.0),
        Portion(Ingredients.Mozzarella, 90.0),
        Portion(Ingredients.Cheddar, 30.0),
        Portion(Ingredients.Chicken, 85.0),
        Portion(Ingredients.Onion, 30.0)
    ),
    "p_veggie_supreme" to listOf(
        Portion(Ingredients.Marinara, 85.0),
        Portion(Ingredients.Mozzarella, 100.0),
        Portion(Ingredients.Olives, 25.0),
        Portion(Ingredients.SweetCorn, 35.0),
        Portion(Ingredients.Capsicum, 35.0),
        Portion(Ingredients.Jalapeno, 15.0)
    ),
    "p_four_cheese" to listOf(
        Portion(Ingredients.Alfredo, 70.0),
        Portion(Ingredients.Mozzarella, 90.0),
        Portion(Ingredients.Cheddar, 55.0),
        Portion(Ingredients.Basil, 4.0)
    ),
    "p_fiery_peri_peri" to listOf(
        Portion(Ingredients.Marinara, 80.0),
        Portion(Ingredients.Mozzarella, 100.0),
        Portion(Ingredients.Chicken, 80.0),
        Portion(Ingredients.Jalapeno, 20.0),
        Portion(Ingredients.Chilli, 10.0)
    ),
    "p_ys_special" to listOf(
        Portion(Ingredients.Marinara, 80.0),
        Portion(Ingredients.Mozzarella, 110.0),
        Portion(Ingredients.Basil, 6.0),
        Portion(Ingredients.RoastedGarlic, 15.0),
        Portion(Ingredients.SunDriedTomato, 25.0),
        Portion(Ingredients.Mushroom, 40.0),
        Portion(Ingredients.CaramelisedOnion, 35.0),
        Portion(Ingredients.OliveOil, 7.0)
    )
)

/** What an added extra topping weighs, and what it is made of. */
private val extraToppings: Map<String, Portion> = mapOf(
    "top_cheese" to Portion(Ingredients.Mozzarella, 35.0),
    "top_jalapeno" to Portion(Ingredients.Jalapeno, 20.0),
    "top_mushroom" to Portion(Ingredients.Mushroom, 35.0),
    "top_corn" to Portion(Ingredients.SweetCorn, 35.0),
    "top_paneer" to Portion(Ingredients.Paneer, 45.0),
    "top_olives" to Portion(Ingredients.Olives, 25.0),
    "top_capsicum" to Portion(Ingredients.Capsicum, 30.0),
    "top_chicken" to Portion(Ingredients.Chicken, 55.0),
    "top_basil" to Portion(Ingredients.Basil, 5.0),
    "top_garlic" to Portion(Ingredients.RoastedGarlic, 12.0),
    "top_sundried_tomato" to Portion(Ingredients.SunDriedTomato, 20.0),
    "top_sauteed_mushroom" to Portion(Ingredients.Mushroom, 35.0),
    "top_caramelised_onion" to Portion(Ingredients.CaramelisedOnion, 30.0)
)

/** Everything the UI and the assistant need about one build. */
data class NutritionBreakdown(
    val whole: NutritionFacts,
    val perSlice: NutritionFacts,
    val slices: Int,
    /** Largest contributors to the calorie count, biggest first. */
    val topCalorieSources: List<Pair<String, Int>>
)

object NutritionCalculator {

    /** Dough on a 10" medium, before the crust style adjusts it. */
    private const val REFERENCE_DOUGH_GRAMS = 240.0

    /**
     * Toppings scale with the area of the pizza, not its diameter — a 12" holds
     * 2.25× the toppings of an 8", not 1.5×.
     */
    private fun areaFactor(size: PizzaSize): Double {
        val inches = when (size) {
            PizzaSize.SMALL -> 8.0
            PizzaSize.MEDIUM -> 10.0
            PizzaSize.LARGE -> 12.0
        }
        return (inches * inches) / (10.0 * 10.0)
    }

    fun slicesFor(size: PizzaSize): Int = when (size) {
        PizzaSize.SMALL -> 4
        PizzaSize.MEDIUM -> 6
        PizzaSize.LARGE -> 8
    }

    fun calculate(
        pizza: Pizza,
        size: PizzaSize,
        crust: PizzaCrust,
        extras: List<Topping>,
        quantity: Int = 1
    ): NutritionBreakdown {
        val area = areaFactor(size)
        val contributions = mutableListOf<Pair<String, NutritionFacts>>()

        // Base dough, adjusted for the crust style.
        val doughGrams = REFERENCE_DOUGH_GRAMS * area * when (crust) {
            PizzaCrust.CLASSIC -> 1.0
            PizzaCrust.THIN_CRUST -> 0.70
            PizzaCrust.CHEESE_BURST -> 1.05
        }
        contributions += "Dough" to Ingredients.Dough.forGrams(doughGrams)

        // Cheese burst stuffs the rim — that cheese is part of the crust, not a topping.
        if (crust == PizzaCrust.CHEESE_BURST) {
            contributions += "Cheese-burst crust" to
                Ingredients.Mozzarella.forGrams(60.0 * area)
        }

        recipes[pizza.id].orEmpty().forEach { portion ->
            contributions += portion.ingredient.name.replaceFirstChar { it.uppercase() } to
                portion.ingredient.forGrams(portion.grams * area)
        }

        extras.forEach { topping ->
            extraToppings[topping.id]?.let { portion ->
                contributions += "${topping.name} (extra)" to
                    portion.ingredient.forGrams(portion.grams * area)
            }
        }

        val whole = contributions.fold(NutritionFacts.ZERO) { acc, (_, facts) -> acc + facts }
        val slices = slicesFor(size)

        return NutritionBreakdown(
            whole = whole * quantity.toDouble(),
            perSlice = whole * (1.0 / slices),
            slices = slices * quantity,
            topCalorieSources = contributions
                .groupBy({ it.first }, { it.second.calories })
                .map { (name, calories) -> name to calories.sum() }
                .sortedByDescending { it.second }
                .take(4)
        )
    }
}

/**
 * Reference intakes used to express a figure as "about a third of a day's…".
 *
 * These are the standard adult reference values printed on food labels. They are a
 * population-level yardstick, not a personal target — the UI frames them that way.
 */
object DailyReference {
    const val CALORIES = 2000
    const val PROTEIN_GRAMS = 50.0
    const val CARBS_GRAMS = 260.0
    const val FAT_GRAMS = 70.0
    const val SATURATED_FAT_GRAMS = 20.0
    const val FIBRE_GRAMS = 30.0
    const val SUGAR_GRAMS = 90.0
    const val SODIUM_MILLIGRAMS = 2300

    fun percentOfDay(value: Double, reference: Double): Int =
        ((value / reference) * 100).roundToInt()
}
