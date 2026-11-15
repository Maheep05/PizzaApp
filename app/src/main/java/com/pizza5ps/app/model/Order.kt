package com.pizza5ps.app.model

enum class OrderStatus(val title: String, val subtitle: String, val stepNumber: Int) {
    CONFIRMED("Order Confirmed", "Restaurant accepted your order", 1),
    PREPARING("Pizza Being Prepared", "Tossed with love & baking in the oven", 2),
    OUT_FOR_DELIVERY("Out for Delivery", "Rider Vikram S. is en route", 3),
    DELIVERED("Order Delivered", "Enjoy your hot and fresh pizza!", 4)
}

data class Order(
    val id: String = "P5P-" + (10000..99999).random(),
    val items: List<CartItem>,
    val subtotal: Int,
    val deliveryFee: Int,
    val tax: Int,
    val discount: Int,
    val grandTotal: Int,
    val deliveryAddress: String = "123, 4th Cross, Indiranagar, Bengaluru - 560038",
    val paymentMethod: String = "UPI (Google Pay)",
    val status: OrderStatus = OrderStatus.CONFIRMED,
    val timestamp: Long = System.currentTimeMillis()
)
