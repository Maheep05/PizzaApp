package com.pizza5ps.app.model

enum class PizzaCategory(val displayName: String) {
    ALL("All"),
    POPULAR("Popular"),
    VEG("Veg"),
    NON_VEG("Non-Veg"),
    CHEESY("Cheesy"),
    SPICY("Spicy")
}

data class Pizza(
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePrice: Int,
    val rating: Double,
    val reviewCount: Int,
    val category: PizzaCategory,
    val isVeg: Boolean,
    val isPopular: Boolean = false,
    val isBestSeller: Boolean = false,
    val prepTimeMinutes: Int = 20,
    /**
     * Toppings this pizza arrives with already chosen.
     *
     * Seeded into the customiser when the product page opens, so a signature pizza
     * comes built the way the kitchen intends it — and the customer can still take
     * any of them off.
     */
    val defaultToppingIds: List<String> = emptyList()
)
