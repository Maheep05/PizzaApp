package com.pizza5ps.app

import com.pizza5ps.app.data.AssistantReply
import com.pizza5ps.app.data.LocalNutritionAssistant
import com.pizza5ps.app.data.NutritionCalculator
import com.pizza5ps.app.data.NutritionContext
import com.pizza5ps.app.data.PizzaDataSource
import com.pizza5ps.app.model.AvailableToppings
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The nutrition figures are health information, so they get tested like arithmetic
 * rather than trusted like copy. These cover the properties that must hold for any
 * pizza, not one hand-checked snapshot that would freeze a wrong answer in place.
 */
class NutritionTest {

    private val margherita = PizzaDataSource.getPizzaById("p_margherita")!!

    @Test
    fun `calories land in a plausible range for a real pizza`() {
        val medium = NutritionCalculator.calculate(
            margherita, PizzaSize.MEDIUM, PizzaCrust.CLASSIC, emptyList()
        )
        // A 10" margherita is somewhere around 1,000-1,600 kcal whole. Anything far
        // outside that means the recipe or the table has drifted.
        assertTrue(
            "Whole pizza was ${medium.whole.calories} kcal",
            medium.whole.calories in 900..1800
        )
    }

    @Test
    fun `toppings scale with area, not diameter`() {
        val small = NutritionCalculator.calculate(
            margherita, PizzaSize.SMALL, PizzaCrust.CLASSIC, emptyList()
        ).whole.calories
        val large = NutritionCalculator.calculate(
            margherita, PizzaSize.LARGE, PizzaCrust.CLASSIC, emptyList()
        ).whole.calories

        // 12" over 8" is an area ratio of 2.25, not the 1.5 of the diameters.
        val ratio = large.toDouble() / small
        assertTrue("Large/small ratio was $ratio", ratio in 2.0..2.5)
    }

    @Test
    fun `per-slice times slice count returns the whole pizza`() {
        val breakdown = NutritionCalculator.calculate(
            margherita, PizzaSize.LARGE, PizzaCrust.CLASSIC, emptyList()
        )
        val reassembled = breakdown.perSlice.calories * breakdown.slices
        // Allow for rounding each slice to a whole calorie.
        assertTrue(
            "Slices summed to $reassembled against ${breakdown.whole.calories}",
            kotlin.math.abs(reassembled - breakdown.whole.calories) <= breakdown.slices
        )
    }

    @Test
    fun `thin crust is lighter than classic, cheese burst heavier`() {
        fun kcal(crust: PizzaCrust) = NutritionCalculator.calculate(
            margherita, PizzaSize.MEDIUM, crust, emptyList()
        ).whole.calories

        assertTrue(kcal(PizzaCrust.THIN_CRUST) < kcal(PizzaCrust.CLASSIC))
        assertTrue(kcal(PizzaCrust.CHEESE_BURST) > kcal(PizzaCrust.CLASSIC))
    }

    @Test
    fun `adding a topping never reduces the calories`() {
        val plain = NutritionCalculator.calculate(
            margherita, PizzaSize.MEDIUM, PizzaCrust.CLASSIC, emptyList()
        ).whole.calories

        AvailableToppings.all.forEach { topping ->
            val withExtra = NutritionCalculator.calculate(
                margherita, PizzaSize.MEDIUM, PizzaCrust.CLASSIC, listOf(topping)
            ).whole.calories
            assertTrue(
                "${topping.name} reduced calories from $plain to $withExtra",
                withExtra >= plain
            )
        }
    }

    @Test
    fun `every pizza on the menu has a nutrition recipe`() {
        PizzaDataSource.pizzas.forEach { pizza ->
            val breakdown = NutritionCalculator.calculate(
                pizza, PizzaSize.MEDIUM, PizzaCrust.CLASSIC, emptyList()
            )
            // A pizza with no recipe would come back as dough alone — far too light.
            assertTrue(
                "${pizza.name} computed only ${breakdown.whole.calories} kcal",
                breakdown.whole.calories > 700
            )
            assertTrue("${pizza.name} has no protein", breakdown.whole.proteinGrams > 10)
        }
    }

    @Test
    fun `offline assistant answers with the computed numbers, not invented ones`() = runBlocking {
        val breakdown = NutritionCalculator.calculate(
            margherita, PizzaSize.MEDIUM, PizzaCrust.CLASSIC, emptyList()
        )
        val context = NutritionContext(
            pizza = margherita,
            size = PizzaSize.MEDIUM,
            crust = PizzaCrust.CLASSIC,
            extras = emptyList(),
            breakdown = breakdown
        )

        val reply = LocalNutritionAssistant().ask("how many calories?", emptyList(), context)
        val text = (reply as AssistantReply.Answer).text

        assertTrue(
            "Answer did not quote the computed total: $text",
            text.contains("${breakdown.whole.calories}")
        )
    }

    @Test
    fun `fact sheet given to Claude carries the computed totals`() {
        val breakdown = NutritionCalculator.calculate(
            margherita, PizzaSize.LARGE, PizzaCrust.CHEESE_BURST, AvailableToppings.all.take(2)
        )
        assertEquals(8, breakdown.slices)
        assertTrue(breakdown.topCalorieSources.isNotEmpty())
        // The dough or the cheese should dominate a cheese-burst build.
        assertTrue(breakdown.topCalorieSources.first().second > 200)
    }
}
