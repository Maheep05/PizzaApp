package com.pizza5ps.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.model.CartItem
import com.pizza5ps.app.ui.components.StandardTopBar
import com.pizza5ps.app.ui.components.PizzaBase
import com.pizza5ps.app.ui.components.VegNonVegBadge
import com.pizza5ps.app.viewmodel.PizzaViewModel
import androidx.compose.material.icons.filled.RemoveShoppingCart

@Composable
fun CartScreen(
    viewModel: PizzaViewModel,
    onBackClick: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onBrowseMenuClick: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val billSummary by viewModel.billSummary.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()

    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Your Cart (${cartItems.sumOf { it.quantity }})",
                onBackClick = onBackClick,
                trailingAction = {
                    if (cartItems.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PizzaTheme.colors.brand,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { viewModel.clearCart() }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = PizzaTheme.colors.surface,
                    shadowElevation = 10.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total",
                                fontSize = 11.sp,
                                color = PizzaTheme.colors.textMuted
                            )
                            Text(
                                text = "₹${billSummary.grandTotal}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = PizzaTheme.colors.textPrimary
                            )
                        }

                        Button(
                            onClick = onNavigateToCheckout,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PizzaTheme.colors.brand),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Checkout",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PizzaTheme.colors.surface
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Checkout",
                                    tint = PizzaTheme.colors.surface,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = PizzaTheme.colors.ground
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        tint = PizzaTheme.colors.textMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Cart is Empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PizzaTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore our handcrafted stone-baked pizzas and satisfy your cravings!",
                        fontSize = 13.sp,
                        color = PizzaTheme.colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBrowseMenuClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PizzaTheme.colors.brand)
                    ) {
                        Text(
                            text = "Browse Pizzas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PizzaTheme.colors.surface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cart Items List
                items(cartItems, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = { viewModel.updateCartItemQuantity(item.id, 1) },
                        onDecrement = { viewModel.updateCartItemQuantity(item.id, -1) },
                        onRemove = { viewModel.removeCartItem(item.id) }
                    )
                }

                // Coupon Application Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = "Coupon",
                                        tint = PizzaTheme.colors.brand,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Coupons & Offers",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PizzaTheme.colors.textPrimary
                                    )
                                }

                                if (appliedCoupon != null) {
                                    Text(
                                        text = "Remove",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PizzaTheme.colors.brand,
                                        modifier = Modifier.clickable { viewModel.removeCoupon() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PizzaTheme.colors.success.copy(alpha = 0.1f))
                                        .border(1.dp, PizzaTheme.colors.success, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Applied",
                                            tint = PizzaTheme.colors.success,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "'$appliedCoupon' applied!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = PizzaTheme.colors.success
                                        )
                                    }
                                    Text(
                                        text = "Saved ₹${billSummary.discount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PizzaTheme.colors.success
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = {
                                            couponInput = it
                                            couponError = null
                                        },
                                        placeholder = {
                                            Text(text = "Enter code (try PIZZA20)", fontSize = 12.sp, color = PizzaTheme.colors.textMuted)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = PizzaTheme.colors.ground,
                                            unfocusedContainerColor = PizzaTheme.colors.ground,
                                            focusedBorderColor = PizzaTheme.colors.brand,
                                            unfocusedBorderColor = PizzaTheme.colors.border
                                        ),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (couponInput.isNotBlank()) {
                                                val success = viewModel.applyCoupon(couponInput.trim())
                                                if (!success) {
                                                    couponError = "Invalid code. Try PIZZA20"
                                                } else {
                                                    couponInput = ""
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PizzaTheme.colors.brand)
                                    ) {
                                        Text(text = "Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                if (couponError != null) {
                                    Text(
                                        text = couponError!!,
                                        fontSize = 11.sp,
                                        color = PizzaTheme.colors.brand,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bill Details Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Price Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PizzaTheme.colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Item Subtotal", fontSize = 13.sp, color = PizzaTheme.colors.textMuted)
                                Text(text = "₹${billSummary.subtotal}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PizzaTheme.colors.textPrimary)
                            }

                            if (billSummary.discount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Coupon Discount", fontSize = 13.sp, color = PizzaTheme.colors.success)
                                    Text(text = "-₹${billSummary.discount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PizzaTheme.colors.success)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Delivery Fee", fontSize = 13.sp, color = PizzaTheme.colors.textMuted)
                                Text(
                                    text = if (billSummary.deliveryFee == 0) "FREE" else "₹${billSummary.deliveryFee}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (billSummary.deliveryFee == 0) PizzaTheme.colors.success else PizzaTheme.colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Taxes & Charges (GST 5%)", fontSize = 13.sp, color = PizzaTheme.colors.textMuted)
                                Text(text = "₹${billSummary.tax}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PizzaTheme.colors.textPrimary)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = PizzaTheme.colors.border)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Payable",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = PizzaTheme.colors.textPrimary
                                )
                                Text(
                                    text = "₹${billSummary.grandTotal}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = PizzaTheme.colors.brand
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PizzaTheme.colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                PizzaBase(
                    pizza = item.pizza,
                    modifier = Modifier.size(60.dp)
                )
                VegNonVegBadge(
                    isVeg = item.pizza.isVeg,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.pizza.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PizzaTheme.colors.textPrimary
                )
                Text(
                    text = item.optionsSummary,
                    fontSize = 11.sp,
                    color = PizzaTheme.colors.textMuted,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${item.totalPrice}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = PizzaTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stepper
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PizzaTheme.colors.surfaceMuted)
                    .border(1.dp, PizzaTheme.colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = PizzaTheme.colors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PizzaTheme.colors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PizzaTheme.colors.brand)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = PizzaTheme.colors.surface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
