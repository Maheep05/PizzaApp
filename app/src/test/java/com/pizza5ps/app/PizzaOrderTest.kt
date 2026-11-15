package com.pizza5ps.app

import com.pizza5ps.app.data.PizzaDataSource
import com.pizza5ps.app.model.AvailableToppings
import com.pizza5ps.app.model.CartItem
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import com.pizza5ps.app.model.Topping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PizzaOrderTest {

    @Test
    fun testPizzaCatalogLoaded() {
        val pizzas = PizzaDataSource.pizzas
        assertEquals(9, pizzas.size)
        val margherita = PizzaDataSource.getPizzaById("p_margherita")
        assertNotNull(margherita)
        assertEquals("Margherita", margherita?.name)
        assertEquals(249, margherita?.basePrice)
    }

    /**
     * Exercises the price arithmetic with priced toppings on purpose.
     *
     * The catalogue's toppings are currently free, so using them here would make the
     * sum pass no matter what. Constructing priced toppings keeps this a real test of
     * [CartItem.unitPrice] and means it still guards the maths if a price is ever
     * reintroduced.
     */
    @Test
    fun testDynamicCartItemPriceCalculation() {
        // Base Margherita ₹249 + Medium ₹100 + Cheese burst ₹70 + ₹50 + ₹30 = ₹499
        // × quantity 2 = ₹998
        val margherita = PizzaDataSource.getPizzaById("p_margherita")!!
        val cartItem = CartItem(
            pizza = margherita,
            size = PizzaSize.MEDIUM,
            crust = PizzaCrust.CHEESE_BURST,
            selectedToppings = listOf(
                Topping("test_cheese", "Extra Mozzarella", "Mozzarella", 50),
                Topping("test_jalapeno", "Fresh Jalapeños", "Jalapeños", 30)
            ),
            quantity = 2
        )

        assertEquals(499, cartItem.unitPrice)
        assertEquals(998, cartItem.totalPrice)
    }

    @Test
    fun testEveryToppingIsFree() {
        assertTrue(
            "Toppings are advertised as complimentary, so none may carry a price",
            AvailableToppings.all.all { it.price == 0 }
        )
    }

    /** Y's Special ships pre-loaded, and adding those toppings must cost nothing. */
    @Test
    fun testSignaturePizzaShipsWithItsToppings() {
        val special = PizzaDataSource.getPizzaById("p_ys_special")!!
        val defaults = AvailableToppings.byIds(special.defaultToppingIds)

        assertEquals(5, defaults.size)
        assertEquals(
            setOf(
                "Fresh Basil",
                "Roasted Garlic",
                "Sun-dried Tomatoes",
                "Sautéed Mushrooms",
                "Caramelised Onions"
            ),
            defaults.map { it.name }.toSet()
        )

        val cartItem = CartItem(
            pizza = special,
            size = PizzaSize.MEDIUM,
            crust = PizzaCrust.CLASSIC,
            selectedToppings = defaults.toList(),
            quantity = 1
        )
        // 399 base + 100 medium, and nothing for the five signature toppings.
        assertEquals(499, cartItem.unitPrice)
    }

    @Test
    fun testCouponDiscountCalculation() {
        val subtotal = 499
        val discount = (subtotal * 0.20).toInt()
        assertEquals(99, discount)
        val deliveryFee = 30 // since < 500
        val tax = (subtotal * 0.05).toInt() // 24
        val grandTotal = subtotal + deliveryFee + tax - discount
        assertEquals(454, grandTotal)
    }
}
