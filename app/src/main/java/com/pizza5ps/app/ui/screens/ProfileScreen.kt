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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizza5ps.app.model.Order
import com.pizza5ps.app.ui.components.BottomNavBar
import com.pizza5ps.app.ui.components.BottomNavTab
import com.pizza5ps.app.ui.theme.PizzaTheme
import com.pizza5ps.app.ui.theme.PlayfairFamily
import com.pizza5ps.app.viewmodel.PizzaViewModel
import com.pizza5ps.app.ui.theme.bouncyClick
import com.pizza5ps.app.ui.theme.ThemeMode
import com.pizza5ps.app.ui.theme.Space
import com.pizza5ps.app.ui.theme.Radius
import com.pizza5ps.app.ui.theme.Motion
import com.pizza5ps.app.ui.components.SoftCard
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import com.pizza5ps.app.data.DeliveryGeo
import com.pizza5ps.app.data.DeliveryStop
import com.pizza5ps.app.ui.components.AddressPickerDialog
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.pizza5ps.app.data.ApiKeyStore
import com.pizza5ps.app.data.looksLikeAnthropicKey

@Composable
fun ProfileScreen(
    viewModel: PizzaViewModel,
    onSignedOut: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToTracking: () -> Unit
) {
    val activeOrder by viewModel.activeOrder.collectAsState()
    val pastOrders by viewModel.pastOrders.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val account by viewModel.account.collectAsState()
    val address by viewModel.deliveryDestination.collectAsState()

    var showAddressPicker by remember { mutableStateOf(false) }

    val storedApiKey by viewModel.apiKey.collectAsState()
    // The ViewModel must not hold a Context, so the encrypted store is built here
    // and handed in once.
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.attachKeyStore(ApiKeyStore(context))
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = BottomNavTab.PROFILE.route,
                cartItemCount = cartItems.sumOf { it.quantity },
                onTabSelected = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> onNavigateToHome()
                        BottomNavTab.SEARCH -> onNavigateToSearch()
                        BottomNavTab.CART -> onNavigateToCart()
                        BottomNavTab.PROFILE -> {}
                    }
                }
            )
        },
        containerColor = PizzaTheme.colors.ground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PizzaTheme.colors.brand.copy(alpha = 0.12f))
                                .border(2.dp, PizzaTheme.colors.brand, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account?.initials ?: "?",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = PizzaTheme.colors.brand
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = account?.name ?: "Guest",
                            fontFamily = PlayfairFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PizzaTheme.colors.textPrimary
                        )

                        Text(
                            text = account?.email ?: "Not signed in",
                            fontSize = 12.sp,
                            color = PizzaTheme.colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(PizzaTheme.colors.surfaceMuted)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PIZZA 5 P'S GOLD MEMBER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PizzaTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Active Order Shortcut Banner (if order in progress)
            if (activeOrder != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToTracking),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.inverse),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PizzaTheme.colors.brand),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = PizzaTheme.colors.onBrand,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Active Order #${activeOrder!!.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = PizzaTheme.colors.surface
                                    )
                                    Text(
                                        text = activeOrder!!.status.title,
                                        fontSize = 12.sp,
                                        color = PizzaTheme.colors.success,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Track",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PizzaTheme.colors.brand
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Track",
                                    tint = PizzaTheme.colors.brand,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Past Orders Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Past Orders",
                        tint = PizzaTheme.colors.brand,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Order History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PizzaTheme.colors.textPrimary
                    )
                }
            }

            if (pastOrders.isEmpty() && activeOrder == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = PizzaTheme.colors.textMuted,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No past orders yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PizzaTheme.colors.textPrimary
                            )
                            Text(
                                text = "Place an order to see it in your history!",
                                fontSize = 12.sp,
                                color = PizzaTheme.colors.textMuted
                            )
                        }
                    }
                }
            } else {
                items(pastOrders) { order ->
                    PastOrderCard(
                        order = order,
                        onReorder = {
                            viewModel.reorder(order)
                            onNavigateToCart()
                        }
                    )
                }
            }

            // Delivery address
            item {
                DeliveryAddressCard(
                    address = address,
                    onChange = { showAddressPicker = true }
                )
            }

            // AI assistant
            item {
                AiAssistantCard(
                    storedKey = storedApiKey,
                    onSave = { viewModel.saveApiKey(it) },
                    onClear = { viewModel.clearApiKey() }
                )
            }

            // Appearance
            item {
                AppearanceCard(
                    selected = themeMode,
                    onSelect = { viewModel.setThemeMode(it) }
                )
            }

            item {
                SignOutButton(
                    onSignOut = {
                        viewModel.signOut()
                        onSignedOut()
                    }
                )
            }
        }
    }

    if (showAddressPicker) {
        AddressPickerDialog(
            addresses = viewModel.savedAddresses,
            selected = address,
            onSelect = { viewModel.selectDeliveryAddress(it) },
            onDismiss = { showAddressPicker = false }
        )
    }
}

