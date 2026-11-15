package com.pizza5ps.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.components.StandardTopBar
import com.pizza5ps.app.viewmodel.PizzaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    viewModel: PizzaViewModel,
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val billSummary by viewModel.billSummary.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val selectedAddress by viewModel.deliveryDestination.collectAsState()
    var selectedPayment by remember { mutableStateOf("UPI (Google Pay / PhonePe)") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            StandardTopBar(
                title = "Checkout",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                color = PizzaTheme.colors.surface,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isProcessingPayment) {
                                isProcessingPayment = true
                                coroutineScope.launch {
                                    delay(1200) // Simulated payment verification
                                    viewModel.placeOrder(
                                        address = selectedAddress.address,
                                        paymentMethod = selectedPayment
                                    )
                                    isProcessingPayment = false
                                    onOrderPlaced()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PizzaTheme.colors.brand),
                        enabled = !isProcessingPayment
                    ) {
                        if (isProcessingPayment) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = PizzaTheme.colors.surface,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Authorizing UPI Payment...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PizzaTheme.colors.surface
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PLACE ORDER",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PizzaTheme.colors.surface
                                )
                                Text(
                                    text = "₹${billSummary.grandTotal}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = PizzaTheme.colors.surface
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = PizzaTheme.colors.ground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Delivery Address
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = PizzaTheme.colors.brand,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Delivery Address",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PizzaTheme.colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // The customer's own saved addresses, selected here and shared
                    // with the home header and profile — checkout used to offer two
                    // hardcoded options that nothing else in the app knew about.
                    viewModel.savedAddresses.forEach { stop ->
                        val isSelected = stop.address == selectedAddress.address
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) {
                                        PizzaTheme.colors.brand.copy(alpha = 0.06f)
                                    } else {
                                        PizzaTheme.colors.surface
                                    }
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) {
                                        PizzaTheme.colors.brand
                                    } else {
                                        PizzaTheme.colors.border
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectDeliveryAddress(stop) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectDeliveryAddress(stop) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PizzaTheme.colors.brand
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stop.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = PizzaTheme.colors.textPrimary
                                )
                                Text(
                                    text = stop.address,
                                    fontSize = 12.sp,
                                    color = PizzaTheme.colors.textMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Estimated Delivery Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PizzaTheme.colors.surfaceMuted)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Express",
                            tint = PizzaTheme.colors.brand,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Estimated delivery in 25–30 minutes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PizzaTheme.colors.textPrimary
                        )
                    }
                }
            }

            // Section 2: Payment Methods
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Payment",
                            tint = PizzaTheme.colors.brand,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Payment Method",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PizzaTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val paymentOptions = listOf(
                        "UPI (Google Pay / PhonePe)" to "Recommended • Instant simulated confirmation",
                        "Cash on Delivery (COD)" to "Pay with cash or QR at the door",
                        "Credit / Debit Card" to "Visa, Mastercard, RuPay"
                    )

                    paymentOptions.forEach { (title, subtitle) ->
                        val isSelected = selectedPayment.startsWith(title.take(3))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PizzaTheme.colors.brand.copy(alpha = 0.06f) else PizzaTheme.colors.surface)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PizzaTheme.colors.brand else PizzaTheme.colors.border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPayment = title }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPayment = title },
                                colors = RadioButtonDefaults.colors(selectedColor = PizzaTheme.colors.brand)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = PizzaTheme.colors.textPrimary
                                )
                                Text(
                                    text = subtitle,
                                    fontSize = 11.sp,
                                    color = PizzaTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Order Items Summary Mini
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Items in this Order (${cartItems.sumOf { it.quantity }})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PizzaTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    cartItems.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.pizza.name} (${item.size.label})",
                                fontSize = 12.sp,
                                color = PizzaTheme.colors.textPrimary
                            )
                            Text(
                                text = "₹${item.totalPrice}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PizzaTheme.colors.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PizzaTheme.colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Final Payable Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PizzaTheme.colors.textPrimary)
                        Text(text = "₹${billSummary.grandTotal}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PizzaTheme.colors.brand)
                    }
                }
            }
        }
    }
}
