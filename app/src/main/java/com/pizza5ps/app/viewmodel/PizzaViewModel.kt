package com.pizza5ps.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pizza5ps.app.data.Account
import com.pizza5ps.app.data.AccountRepository
import com.pizza5ps.app.data.AuthResult
import com.pizza5ps.app.data.ApiKeyStore
import com.pizza5ps.app.data.AssistantReply
import com.pizza5ps.app.data.ChatMessage
import com.pizza5ps.app.data.ClaudeNutritionAssistant
import com.pizza5ps.app.data.LocalNutritionAssistant
import com.pizza5ps.app.data.NutritionBreakdown
import com.pizza5ps.app.data.NutritionCalculator
import com.pizza5ps.app.data.NutritionContext
import kotlinx.coroutines.flow.map
import com.pizza5ps.app.data.DeliveryGeo
import com.pizza5ps.app.data.DeliveryRoute
import com.pizza5ps.app.data.DeliveryStop
import com.pizza5ps.app.data.PizzaDataSource
import com.pizza5ps.app.data.RouteRepository
import com.pizza5ps.app.model.AvailableToppings
import com.pizza5ps.app.model.CartItem
import com.pizza5ps.app.model.Order
import com.pizza5ps.app.model.OrderStatus
import com.pizza5ps.app.model.Pizza
import com.pizza5ps.app.model.PizzaCategory
import com.pizza5ps.app.model.PizzaCrust
import com.pizza5ps.app.model.PizzaSize
import com.pizza5ps.app.model.Topping
import com.pizza5ps.app.ui.theme.ThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BillSummary(
    val subtotal: Int = 0,
    val deliveryFee: Int = 0,
    val tax: Int = 0,
    val discount: Int = 0,
    val grandTotal: Int = 0
)

class PizzaViewModel : ViewModel() {

    // All pizzas from repository
    val allPizzas: List<Pizza> = PizzaDataSource.pizzas

    // Search & Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PizzaCategory.ALL)
    val selectedCategory: StateFlow<PizzaCategory> = _selectedCategory.asStateFlow()

    // Filtered pizza list based on query and category
    val filteredPizzas: StateFlow<List<Pizza>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        allPizzas.filter { pizza ->
            val matchesCategory = when (category) {
                PizzaCategory.ALL -> true
                PizzaCategory.POPULAR -> pizza.isPopular
                PizzaCategory.VEG -> pizza.isVeg
                PizzaCategory.NON_VEG -> !pizza.isVeg
                PizzaCategory.CHEESY -> pizza.category == PizzaCategory.CHEESY
                PizzaCategory.SPICY -> pizza.category == PizzaCategory.SPICY
            }
            val matchesQuery = query.isBlank() ||
                    pizza.name.contains(query, ignoreCase = true) ||
                    pizza.tagline.contains(query, ignoreCase = true) ||
                    pizza.description.contains(query, ignoreCase = true)

            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allPizzas)