/**
 * The AI assistant setting.
 *
 * The app ships with **no API key**. One baked into an APK would be readable by
 * anyone the APK is sent to, and chargeable to whoever owns it — so the key is the
 * customer's own, pasted in here, kept encrypted on this device, and sent only to
 * Anthropic. Without one the nutrition chat still works, answering from the figures
 * the app computes itself.
 */
@Composable
private fun AiAssistantCard(
    storedKey: String?,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember(storedKey) { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val isConnected = !storedKey.isNullOrBlank()

    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PizzaTheme.colors.brand,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(Space.sm))

            Text(
                text = if (isConnected) {
                    "Connected. The nutrition chat is answered by Claude using your key."
                } else {
                    "Add your own Anthropic API key to have Claude answer the nutrition " +
                        "chat. Without one it still works, answered on-device."
                },
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Space.md))

            if (isConnected) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.successSoft)
                        .padding(horizontal = Space.md, vertical = Space.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PizzaTheme.colors.success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(Space.xs))
                    Text(
                        // Only the tail is shown — enough to tell two keys apart,
                        // not enough to be useful to someone reading over a shoulder.
                        text = "Key ending …${'$'}{storedKey.orEmpty().takeLast(6)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = PizzaTheme.colors.success
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "Remove key",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.brand,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.brand.copy(alpha = 0.10f))
                        .bouncyClick(pressedScale = 0.94f) {
                            onClear()
                            draft = ""
                            error = null
                        }
                        .padding(horizontal = Space.lg, vertical = Space.sm)
                )
            } else {
                OutlinedTextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    label = { Text(text = "Anthropic API key") },
                    placeholder = { Text(text = "sk-ant-…") },
                    singleLine = true,
                    visualTransformation = if (revealed) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (revealed) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (revealed) "Hide key" else "Show key",
                            tint = PizzaTheme.colors.textMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .bouncyClick(pressedScale = 0.85f) { revealed = !revealed }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PizzaTheme.colors.surface,
                        unfocusedContainerColor = PizzaTheme.colors.surface,
                        focusedBorderColor = PizzaTheme.colors.brand,
                        unfocusedBorderColor = PizzaTheme.colors.border,
                        cursorColor = PizzaTheme.colors.brand,
                        focusedLabelColor = PizzaTheme.colors.brand,
                        unfocusedLabelColor = PizzaTheme.colors.textMuted,
                        focusedTextColor = PizzaTheme.colors.textPrimary,
                        unfocusedTextColor = PizzaTheme.colors.textPrimary
                    )
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(Space.sm))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaTheme.colors.brand,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "Save key",
                    style = MaterialTheme.typography.labelMedium,
                    color = PizzaTheme.colors.onBrand,
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.pill))
                        .background(PizzaTheme.colors.brand)
                        .bouncyClick(pressedScale = 0.94f) {
                            when {
                                draft.isBlank() ->
                                    error = "Paste a key first"
                                !draft.looksLikeAnthropicKey() ->
                                    error = "Anthropic keys start with sk-ant-"
                                else -> {
                                    onSave(draft)
                                    error = null
                                }
                            }
                        }
                        .padding(horizontal = Space.xl, vertical = Space.sm)
                )

                Spacer(modifier = Modifier.height(Space.md))

                Text(
                    text = "Stored encrypted on this device only, and sent only to Anthropic. " +
                        "Usage is billed to your own account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PizzaTheme.colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Where this account's orders go.
 *
 * Only the delivery end is editable. The outlet is a single kitchen in Indiranagar, so
 * it is shown as a fact rather than offered as a setting.
 */
@Composable
private fun DeliveryAddressCard(
    address: DeliveryStop,
    onChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PizzaTheme.colors.brand,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = "Delivery Address",
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            Text(
                text = address.label,
                style = MaterialTheme.typography.titleMedium,
                color = PizzaTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = address.address,
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Space.md))

            Text(
                text = "Change address",
                style = MaterialTheme.typography.labelMedium,
                color = PizzaTheme.colors.brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.pill))
                    .background(PizzaTheme.colors.brand.copy(alpha = 0.10f))
                    .bouncyClick(pressedScale = 0.94f, onClick = onChange)
                    .padding(horizontal = Space.lg, vertical = Space.sm)
            )

            Spacer(modifier = Modifier.height(Space.md))

            Text(
                text = "Cooked at ${DeliveryGeo.store.label}",
                style = MaterialTheme.typography.bodySmall,
                color = PizzaTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SignOutButton(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(PizzaTheme.colors.surfaceMuted)
            .bouncyClick(pressedScale = 0.98f, onClick = onSignOut)
            .padding(vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = PizzaTheme.colors.brand,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(Space.sm))
        Text(
            text = "Sign out",
            style = MaterialTheme.typography.labelLarge,
            color = PizzaTheme.colors.brand
        )
    }
}

