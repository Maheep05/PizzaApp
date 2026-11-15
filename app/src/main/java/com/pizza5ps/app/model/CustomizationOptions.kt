package com.pizza5ps.app.model

enum class PizzaSize(val label: String, val inches: String, val priceOffset: Int) {
    SMALL("Small", "8\"", 0),
    MEDIUM("Medium", "10\"", 100),
    LARGE("Large", "12\"", 200)
}

enum class PizzaCrust(val label: String, val priceOffset: Int, val description: String) {
    CLASSIC("Classic Hand Tossed", 0, "Crisp outside, soft inside"),
    THIN_CRUST("Crispy Thin Crust", 30, "Ultra light & crunchy base"),
    CHEESE_BURST("Cheese Burst", 70, "Crust filled with molten cheese")
}

/**
 * @param name the full menu name, used in the cart and the price breakdown.
 * @param shortName the label on the selection chip. Short enough that chips sit two or
 *   three to a row — with the full names several were wide enough to claim a whole row
 *   each, and the list ran off the bottom of the screen.
 */
data class Topping(
    val id: String,
    val name: String,
    val shortName: String,
    val price: Int,
    val isVeg: Boolean = true
)

/**
 * Extra toppings are complimentary — every price here is zero.
 *
 * They still exist as a priced concept so the breakdown, the running total and the
 * cart maths all keep working unchanged if a price is ever put back.
 */
object AvailableToppings {
    val all = listOf(
        Topping("top_cheese", "Extra Mozzarella", "Mozzarella", 0),
        Topping("top_jalapeno", "Fresh Jalapeños", "Jalapeños", 0),
        Topping("top_mushroom", "Grilled Mushrooms", "Mushrooms", 0),
        Topping("top_corn", "Golden Sweet Corn", "Sweet corn", 0),
        Topping("top_paneer", "Peri-Peri Paneer", "Paneer", 0),
        Topping("top_olives", "Black Olives", "Olives", 0),
        Topping("top_capsicum", "Crisp Bell Peppers", "Bell peppers", 0),
        Topping("top_chicken", "BBQ Chicken Chunks", "BBQ chicken", 0, isVeg = false),

        // The Y's Special line-up. Selectable on any pizza, and pre-selected on
        // Y's Special itself.
        Topping("top_basil", "Fresh Basil", "Basil", 0),
        Topping("top_garlic", "Roasted Garlic", "Garlic", 0),
        Topping("top_sundried_tomato", "Sun-dried Tomatoes", "Sun-dried tomato", 0),
        Topping("top_sauteed_mushroom", "Sautéed Mushrooms", "Sautéed mushroom", 0),
        Topping("top_caramelised_onion", "Caramelised Onions", "Caramelised onion", 0)
    )

    /** Resolves the ids a pizza ships with into the toppings themselves. */
    fun byIds(ids: Collection<String>): Set<Topping> =
        all.filter { it.id in ids }.toSet()
}