    // Cart Management
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<String?>("PIZZA20")
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    val billSummary: StateFlow<BillSummary> = combine(
        _cartItems,
        _appliedCoupon
    ) { items, coupon ->
        val subtotal = items.sumOf { it.totalPrice }
        if (subtotal == 0) {
            BillSummary()
        } else {
            val deliveryFee = if (subtotal >= 500) 0 else 30
            val tax = (subtotal * 0.05).toInt()
            val discount = if (coupon.equals("PIZZA20", ignoreCase = true)) {
                (subtotal * 0.20).toInt()
            } else {
                0
            }
            val grandTotal = (subtotal + deliveryFee + tax - discount).coerceAtLeast(0)
            BillSummary(
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                tax = tax,
                discount = discount,
                grandTotal = grandTotal
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillSummary())

    // Customization Drafting State
    private val _selectedPizzaForDetail = MutableStateFlow<Pizza?>(allPizzas.firstOrNull())
    val selectedPizzaForDetail: StateFlow<Pizza?> = _selectedPizzaForDetail.asStateFlow()

    private val _draftSize = MutableStateFlow(PizzaSize.MEDIUM)
    val draftSize: StateFlow<PizzaSize> = _draftSize.asStateFlow()

    private val _draftCrust = MutableStateFlow(PizzaCrust.CLASSIC)
    val draftCrust: StateFlow<PizzaCrust> = _draftCrust.asStateFlow()

    private val _draftToppings = MutableStateFlow<Set<Topping>>(emptySet())
    val draftToppings: StateFlow<Set<Topping>> = _draftToppings.asStateFlow()

    private val _draftQuantity = MutableStateFlow(1)
    val draftQuantity: StateFlow<Int> = _draftQuantity.asStateFlow()

    val draftPrice: StateFlow<Int> = combine(
        _selectedPizzaForDetail,
        _draftSize,
        _draftCrust,
        _draftToppings,
        _draftQuantity
    ) { pizza, size, crust, toppings, qty ->
        val base = pizza?.basePrice ?: 0
        val unit = base + size.priceOffset + crust.priceOffset + toppings.sumOf { it.price }
        unit * qty
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 349)

    // Order Tracking & History
    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    private val _pastOrders = MutableStateFlow<List<Order>>(emptyList())
    val pastOrders: StateFlow<List<Order>> = _pastOrders.asStateFlow()

    private var trackingJob: Job? = null

    // ── Account ──────────────────────────────────────────────────────────────
    /** Null until someone signs in. Drives the greeting, the profile and the gate. */
    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    /** Last auth error, shown under the form. Cleared on the next attempt. */
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // ── Delivery address ─────────────────────────────────────────────────────
    // The outlet never moves — it is a single-branch pizzeria in Indiranagar — so only
    // the destination is selectable.
    val savedAddresses: List<DeliveryStop> = DeliveryGeo.destinations

    private val _deliveryDestination = MutableStateFlow(DeliveryGeo.defaultDestination)
    val deliveryDestination: StateFlow<DeliveryStop> = _deliveryDestination.asStateFlow()

    /** Null until the road route has been resolved for the current order. */
    private val _deliveryRoute = MutableStateFlow<DeliveryRoute?>(null)
    val deliveryRoute: StateFlow<DeliveryRoute?> = _deliveryRoute.asStateFlow()

    private val _isRouteLoading = MutableStateFlow(false)
    val isRouteLoading: StateFlow<Boolean> = _isRouteLoading.asStateFlow()

    private var routeJob: Job? = null

    // ── Nutrition assistant ──────────────────────────────────────────────────
    // Nutrition itself is always computed locally; only the *conversation* can be
    // handed to Claude, and only when the customer has supplied their own key.
    private var apiKeyStore: ApiKeyStore? = null

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    /** True once a key is stored — the chat says which brain is answering. */
    val isCloudAssistantEnabled: StateFlow<Boolean> = _apiKey
        .map { !it.isNullOrBlank() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    private val localAssistant = LocalNutritionAssistant()
    private var chatJob: Job? = null

    // ── Appearance ───────────────────────────────────────────────────────────
    // Held in the ViewModel so the choice survives configuration changes and can
    // be read above the navigation graph, where the theme is applied.
    //
    // Dark is the default rather than SYSTEM: the warm reds and ambers of the brand
    // sit best on the espresso ground, and it is the appearance the app is presented
    // in. System remains one tap away in Profile.
    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    // ── Nutrition assistant actions ──────────────────────────────────────────

    /**
     * Binds the encrypted key store. Called once from the UI layer, which is the
     * only place with a Context — the ViewModel must not hold one itself.
     */
    fun attachKeyStore(store: ApiKeyStore) {
        if (apiKeyStore != null) return
        apiKeyStore = store
        _apiKey.value = store.read()
    }

    fun saveApiKey(key: String) {
        apiKeyStore?.write(key)
        _apiKey.value = key.trim().takeIf { it.isNotBlank() }
    }

    fun clearApiKey() {
        apiKeyStore?.clear()
        _apiKey.value = null
    }

    /** Computes the nutrition for whatever is currently being customised. */
    fun nutritionForDraft(): NutritionBreakdown? {
        val pizza = _selectedPizzaForDetail.value ?: return null
        return NutritionCalculator.calculate(
            pizza = pizza,
            size = _draftSize.value,
            crust = _draftCrust.value,
            extras = _draftToppings.value.toList(),
            quantity = 1
        )
    }

    fun startNutritionChat() {
        if (_chatMessages.value.isNotEmpty()) return
        val pizza = _selectedPizzaForDetail.value ?: return
        _chatMessages.value = listOf(
            ChatMessage(
                fromUser = false,
                text = "Ask me anything about the nutrition of your ${'$'}{pizza.name} — " +
                    "calories, protein, salt, or how to make it lighter."
            )
        )
    }

    fun clearNutritionChat() {
        chatJob?.cancel()
        _chatMessages.value = emptyList()
        _isAssistantThinking.value = false
    }

    fun askNutritionQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty() || _isAssistantThinking.value) return

        val pizza = _selectedPizzaForDetail.value ?: return
        val breakdown = nutritionForDraft() ?: return
        val context = NutritionContext(
            pizza = pizza,
            size = _draftSize.value,
            crust = _draftCrust.value,
            extras = _draftToppings.value.toList(),
            breakdown = breakdown
        )

        val historyBeforeQuestion = _chatMessages.value
        _chatMessages.value = historyBeforeQuestion + ChatMessage(fromUser = true, text = trimmed)
        _isAssistantThinking.value = true

        // The cloud assistant is used only when a key is present; otherwise the
        // offline one answers, so the chat always works.
        val key = _apiKey.value
        val assistant = if (key.isNullOrBlank()) {
            localAssistant
        } else {
            ClaudeNutritionAssistant(key)
        }

        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            val reply = assistant.ask(trimmed, historyBeforeQuestion, context)
            val text = when (reply) {
                is AssistantReply.Answer -> reply.text
                is AssistantReply.Error -> reply.message
            }
            _chatMessages.value = _chatMessages.value + ChatMessage(fromUser = false, text = text)
            _isAssistantThinking.value = false
        }
    }

