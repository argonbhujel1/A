package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MeghaRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenDestination {
    BAM_SPLASH,
    MEGA_SPLASH,
    AUTH_LOGIN,
    HOME,
    MENU,
    CART,
    CHECKOUT,
    ORDER_CONFIRMATION,
    ORDER_TRACKING,
    ORDERS_HISTORY,
    PROFILE,
    ADMIN_PANEL,
    KITCHEN_PANEL,
    RIDER_PANEL
}

class MeghaViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MeghaRepository(application)
    private val appPrefs = application.getSharedPreferences("megha_theme_prefs", Context.MODE_PRIVATE)

    // Dark/Light Theme state
    private val _isDarkTheme = MutableStateFlow(appPrefs.getBoolean("dark_theme_enabled", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        appPrefs.edit().putBoolean("dark_theme_enabled", next).apply()
    }

    // Navigation stack / current screen
    private val _currentScreen = MutableStateFlow(ScreenDestination.BAM_SPLASH)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<ScreenDestination>()

    // Splash display countdown states
    private val _bamCountdown = MutableStateFlow(4)
    val bamCountdown: StateFlow<Int> = _bamCountdown.asStateFlow()

    private val _megaCountdown = MutableStateFlow(4)
    val megaCountdown: StateFlow<Int> = _megaCountdown.asStateFlow()

    // Search and category filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null) // null = "All"
    val vegOnlyFilter = MutableStateFlow(false)

    // Active order for tracking / confirmation
    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    // Selected item for detail view
    private val _detailFoodItem = MutableStateFlow<FoodItem?>(null)
    val detailFoodItem: StateFlow<FoodItem?> = _detailFoodItem.asStateFlow()

    // Info Dialog state (for Social login coming-soon messages, alerts, etc.)
    private val _infoDialogMessage = MutableStateFlow<String?>(null)
    val infoDialogMessage: StateFlow<String?> = _infoDialogMessage.asStateFlow()

    // Delivery coordinates chosen for checkout
    val customerSelectedLat = MutableStateFlow(26.665420913346072)
    val customerSelectedLng = MutableStateFlow(87.63826396825684)

    // Repository flows exposed to UI
    val language: StateFlow<AppLanguage> = repository.language
    val menuItems: StateFlow<List<FoodItem>> = repository.menuItems
    val categories: StateFlow<List<FoodCategory>> = repository.categories
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
    val appliedCoupon: StateFlow<PromotionCoupon?> = repository.appliedCoupon
    val deliverySettings: StateFlow<DeliverySettings> = repository.deliverySettings
    val splashConfig: StateFlow<SplashConfig> = repository.splashConfig
    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val orders: StateFlow<List<Order>> = repository.orders

    // Filtered menu items
    val filteredMenuItems: StateFlow<List<FoodItem>> = combine(
        menuItems,
        searchQuery,
        selectedCategory,
        vegOnlyFilter
    ) { items, query, categoryId, vegOnly ->
        items.filter { item ->
            val matchesCategory = categoryId == null || item.categoryId.equals(categoryId, ignoreCase = true)
            val matchesVeg = !vegOnly || item.isVeg
            val cleanQuery = query.trim().lowercase()
            val matchesQuery = cleanQuery.isEmpty() ||
                    item.nameEn.lowercase().contains(cleanQuery) ||
                    item.nameNp.lowercase().contains(cleanQuery) ||
                    item.descriptionEn.lowercase().contains(cleanQuery) ||
                    item.descriptionNp.lowercase().contains(cleanQuery)

            matchesCategory && matchesVeg && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart totals
    val subtotalAmount: StateFlow<Double> = cartItems.map { list ->
        list.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val discountAmount: StateFlow<Double> = combine(subtotalAmount, appliedCoupon) { subtotal, coupon ->
        if (coupon == null) 0.0
        else if (coupon.discountPercentage > 0) (subtotal * coupon.discountPercentage) / 100.0
        else minOf(coupon.flatDiscountNpr, subtotal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryFeeAmount: StateFlow<Double> = combine(
        subtotalAmount,
        customerSelectedLat,
        customerSelectedLng,
        deliverySettings
    ) { subtotal, lat, lng, settings ->
        if (subtotal >= settings.freeDeliveryOverNpr) 0.0
        else repository.calculateDeliveryCharge(lat, lng)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val finalPayableAmount: StateFlow<Double> = combine(
        subtotalAmount,
        discountAmount,
        deliveryFeeAmount
    ) { subtotal, discount, fee ->
        maxOf(0.0, subtotal - discount + fee)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        // Cold launch sequence:
        // 1. BAM Studio Splash for 4 seconds
        // 2. Mega Hotel Splash for 4 seconds
        // 3. Auth / Login Screen
        viewModelScope.launch {
            // Stage 1: BAM Studio Splash (4 sec)
            for (sec in 4 downTo 1) {
                _bamCountdown.value = sec
                delay(1000)
            }
            if (_currentScreen.value == ScreenDestination.BAM_SPLASH) {
                _currentScreen.value = ScreenDestination.MEGA_SPLASH
            }

            // Stage 2: Mega Hotel Splash (4 sec)
            for (sec in 4 downTo 1) {
                _megaCountdown.value = sec
                delay(1000)
            }
            if (_currentScreen.value == ScreenDestination.MEGA_SPLASH) {
                _currentScreen.value = ScreenDestination.AUTH_LOGIN
            }
        }
    }

    fun dismissBAMSplash() {
        if (_currentScreen.value == ScreenDestination.BAM_SPLASH) {
            _currentScreen.value = ScreenDestination.MEGA_SPLASH
        }
    }

    fun dismissMegaSplash() {
        if (_currentScreen.value == ScreenDestination.MEGA_SPLASH) {
            _currentScreen.value = ScreenDestination.AUTH_LOGIN
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        if (_currentScreen.value != destination) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = destination
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
            return true
        } else if (_currentScreen.value != ScreenDestination.HOME &&
                   _currentScreen.value != ScreenDestination.BAM_SPLASH &&
                   _currentScreen.value != ScreenDestination.MEGA_SPLASH &&
                   _currentScreen.value != ScreenDestination.AUTH_LOGIN) {
            _currentScreen.value = ScreenDestination.HOME
            return true
        }
        return false
    }

    fun openFoodDetail(item: FoodItem) {
        _detailFoodItem.value = item
    }

    fun closeFoodDetail() {
        _detailFoodItem.value = null
    }

    fun showInfoDialog(msg: String) {
        _infoDialogMessage.value = msg
    }

    fun dismissInfoDialog() {
        _infoDialogMessage.value = null
    }

    fun toggleLanguage() {
        val next = if (language.value == AppLanguage.ENGLISH) AppLanguage.NEPALI else AppLanguage.ENGLISH
        repository.setLanguage(next)
    }

    fun viewOrderDetails(order: Order) {
        _activeOrder.value = order
        navigateTo(ScreenDestination.ORDER_TRACKING)
    }

    fun viewOrderConfirmation(order: Order) {
        _activeOrder.value = order
        navigateTo(ScreenDestination.ORDER_CONFIRMATION)
    }
}
