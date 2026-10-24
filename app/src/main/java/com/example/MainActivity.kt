package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.FoodDetailDialog
import com.example.ui.components.MeghaBottomNavBar
import com.example.ui.components.MeghaTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.MegaHotelTheme
import com.example.viewmodel.MeghaViewModel
import com.example.viewmodel.ScreenDestination
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MeghaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            MegaHotelTheme(darkTheme = isDarkTheme) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val bamCountdown by viewModel.bamCountdown.collectAsStateWithLifecycle()
                val megaCountdown by viewModel.megaCountdown.collectAsStateWithLifecycle()
                val language by viewModel.language.collectAsStateWithLifecycle()
                val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
                val filteredItems by viewModel.filteredMenuItems.collectAsStateWithLifecycle()
                val categories by viewModel.categories.collectAsStateWithLifecycle()
                val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
                val subtotal by viewModel.subtotalAmount.collectAsStateWithLifecycle()
                val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
                val deliveryFee by viewModel.deliveryFeeAmount.collectAsStateWithLifecycle()
                val finalTotal by viewModel.finalPayableAmount.collectAsStateWithLifecycle()
                val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
                val deliverySettings by viewModel.deliverySettings.collectAsStateWithLifecycle()
                val splashConfig by viewModel.splashConfig.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val orders by viewModel.orders.collectAsStateWithLifecycle()
                val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
                val detailItem by viewModel.detailFoodItem.collectAsStateWithLifecycle()
                val infoMessage by viewModel.infoDialogMessage.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
                val vegOnly by viewModel.vegOnlyFilter.collectAsStateWithLifecycle()
                val customerLat by viewModel.customerSelectedLat.collectAsStateWithLifecycle()
                val customerLng by viewModel.customerSelectedLng.collectAsStateWithLifecycle()

                val coroutineScope = rememberCoroutineScope()

                // Android 13+ Notification Permission Request
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            Toast.makeText(this@MainActivity, "Order alerts & notifications enabled ✓", Toast.LENGTH_SHORT).show()
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Hardware back press handling
                BackHandler(
                    enabled = currentScreen != ScreenDestination.HOME &&
                              currentScreen != ScreenDestination.BAM_SPLASH &&
                              currentScreen != ScreenDestination.MEGA_SPLASH &&
                              currentScreen != ScreenDestination.AUTH_LOGIN
                ) {
                    viewModel.navigateBack()
                }

                // Informational Modal for Coming Soon Social Login
                if (infoMessage != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissInfoDialog() },
                        title = {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "जानकारी" else "Notice",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        text = {
                            Text(
                                text = infoMessage!!,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.dismissInfoDialog() }
                            ) {
                                Text(if (language == AppLanguage.NEPALI) "बुझें (ठिक छ)" else "OK")
                            }
                        }
                    )
                }

                // Food Detail Dialog
                if (detailItem != null) {
                    FoodDetailDialog(
                        item = detailItem!!,
                        language = language,
                        onDismiss = { viewModel.closeFoodDetail() },
                        onAddToCart = { qty, instructions ->
                            viewModel.repository.addToCart(detailItem!!, qty, instructions)
                            Toast.makeText(this@MainActivity, "Added to cart ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Screen Routing
                when (currentScreen) {
                    ScreenDestination.BAM_SPLASH -> {
                        BAMSplashScreen(
                            countdown = bamCountdown,
                            onSkip = { viewModel.dismissBAMSplash() }
                        )
                    }

                    ScreenDestination.MEGA_SPLASH -> {
                        MegaHotelSplashScreen(
                            splashConfig = splashConfig,
                            countdown = megaCountdown,
                            onSkip = { viewModel.dismissMegaSplash() }
                        )
                    }

                    ScreenDestination.AUTH_LOGIN -> {
                        AuthLoginScreen(
                            language = language,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onContinueAsGuest = {
                                viewModel.repository.continueAsGuest()
                                viewModel.navigateTo(ScreenDestination.HOME)
                            },
                            onLogin = { email, pass ->
                                val res = viewModel.repository.login(email, pass)
                                res.onSuccess { user ->
                                    Toast.makeText(this@MainActivity, "Signed in as ${user.fullName} (${user.role.label})", Toast.LENGTH_SHORT).show()
                                    when (user.role) {
                                        UserRole.ADMIN -> viewModel.navigateTo(ScreenDestination.ADMIN_PANEL)
                                        UserRole.KITCHEN -> viewModel.navigateTo(ScreenDestination.KITCHEN_PANEL)
                                        UserRole.RIDER -> viewModel.navigateTo(ScreenDestination.RIDER_PANEL)
                                        else -> viewModel.navigateTo(ScreenDestination.HOME)
                                    }
                                }.onFailure { err ->
                                    Toast.makeText(this@MainActivity, err.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSocialUnavailable = {
                                viewModel.showInfoDialog("यो सुविधा आगामी अपडेटमा उपलब्ध हुनेछ। अहिले Guest का रूपमा अर्डर गर्नुहोस्।")
                            }
                        )
                    }

                    ScreenDestination.ADMIN_PANEL -> {
                        AdminDashboardScreen(
                            menuItems = menuItems,
                            categories = categories,
                            orders = orders,
                            splashConfig = splashConfig,
                            deliverySettings = deliverySettings,
                            language = language,
                            onAddFoodItem = { viewModel.repository.addMenuItem(it) },
                            onUpdateFoodItem = { viewModel.repository.updateMenuItem(it) },
                            onDeleteFoodItem = { viewModel.repository.deleteMenuItem(it) },
                            onResetMenuToDefaults = {
                                viewModel.repository.resetMenuToDefaults()
                                Toast.makeText(this@MainActivity, "Menu reset to website defaults", Toast.LENGTH_SHORT).show()
                            },
                            onAssignRider = { orderId, rId, rName, rPhone ->
                                viewModel.repository.assignRiderToOrder(orderId, rId, rName, rPhone)
                                Toast.makeText(this@MainActivity, "Assigned to $rName", Toast.LENGTH_SHORT).show()
                            },
                            onUpdateOrderStatus = { orderId, status ->
                                viewModel.repository.updateOrderStatus(orderId, status)
                            },
                            onVerifyPayment = { orderId, verified ->
                                viewModel.repository.verifyOrderPayment(orderId, verified)
                            },
                            onUpdateSplashConfig = {
                                viewModel.repository.updateSplashConfig(it)
                                Toast.makeText(this@MainActivity, "Splash settings updated", Toast.LENGTH_SHORT).show()
                            },
                            onUpdateDeliverySettings = {
                                viewModel.repository.updateDeliverySettings(it)
                                Toast.makeText(this@MainActivity, "Delivery settings updated", Toast.LENGTH_SHORT).show()
                            },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    ScreenDestination.KITCHEN_PANEL -> {
                        KitchenStaffScreen(
                            orders = orders,
                            language = language,
                            onUpdateOrderStatus = { orderId, status ->
                                viewModel.repository.updateOrderStatus(orderId, status)
                            },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    ScreenDestination.RIDER_PANEL -> {
                        RiderStaffScreen(
                            currentRider = currentUser,
                            orders = orders,
                            language = language,
                            onUpdateOrderStatus = { orderId, status ->
                                viewModel.repository.updateOrderStatus(orderId, status)
                            },
                            onUpdateRiderGps = { orderId, lat, lng ->
                                viewModel.repository.updateRiderLocation(orderId, lat, lng)
                            },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    ScreenDestination.CHECKOUT -> {
                        CheckoutScreen(
                            currentUser = currentUser,
                            subtotal = subtotal,
                            discount = discount,
                            deliveryFee = deliveryFee,
                            finalTotal = finalTotal,
                            customerLat = customerLat,
                            customerLng = customerLng,
                            language = language,
                            onLocationUpdate = { lat, lng ->
                                viewModel.customerSelectedLat.value = lat
                                viewModel.customerSelectedLng.value = lng
                            },
                            onPlaceOrder = { name, phone, email, address, locality, landmark, notes, special, orderType, paymentMethod, paymentProofRef ->
                                coroutineScope.launch {
                                    val result = viewModel.repository.placeOrder(
                                        customerName = name,
                                        customerPhone = phone,
                                        customerEmail = email,
                                        deliveryAddress = address,
                                        locality = locality,
                                        landmark = landmark,
                                        deliveryNotes = notes,
                                        specialInstructions = special,
                                        orderType = orderType,
                                        paymentMethod = paymentMethod,
                                        customerLat = customerLat,
                                        customerLng = customerLng,
                                        paymentProofRef = paymentProofRef
                                    )
                                    result.onSuccess { newOrder ->
                                        viewModel.viewOrderConfirmation(newOrder)
                                    }.onFailure { err ->
                                        Toast.makeText(this@MainActivity, err.message ?: "Order failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }

                    ScreenDestination.ORDER_CONFIRMATION -> {
                        if (activeOrder != null) {
                            OrderConfirmationScreen(
                                order = activeOrder!!,
                                language = language,
                                onTrackOrder = { viewModel.viewOrderDetails(activeOrder!!) },
                                onHomeClick = { viewModel.navigateTo(ScreenDestination.HOME) }
                            )
                        } else {
                            viewModel.navigateTo(ScreenDestination.HOME)
                        }
                    }

                    ScreenDestination.ORDER_TRACKING -> {
                        if (activeOrder != null) {
                            OrderTrackingScreen(
                                order = activeOrder!!,
                                language = language,
                                onBackClick = { viewModel.navigateBack() }
                            )
                        } else {
                            viewModel.navigateTo(ScreenDestination.ORDERS_HISTORY)
                        }
                    }

                    else -> {
                        // Main Tab Navigation Shell (HOME, MENU, CART, ORDERS, PROFILE)
                        val bottomNavRoute = when (currentScreen) {
                            ScreenDestination.HOME -> "home"
                            ScreenDestination.MENU -> "menu"
                            ScreenDestination.CART -> "cart"
                            ScreenDestination.ORDERS_HISTORY -> "orders"
                            ScreenDestination.PROFILE -> "profile"
                            else -> "home"
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                MeghaTopAppBar(
                                    title = "Megha Hotel",
                                    subtitle = "Urlabari, Morang",
                                    language = language,
                                    cartCount = cartItems.sumOf { it.quantity },
                                    currentUser = currentUser,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                    onLanguageToggle = { viewModel.toggleLanguage() },
                                    onCartClick = { viewModel.navigateTo(ScreenDestination.CART) },
                                    onProfileClick = { viewModel.navigateTo(ScreenDestination.PROFILE) }
                                )
                            },
                            bottomBar = {
                                MeghaBottomNavBar(
                                    currentRoute = bottomNavRoute,
                                    cartItemCount = cartItems.sumOf { it.quantity },
                                    onNavigate = { route ->
                                        when (route) {
                                            "home" -> viewModel.navigateTo(ScreenDestination.HOME)
                                            "menu" -> viewModel.navigateTo(ScreenDestination.MENU)
                                            "cart" -> viewModel.navigateTo(ScreenDestination.CART)
                                            "orders" -> viewModel.navigateTo(ScreenDestination.ORDERS_HISTORY)
                                            "profile" -> viewModel.navigateTo(ScreenDestination.PROFILE)
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentScreen) {
                                    ScreenDestination.HOME -> {
                                        HomeScreen(
                                            menuItems = menuItems,
                                            categories = categories,
                                            activeOrders = orders,
                                            language = language,
                                            searchQuery = searchQuery,
                                            onSearchChange = { viewModel.searchQuery.value = it },
                                            onCategoryClick = { catId ->
                                                viewModel.selectedCategory.value = catId
                                                viewModel.navigateTo(ScreenDestination.MENU)
                                            },
                                            onItemClick = { viewModel.openFoodDetail(it) },
                                            onAddToCart = { item ->
                                                viewModel.repository.addToCart(item, 1)
                                                Toast.makeText(this@MainActivity, "Added ${item.nameEn} to cart", Toast.LENGTH_SHORT).show()
                                            },
                                            onTrackOrderClick = { order ->
                                                viewModel.viewOrderDetails(order)
                                            },
                                            onViewAllMenuClick = {
                                                viewModel.selectedCategory.value = null
                                                viewModel.navigateTo(ScreenDestination.MENU)
                                            }
                                        )
                                    }

                                    ScreenDestination.MENU -> {
                                        MenuScreen(
                                            items = filteredItems,
                                            categories = categories,
                                            selectedCategory = selectedCategory,
                                            searchQuery = searchQuery,
                                            vegOnly = vegOnly,
                                            language = language,
                                            onCategorySelect = { viewModel.selectedCategory.value = it },
                                            onSearchChange = { viewModel.searchQuery.value = it },
                                            onVegOnlyToggle = { viewModel.vegOnlyFilter.value = it },
                                            onItemClick = { viewModel.openFoodDetail(it) },
                                            onAddToCart = { item ->
                                                viewModel.repository.addToCart(item, 1)
                                                Toast.makeText(this@MainActivity, "Added ${item.nameEn} to cart", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    ScreenDestination.CART -> {
                                        CartScreen(
                                            cartItems = cartItems,
                                            subtotal = subtotal,
                                            discount = discount,
                                            deliveryFee = deliveryFee,
                                            finalTotal = finalTotal,
                                            appliedCoupon = appliedCoupon,
                                            language = language,
                                            onUpdateQuantity = { id, qty ->
                                                viewModel.repository.updateCartQuantity(id, qty)
                                            },
                                            onRemoveItem = { id ->
                                                viewModel.repository.removeFromCart(id)
                                            },
                                            onApplyCoupon = { code ->
                                                val res = viewModel.repository.applyCoupon(code)
                                                res.onSuccess {
                                                    Toast.makeText(this@MainActivity, "Coupon ${it.code} applied!", Toast.LENGTH_SHORT).show()
                                                }.onFailure {
                                                    Toast.makeText(this@MainActivity, it.message ?: "Invalid coupon", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onRemoveCoupon = {
                                                viewModel.repository.removeCoupon()
                                            },
                                            onProceedToCheckout = {
                                                viewModel.navigateTo(ScreenDestination.CHECKOUT)
                                            },
                                            onBrowseMenu = {
                                                viewModel.navigateTo(ScreenDestination.MENU)
                                            }
                                        )
                                    }

                                    ScreenDestination.ORDERS_HISTORY -> {
                                        OrdersHistoryScreen(
                                            orders = orders,
                                            menuItems = menuItems,
                                            language = language,
                                            onTrackOrder = { order ->
                                                viewModel.viewOrderDetails(order)
                                            },
                                            onReorder = { order ->
                                                order.items.forEach { snapshot ->
                                                    val found = menuItems.find { it.id == snapshot.foodItemId }
                                                    if (found != null && found.isAvailable) {
                                                        viewModel.repository.addToCart(found, snapshot.quantity)
                                                    }
                                                }
                                                Toast.makeText(this@MainActivity, "Items added to cart", Toast.LENGTH_SHORT).show()
                                                viewModel.navigateTo(ScreenDestination.CART)
                                            },
                                            onBrowseMenu = {
                                                viewModel.navigateTo(ScreenDestination.MENU)
                                            }
                                        )
                                    }

                                    ScreenDestination.PROFILE -> {
                                        ProfileScreen(
                                            currentUser = currentUser,
                                            language = language,
                                            isDarkTheme = isDarkTheme,
                                            onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                                            onToggleLanguage = { viewModel.toggleLanguage() },
                                            onOpenAuthLogin = { viewModel.navigateTo(ScreenDestination.AUTH_LOGIN) },
                                            onLoginClick = { email, pass ->
                                                val res = viewModel.repository.login(email, pass)
                                                res.onSuccess { user ->
                                                    Toast.makeText(this@MainActivity, "Signed in as ${user.fullName} (${user.role.label})", Toast.LENGTH_SHORT).show()
                                                    if (user.role == UserRole.ADMIN) {
                                                        viewModel.navigateTo(ScreenDestination.ADMIN_PANEL)
                                                    } else if (user.role == UserRole.KITCHEN) {
                                                        viewModel.navigateTo(ScreenDestination.KITCHEN_PANEL)
                                                    } else if (user.role == UserRole.RIDER) {
                                                        viewModel.navigateTo(ScreenDestination.RIDER_PANEL)
                                                    }
                                                }.onFailure { err ->
                                                    Toast.makeText(this@MainActivity, err.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onLogoutClick = {
                                                viewModel.repository.logout()
                                                Toast.makeText(this@MainActivity, "Switched to Guest mode", Toast.LENGTH_SHORT).show()
                                            },
                                            onUpdateProfile = { name, phone, email ->
                                                viewModel.repository.updateUserProfile(name, phone, email)
                                                Toast.makeText(this@MainActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                                            },
                                            onAddAddress = { addr ->
                                                viewModel.repository.addSavedAddress(addr)
                                            },
                                            onOpenStaffPortal = {
                                                when (currentUser?.role) {
                                                    UserRole.ADMIN -> viewModel.navigateTo(ScreenDestination.ADMIN_PANEL)
                                                    UserRole.KITCHEN -> viewModel.navigateTo(ScreenDestination.KITCHEN_PANEL)
                                                    UserRole.RIDER -> viewModel.navigateTo(ScreenDestination.RIDER_PANEL)
                                                    else -> {}
                                                }
                                            },
                                            onSocialUnavailableClick = {
                                                viewModel.showInfoDialog("यो सुविधा आगामी अपडेटमा उपलब्ध हुनेछ। अहिले Guest का रूपमा अर्डर गर्नुहोस्।")
                                            },
                                            onTestNotification = {
                                                viewModel.repository.sendTestNotification()
                                                Toast.makeText(this@MainActivity, "Notification sent ✓ Check status bar!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