    // ── Account actions ──────────────────────────────────────────────────────

    fun signIn(email: String, password: String) {
        when (val result = AccountRepository.signIn(email, password)) {
            is AuthResult.Success -> {
                _account.value = result.account
                _authError.value = null
            }

            is AuthResult.Failure -> _authError.value = result.message
        }
    }

    fun signUp(name: String, email: String, password: String) {
        when (val result = AccountRepository.signUp(name, email, password)) {
            is AuthResult.Success -> {
                _account.value = result.account
                _authError.value = null
            }

            is AuthResult.Failure -> _authError.value = result.message
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    /**
     * Signs out and drops everything tied to the session.
     *
     * The cart and any order in flight belong to the person who was signed in, so
     * leaving them behind for the next account would be wrong.
     */
    fun signOut() {
        _account.value = null
        _authError.value = null
        _cartItems.value = emptyList()
        _deliveryDestination.value = DeliveryGeo.defaultDestination
        trackingJob?.cancel()
        routeJob?.cancel()
        _activeOrder.value = null
        _deliveryRoute.value = null
    }

    /** Changes where the order is going. The outlet it comes from is fixed. */
    fun selectDeliveryAddress(stop: DeliveryStop) {
        _deliveryDestination.value = stop
    }

    // Wishlist / Favorites
    private val _favoritePizzaIds = MutableStateFlow<Set<String>>(setOf("p_margherita", "p_pepperoni"))
    val favoritePizzaIds: StateFlow<Set<String>> = _favoritePizzaIds.asStateFlow()

    // Actions
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: PizzaCategory) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(pizzaId: String) {
        val current = _favoritePizzaIds.value.toMutableSet()
        if (current.contains(pizzaId)) {
            current.remove(pizzaId)
        } else {
            current.add(pizzaId)
        }
        _favoritePizzaIds.value = current
    }

    fun openPizzaDetail(pizza: Pizza) {
        _selectedPizzaForDetail.value = pizza
        _draftSize.value = PizzaSize.MEDIUM
        _draftCrust.value = PizzaCrust.CLASSIC
        // A signature pizza opens with its own toppings already on it.
        _draftToppings.value = AvailableToppings.byIds(pizza.defaultToppingIds)
        _draftQuantity.value = 1
    }

    fun selectDraftSize(size: PizzaSize) {
        _draftSize.value = size
    }

    fun selectDraftCrust(crust: PizzaCrust) {
        _draftCrust.value = crust
    }

    fun toggleDraftTopping(topping: Topping) {
        val current = _draftToppings.value.toMutableSet()
        if (current.contains(topping)) {
            current.remove(topping)
        } else {
            current.add(topping)
        }
        _draftToppings.value = current
    }

    fun incrementDraftQuantity() {
        _draftQuantity.value = (_draftQuantity.value + 1).coerceAtMost(10)
    }

    fun decrementDraftQuantity() {
        _draftQuantity.value = (_draftQuantity.value - 1).coerceAtLeast(1)
    }

    fun addDraftToCart() {
        val pizza = _selectedPizzaForDetail.value ?: return
        val item = CartItem(
            pizza = pizza,
            size = _draftSize.value,
            crust = _draftCrust.value,
            selectedToppings = _draftToppings.value.toList(),
            quantity = _draftQuantity.value
        )
        _cartItems.value = _cartItems.value + item
    }

    fun quickAdd(pizza: Pizza) {
        val item = CartItem(
            pizza = pizza,
            size = PizzaSize.MEDIUM,
            crust = PizzaCrust.CLASSIC,
            selectedToppings = emptyList(),
            quantity = 1
        )
        _cartItems.value = _cartItems.value + item
    }

    fun updateCartItemQuantity(cartItemId: String, delta: Int) {
        val updated = _cartItems.value.mapNotNull { item ->
            if (item.id == cartItemId) {
                val newQty = item.quantity + delta
                if (newQty > 0) item.copy(quantity = newQty) else null
            } else {
                item
            }
        }
        _cartItems.value = updated
    }

    fun removeCartItem(cartItemId: String) {
        _cartItems.value = _cartItems.value.filter { it.id != cartItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
    }

    fun applyCoupon(couponCode: String): Boolean {
        return if (couponCode.equals("PIZZA20", ignoreCase = true) || couponCode.equals("PIZZA40", ignoreCase = true)) {
            _appliedCoupon.value = couponCode.uppercase()
            true
        } else {
            false
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun placeOrder(address: String, paymentMethod: String): Order {
        val currentItems = _cartItems.value
        val summary = billSummary.value

        // The order goes wherever the customer has chosen to have it delivered.
        val stop = _deliveryDestination.value

        val order = Order(
            items = currentItems,
            subtotal = summary.subtotal,
            deliveryFee = summary.deliveryFee,
            tax = summary.tax,
            discount = summary.discount,
            grandTotal = summary.grandTotal,
            deliveryAddress = stop.address,
            paymentMethod = paymentMethod,
            status = OrderStatus.CONFIRMED
        )

        _activeOrder.value = order
        _cartItems.value = emptyList() // clear cart
        loadDeliveryRoute(stop)
        startTrackingDemoProgression()

        return order
    }

    /**
     * Resolves the shortest road route from the outlet to the drop-off.
     *
     * Kept off the placeOrder call path so a slow or unreachable routing service can
     * never delay the order being confirmed — the map fills in when the route lands,
     * and falls back to an approximate path if the request fails.
     */
    private fun loadDeliveryRoute(stop: DeliveryStop) {
        routeJob?.cancel()
        _isRouteLoading.value = true
        routeJob = viewModelScope.launch {
            val route = RouteRepository.shortestRoute(DeliveryGeo.store.point, stop.point)
            _deliveryRoute.value = route
            _isRouteLoading.value = false
        }
    }

    fun setTrackingStage(newStatus: OrderStatus) {
        val current = _activeOrder.value ?: return
        val updated = current.copy(status = newStatus)
        _activeOrder.value = updated

        if (newStatus == OrderStatus.DELIVERED) {
            _pastOrders.value = listOf(updated) + _pastOrders.value
        }
    }

    fun startTrackingDemoProgression() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            // Stage 1: Confirmed
            setTrackingStage(OrderStatus.CONFIRMED)
            delay(5000)

            // Stage 2: Preparing
            setTrackingStage(OrderStatus.PREPARING)
            delay(5000)

            // Stage 3: Out for Delivery
            setTrackingStage(OrderStatus.OUT_FOR_DELIVERY)
            delay(5000)

            // Stage 4: Delivered
            setTrackingStage(OrderStatus.DELIVERED)
        }
    }

    fun stopTrackingDemo() {
        trackingJob?.cancel()
        trackingJob = null
    }

    fun clearActiveOrder() {
        val current = _activeOrder.value
        if (current != null && !_pastOrders.value.any { it.id == current.id }) {
            _pastOrders.value = listOf(current) + _pastOrders.value
        }
        _activeOrder.value = null
        trackingJob?.cancel()
    }

    fun reorder(order: Order) {
        _cartItems.value = _cartItems.value + order.items
    }
}
