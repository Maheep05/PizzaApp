package com.pizza5ps.app.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val pizza: Pizza,
    val size: PizzaSize = PizzaSize.MEDIUM,
    val crust: PizzaCrust = PizzaCrust.CLASSIC,
    val selectedToppings: List<Topping> = emptyList(),
    val quantity: Int = 1
) {
    val unitPrice: Int
        get() = pizza.basePrice + size.priceOffset + crust.priceOffset + selectedToppings.sumOf { it.price }

    val totalPrice: Int
        get() = unitPrice * quantity

    val optionsSummary: String
        get() {
            val parts = mutableListOf("${size.label} (${size.inches})", crust.label)
            if (selectedToppings.isNotEmpty()) {
                parts.add("+" + selectedToppings.joinToString(", ") { it.name })
            }
            return parts.joinToString(" • ")
        }
}
