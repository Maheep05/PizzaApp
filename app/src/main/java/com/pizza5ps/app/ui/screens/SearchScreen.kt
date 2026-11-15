package com.pizza5ps.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.ui.components.BestSellerPizzaRow
import com.pizza5ps.app.ui.components.BottomNavBar
import com.pizza5ps.app.ui.components.BottomNavTab
import com.pizza5ps.app.viewmodel.PizzaViewModel
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.foundation.layout.statusBarsPadding

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: PizzaViewModel,
    onNavigateToDetails: (Pizza) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredPizzas by viewModel.filteredPizzas.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val popularTags = listOf("Margherita", "Cheese Burst", "Paneer Tikka", "Pepperoni", "Spicy", "Alfredo")

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = BottomNavTab.SEARCH.route,
                cartItemCount = cartItems.sumOf { it.quantity },
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> onNavigateToHome()
                        BottomNavTab.SEARCH -> {}
                        BottomNavTab.CART -> onNavigateToCart()
                        BottomNavTab.PROFILE -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = PizzaTheme.colors.ground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Search Input Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PizzaTheme.colors.surface,
                        unfocusedContainerColor = PizzaTheme.colors.surface,
                        focusedBorderColor = PizzaTheme.colors.brand,
                        unfocusedBorderColor = PizzaTheme.colors.border,
                        cursorColor = PizzaTheme.colors.brand
                    ),
                    placeholder = {
                        Text(
                            text = "Search pizza by name, flavor or crust...",
                            color = PizzaTheme.colors.textMuted,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PizzaTheme.colors.brand,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = PizzaTheme.colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Quick Popular Search Tags
            if (searchQuery.isEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Popular Searches",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PizzaTheme.colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        popularTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(PizzaTheme.colors.surface)
                                    .border(1.dp, PizzaTheme.colors.border, RoundedCornerShape(50))
                                    .clickable { viewModel.onSearchQueryChanged(tag) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 12.sp,
                                    color = PizzaTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Results List or Empty State
            if (filteredPizzas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = PizzaTheme.colors.textMuted,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No pizzas found for \"$searchQuery\"",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PizzaTheme.colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching for Margherita, Farmhouse, or Pepperoni",
                            fontSize = 13.sp,
                            color = PizzaTheme.colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Found ${filteredPizzas.size} delicious results",
                            fontSize = 13.sp,
                            color = PizzaTheme.colors.textMuted,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(filteredPizzas) { pizza ->
                        BestSellerPizzaRow(
                            pizza = pizza,
                            onCardClick = {
                                viewModel.openPizzaDetail(pizza)
                                onNavigateToDetails(pizza)
                            },
                            onQuickAdd = { viewModel.quickAdd(pizza) }
                        )
                    }
                }
            }
        }
    }
}
