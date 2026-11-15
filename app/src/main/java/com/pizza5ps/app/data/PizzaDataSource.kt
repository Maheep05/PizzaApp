package com.pizza5ps.app.data

import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCategory

object PizzaDataSource {
    val pizzas: List<Pizza> = listOf(
        Pizza(
            id = "p_margherita",
            name = "Margherita",
            tagline = "Classic • Mozzarella",
            description = "Authentic Napoli tomato sauce, 100% whole-milk mozzarella cheese, extra virgin olive oil, and freshly picked sweet basil leaves.",
            basePrice = 249,
            rating = 4.8,
            reviewCount = 340,
            category = PizzaCategory.POPULAR,
            isVeg = true,
            isPopular = true,
            isBestSeller = true,
            prepTimeMinutes = 18
        ),
        Pizza(
            id = "p_farmhouse",
            name = "Farmhouse",
            tagline = "Crisp Veggies • Herb Butter",
            description = "A garden-fresh medley of crisp green bell peppers, crunchy red onions, grilled button mushrooms, and juicy ripe tomatoes on seasoned crust.",
            basePrice = 299,
            rating = 4.7,
            reviewCount = 285,
            category = PizzaCategory.VEG,
            isVeg = true,
            isPopular = true,
            isBestSeller = true,
            prepTimeMinutes = 20
        ),
        Pizza(
            id = "p_pepperoni",
            name = "Pepperoni Supreme",
            tagline = "Smoky Cuts • Mozzarella",
            description = "Generously layered with premium artisanal pepperoni, spicy marinara, melted mozzarella, and a light dash of dried oregano.",
            basePrice = 349,
            rating = 4.9,
            reviewCount = 415,
            category = PizzaCategory.NON_VEG,
            isVeg = false,
            isPopular = true,
            isBestSeller = true,
            prepTimeMinutes = 22
        ),
        Pizza(
            id = "p_paneer_tikka",
            name = "Paneer Tikka Passion",
            tagline = "Tandoori Spices • Paprika",
            description = "Clay-oven baked paneer cubes marinated in rich Indian spices, paired with crunchy capsicum, red paprika, and creamy garlic dip.",
            basePrice = 319,
            rating = 4.8,
            reviewCount = 360,
            category = PizzaCategory.SPICY,
            isVeg = true,
            isPopular = true,
            isBestSeller = false,
            prepTimeMinutes = 20
        ),
        Pizza(
            id = "p_bbq_chicken",
            name = "BBQ Chicken Fiesta",
            tagline = "Hickory Smoke • Cheddar",
            description = "Tender grilled chicken chunks glazed in authentic smoky BBQ sauce, caramelized onions, mozzarella, and sharp English cheddar.",
            basePrice = 379,
            rating = 4.8,
            reviewCount = 220,
            category = PizzaCategory.NON_VEG,
            isVeg = false,
            isPopular = false,
            isBestSeller = true,
            prepTimeMinutes = 25
        ),
        Pizza(
            id = "p_veggie_supreme",
            name = "Veggie Supreme",
            tagline = "Loaded • Black Olives",
            description = "A supreme feast of Spanish black olives, golden sweet corn, crispy bell peppers, spicy jalapeños, and Italian seasoning.",
            basePrice = 329,
            rating = 4.6,
            reviewCount = 195,
            category = PizzaCategory.VEG,
            isVeg = true,
            isPopular = false,
            isBestSeller = false,
            prepTimeMinutes = 20
        ),
        Pizza(
            id = "p_four_cheese",
            name = "Four Cheese Alfredo",
            tagline = "Gouda • Cheddar • Mozzarella",
            description = "Rich white Alfredo sauce layered with four decadent cheeses: Buffalo Mozzarella, aged Parmesan, English Cheddar, and creamy Gouda.",
            basePrice = 359,
            rating = 4.9,
            reviewCount = 270,
            category = PizzaCategory.CHEESY,
            isVeg = true,
            isPopular = true,
            isBestSeller = true,
            prepTimeMinutes = 22
        ),
        Pizza(
            id = "p_ys_special",
            name = "Y's Special",
            tagline = "Chef's Signature • Slow-roasted",
            description = "The house signature: hand-torn fresh basil, slow-roasted garlic, " +
                "sun-dried tomatoes, sautéed button mushrooms and deeply caramelised onions " +
                "over buffalo mozzarella. Built the chef's way, and yours to adjust.",
            basePrice = 399,
            rating = 4.9,
            reviewCount = 512,
            category = PizzaCategory.VEG,
            isVeg = true,
            isPopular = true,
            isBestSeller = true,
            prepTimeMinutes = 24,
            // Arrives fully loaded — the five signature toppings come pre-selected.
            defaultToppingIds = listOf(
                "top_basil",
                "top_garlic",
                "top_sundried_tomato",
                "top_sauteed_mushroom",
                "top_caramelised_onion"
            )
        ),
        Pizza(
            id = "p_fiery_peri_peri",
            name = "Fiery Peri-Peri Chicken",
            tagline = "Hot Paprika • Jalapeños",
            description = "Spicy African bird's eye chili sauce, peri-peri spiced chicken shreds, sliced red chilies, fiery jalapeños, and cool sour cream swirl.",
            basePrice = 369,
            rating = 4.8,
            reviewCount = 230,
            category = PizzaCategory.SPICY,
            isVeg = false,
            isPopular = false,
            isBestSeller = false,
            prepTimeMinutes = 24
        )
    )

    fun getPizzaById(id: String): Pizza? {
        return pizzas.find { it.id == id }
    }
}