/**
 * Appearance switcher.
 *
 * Offers System alongside Light and Dark rather than a bare on/off toggle, because
 * most people set this once at the OS level and expect apps to follow — and it is
 * the default here for that reason.
 */
@Composable
private fun AppearanceCard(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Space.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = PizzaTheme.colors.brand,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Space.sm))
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(Space.md))

            // Segmented control: one row, three mutually exclusive options.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(PizzaTheme.colors.surfaceMuted)
                    .padding(Space.xs),
                horizontalArrangement = Arrangement.spacedBy(Space.xs)
            ) {
                ThemeMode.values().forEach { mode ->
                    val isSelected = selected == mode
                    val container by animateColorAsState(
                        targetValue = if (isSelected) {
                            PizzaTheme.colors.brand
                        } else {
                            Color.Transparent
                        },
                        animationSpec = Motion.mediumTween(),
                        label = "themeSegment"
                    )
                    val content by animateColorAsState(
                        targetValue = if (isSelected) {
                            PizzaTheme.colors.onBrand
                        } else {
                            PizzaTheme.colors.textMuted
                        },
                        animationSpec = Motion.mediumTween(),
                        label = "themeSegmentText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(container)
                            .bouncyClick(pressedScale = 0.95f) { onSelect(mode) }
                            .padding(vertical = Space.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = content
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PastOrderCard(
    order: Order,
    onReorder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PizzaTheme.colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PizzaTheme.colors.textPrimary
                    )
                    Text(
                        text = "Delivered • ₹${order.grandTotal}",
                        fontSize = 11.sp,
                        color = PizzaTheme.colors.success,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onReorder,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaTheme.colors.brand.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Reorder",
                            tint = PizzaTheme.colors.brand,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reorder",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PizzaTheme.colors.brand
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Text(
                    text = "• ${item.quantity}x ${item.pizza.name} (${item.size.label})",
                    fontSize = 12.sp,
                    color = PizzaTheme.colors.textMuted
                )
            }
        }
    }
}
