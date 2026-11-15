package com.pizza5ps.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.model.CartItem
import com.pizza5ps.app.ui.theme.PizzaTheme

@Composable
fun FloatingCartBar(
    cartItems: List<CartItem>,
    totalAmount: Int,
    onViewCartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = cartItems.sumOf { it.quantity }
    val isVisible = totalCount > 0

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onViewCartClicked),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.inverse),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PizzaTheme.colors.brand),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = PizzaTheme.colors.onInverse,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "$totalCount ${if (totalCount == 1) "item" else "items"} added",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PizzaTheme.colors.onInverse
                        )
                        val previewText = cartItems.firstOrNull()?.pizza?.name ?: ""
                        Text(
                            text = if (cartItems.size > 1) "$previewText & more" else previewText,
                            fontSize = 11.sp,
                            color = PizzaTheme.colors.onInverse.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "₹$totalAmount",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = PizzaTheme.colors.onInverse
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PizzaTheme.colors.brand)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PizzaTheme.colors.onInverse
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go",
                            tint = PizzaTheme.colors.onInverse,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
