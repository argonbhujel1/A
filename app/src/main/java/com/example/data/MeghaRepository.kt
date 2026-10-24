package com.example.data

import android.content.Context
import com.example.data.remote.ApiClient
import com.example.data.remote.RemoteOrderItem
import com.example.data.remote.SubmitOrderRequest
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.notification.MeghaNotificationHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class MeghaRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("megha_hotel_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    val notificationHelper = MeghaNotificationHelper.getInstance(context)

    // Language state
    private val _language = MutableStateFlow(
        if (prefs.getString("pref_language", "en") == "ne") AppLanguage.NEPALI else AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("pref_language", lang.code).apply()
    }

    // Menu Categories
    private val _categories = MutableStateFlow<List<FoodCategory>>(MeghaHotelDefaults.categories)
    val categories: StateFlow<List<FoodCategory>> = _categories.asStateFlow()

    // Menu Items
    private val _menuItems = MutableStateFlow<List<FoodItem>>(loadSavedMenuItems())
    val menuItems: StateFlow<List<FoodItem>> = _menuItems.asStateFlow()

    // Shopping Cart
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Coupons & Active Promotion
    private val _coupons = MutableStateFlow<List<PromotionCoupon>>(MeghaHotelDefaults.defaultCoupons)
    val coupons: StateFlow<List<PromotionCoupon>> = _coupons.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<PromotionCoupon?>(null)
    val appliedCoupon: StateFlow<PromotionCoupon?> = _appliedCoupon.asStateFlow()

    // Delivery & Restaurant Settings
    private val _deliverySettings = MutableStateFlow(loadDeliverySettings())
    val deliverySettings: StateFlow<DeliverySettings> = _deliverySettings.asStateFlow()

    // Splash Configuration
    private val _splashConfig = MutableStateFlow(loadSplashConfig())
    val splashConfig: StateFlow<SplashConfig> = _splashConfig.asStateFlow()

    // User Session
    private val _currentUser = MutableStateFlow<UserProfile?>(loadSavedUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Orders List
    private val _orders = MutableStateFlow<List<Order>>(loadSavedOrders())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        // Seed initial orders if empty so customer and staff have meaningful data to inspect
        if (_orders.value.isEmpty()) {
            val initialDemoOrders = createInitialDemoOrders()
            _orders.value = initialDemoOrders
            saveOrders(initialDemoOrders)
        }
    }

    // --- MENU ITEM OPERATIONS ---
    fun addMenuItem(item: FoodItem) {
        val updated = _menuItems.value + item
        _menuItems.value = updated
        saveMenuItems(updated)
    }

    fun updateMenuItem(item: FoodItem) {
        val updated = _menuItems.value.map { if (it.id == item.id) item else it }
        _menuItems.value = updated
        saveMenuItems(updated)
    }

    fun deleteMenuItem(itemId: String) {
        val updated = _menuItems.value.filterNot { it.id == itemId }
        _menuItems.value = updated
        saveMenuItems(updated)
    }

    fun resetMenuToDefaults() {
        val defaults = MeghaHotelDefaults.initialMenuItems
        _menuItems.value = defaults
        saveMenuItems(defaults)
    }

    private fun saveMenuItems(items: List<FoodItem>) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("nameEn", item.nameEn)
                put("nameNp", item.nameNp)
                put("descriptionEn", item.descriptionEn)
                put("descriptionNp", item.descriptionNp)
                put("categoryId", item.categoryId)
                put("priceNpr", item.priceNpr)
                put("discountPriceNpr", item.discountPriceNpr ?: JSONObject.NULL)
                put("imageUrl", item.imageUrl)
                put("isVeg", item.isVeg)
                put("isPopular", item.isPopular)
                put("isFeatured", item.isFeatured)
                put("isAvailable", item.isAvailable)
                put("prepTime", item.preparationTimeMinutes)
                put("stock", item.stockQuantity)
                put("lastUpdated", item.lastUpdated)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_menu_items", jsonArray.toString()).apply()
    }

    private fun loadSavedMenuItems(): List<FoodItem> {
        val saved = prefs.getString("saved_menu_items", null) ?: return MeghaHotelDefaults.initialMenuItems
        return try {
            val jsonArray = JSONArray(saved)
            val list = mutableListOf<FoodItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    FoodItem(
                        id = obj.getString("id"),
                        nameEn = obj.getString("nameEn"),
                        nameNp = obj.optString("nameNp", ""),
                        descriptionEn = obj.optString("descriptionEn", ""),
                        descriptionNp = obj.optString("descriptionNp", ""),
                        categoryId = obj.getString("categoryId"),
                        priceNpr = obj.getDouble("priceNpr"),
                        discountPriceNpr = if (obj.isNull("discountPriceNpr")) null else obj.getDouble("discountPriceNpr"),
                        imageUrl = obj.getString("imageUrl"),
                        isVeg = obj.optBoolean("isVeg", false),
                        isPopular = obj.optBoolean("isPopular", false),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        isAvailable = obj.optBoolean("isAvailable", true),
                        preparationTimeMinutes = obj.optInt("prepTime", 15),
                        stockQuantity = obj.optInt("stock", 50),
                        lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                    )
                )
            }
            if (list.isNotEmpty()) list else MeghaHotelDefaults.initialMenuItems
        } catch (e: Exception) {
            MeghaHotelDefaults.initialMenuItems
        }
    }

    // --- CART OPERATIONS ---
    fun addToCart(foodItem: FoodItem, quantity: Int = 1, specialInstructions: String = "") {
        if (!foodItem.isAvailable) return
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.foodItem.id == foodItem.id }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + quantity,
                specialInstructions = if (specialInstructions.isNotBlank()) specialInstructions else existing.specialInstructions
            )
        } else {
            current.add(CartItem(foodItem, quantity, specialInstructions))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(foodItemId: String, quantity: Int) {
        val current = _cartItems.value.toMutableList()
        if (quantity <= 0) {
            current.removeAll { it.foodItem.id == foodItemId }
        } else {
            val index = current.indexOfFirst { it.foodItem.id == foodItemId }
            if (index >= 0) {
                current[index] = current[index].copy(quantity = quantity)
            }
        }
        _cartItems.value = current
    }

    fun removeFromCart(foodItemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.foodItem.id == foodItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
    }

    // --- COUPON OPERATIONS ---
    fun applyCoupon(code: String): Result<PromotionCoupon> {
        val trimmed = code.trim().uppercase()
        val coupon = _coupons.value.find { it.code.uppercase() == trimmed && it.isActive }
            ?: return Result.failure(Exception("Invalid or expired coupon code"))

        val subtotal = calculateSubtotal()
        if (subtotal < coupon.minOrderAmountNpr) {
            return Result.failure(Exception("Minimum order of Rs. ${coupon.minOrderAmountNpr.toInt()} required for this coupon"))
        }

        _appliedCoupon.value = coupon
        return Result.success(coupon)
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // --- PRICE CALCULATIONS ---
    fun calculateSubtotal(): Double {
        return _cartItems.value.sumOf { it.totalAmount }
    }

    fun calculateDiscount(): Double {
        val coupon = _appliedCoupon.value ?: return 0.0
        val subtotal = calculateSubtotal()
        return if (coupon.discountPercentage > 0) {
            (subtotal * coupon.discountPercentage) / 100.0
        } else {
            min(coupon.flatDiscountNpr, subtotal)
        }
    }

    fun calculateDeliveryCharge(customerLat: Double?, customerLng: Double?): Double {
        val settings = _deliverySettings.value
        val subtotal = calculateSubtotal()
        if (subtotal >= settings.freeDeliveryOverNpr) return 0.0

        if (customerLat == null || customerLng == null) {
            return settings.fixedDeliveryChargeNpr
        }

        val distanceKm = calculateDistanceKm(
            settings.restaurantLat, settings.restaurantLng,
            customerLat, customerLng
        )

        // 2km radius is FREE delivery (Rs. 0)
        return if (distanceKm <= settings.freeDeliveryRadiusKm) {
            0.0
        } else {
            // Beyond 2km, fixed delivery charge is Rs. 100
            settings.fixedDeliveryChargeNpr
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // --- ORDER SUBMISSION & LIFECYCLE ---
    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        deliveryAddress: String,
        locality: String,
        landmark: String,
        deliveryNotes: String,
        specialInstructions: String,
        orderType: String,
        paymentMethod: PaymentMethod,
        customerLat: Double?,
        customerLng: Double?,
        paymentProofRef: String? = null
    ): Result<Order> {
        val cart = _cartItems.value
        if (cart.isEmpty()) return Result.failure(Exception("Cart is empty"))

        val subtotal = calculateSubtotal()
        if (subtotal < _deliverySettings.value.minOrderAmountNpr) {
            return Result.failure(Exception("न्यूनतम अर्डर रु. ५०० हुनुपर्छ। (Minimum order amount is Rs. 500)"))
        }

        // Validate delivery area: Urlabari & Damak
        if (orderType == "delivery") {
            val validAreas = listOf("urlabari", "damak", "उर्लाबारी", "दमक")
            val isAreaMatched = validAreas.any { area ->
                locality.lowercase().contains(area) || deliveryAddress.lowercase().contains(area)
            }
            val distanceKm = if (customerLat != null && customerLng != null) {
                calculateDistanceKm(
                    _deliverySettings.value.restaurantLat,
                    _deliverySettings.value.restaurantLng,
                    customerLat,
                    customerLng
                )
            } else 0.0

            if (!isAreaMatched && distanceKm > 15.0) {
                return Result.failure(Exception("डेलिभरी सेवा उर्लाबारी र दमकमा मात्र उपलब्ध छ। (Delivery is only available in Urlabari and Damak)"))
            }
        }

        val discount = calculateDiscount()
        val deliveryFee = if (orderType == "delivery") calculateDeliveryCharge(customerLat, customerLng) else 0.0
        val total = max(0.0, subtotal - discount + deliveryFee)

        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomSuffix = UUID.randomUUID().toString().take(4).uppercase()
        val orderNumber = "MH-$dateCode-$randomSuffix"
        val trackingToken = "trk_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val orderId = "ord_${System.currentTimeMillis()}"

        val snapshots = cart.map {
            OrderItemSnapshot(
                foodItemId = it.foodItem.id,
                nameEn = it.foodItem.nameEn,
                nameNp = it.foodItem.nameNp,
                imageUrl = it.foodItem.imageUrl,
                unitPriceNpr = it.foodItem.effectivePrice,
                quantity = it.quantity,
                specialInstructions = it.specialInstructions
            )
        }

        val order = Order(
            orderId = orderId,
            orderNumber = orderNumber,
            trackingToken = trackingToken,
            customerName = customerName,
            customerPhone = customerPhone,
            customerEmail = customerEmail,
            deliveryAddress = deliveryAddress,
            locality = locality,
            landmark = landmark,
            deliveryNotes = deliveryNotes,
            specialInstructions = specialInstructions,
            orderType = orderType,
            items = snapshots,
            subtotalNpr = subtotal,
            discountNpr = discount,
            deliveryFeeNpr = deliveryFee,
            totalPayableNpr = total,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.QR && paymentProofRef != null) PaymentStatus.PENDING_VERIFICATION else PaymentStatus.PENDING_VERIFICATION,
            paymentProofReference = paymentProofRef,
            orderStatus = OrderStatus.PENDING,
            customerLat = customerLat,
            customerLng = customerLng,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Attempt submission to real Flask backend endpoint
        val remoteItems = snapshots.mapNotNull { snapshot ->
            snapshot.foodItemId.toIntOrNull()?.let { idInt ->
                RemoteOrderItem(
                    menuItemId = idInt,
                    quantity = snapshot.quantity,
                    specialInstructions = snapshot.specialInstructions.ifBlank { null }
                )
            }
        }

        if (remoteItems.isNotEmpty() && customerEmail.isNotBlank()) {
            try {
                val req = SubmitOrderRequest(
                    items = remoteItems,
                    orderType = orderType,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerEmail = customerEmail,
                    deliveryAddress = deliveryAddress,
                    deliveryNotes = deliveryNotes.ifBlank { null },
                    deliveryFeePaisa = (deliveryFee * 100).toInt(),
                    customerLat = customerLat?.toString(),
                    customerLng = customerLng?.toString(),
                    specialInstructions = specialInstructions.ifBlank { null }
                )
                ApiClient.apiService.submitOrder(
                    idempotencyKey = UUID.randomUUID().toString(),
                    request = req
                )
            } catch (e: Exception) {
                // Non-fatal: if offline or backend returns validation warning, local persistence still guarantees complete ordering workflow
            }
        }

        // Save order locally
        val updatedOrders = listOf(order) + _orders.value
        _orders.value = updatedOrders
        saveOrders(updatedOrders)

        // Clear shopping cart
        clearCart()

        // Trigger notifications
        notificationHelper.showOrderPlacedNotification(order, _language.value)
        notificationHelper.showStaffOrderAlert(order, "Kitchen & Staff")

        return Result.success(order)
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val updated = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(orderStatus = newStatus, updatedAt = System.currentTimeMillis())
            } else it
        }
        _orders.value = updated
        saveOrders(updated)
        updated.find { it.orderId == orderId }?.let {
            notificationHelper.showOrderStatusNotification(it, _language.value)
        }
    }

    fun assignRiderToOrder(orderId: String, riderId: String, riderName: String, riderPhone: String) {
        val restaurantLat = _deliverySettings.value.restaurantLat
        val restaurantLng = _deliverySettings.value.restaurantLng
        val updated = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(
                    orderStatus = OrderStatus.ASSIGNED,
                    assignedRiderId = riderId,
                    assignedRiderName = riderName,
                    assignedRiderPhone = riderPhone,
                    riderLat = restaurantLat,
                    riderLng = restaurantLng,
                    lastRiderUpdate = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        _orders.value = updated
        saveOrders(updated)
        updated.find { it.orderId == orderId }?.let {
            notificationHelper.showOrderStatusNotification(it, _language.value)
        }
    }

    fun updateRiderLocation(orderId: String, lat: Double, lng: Double) {
        val updated = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(
                    riderLat = lat,
                    riderLng = lng,
                    lastRiderUpdate = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        _orders.value = updated
        saveOrders(updated)
    }

    fun verifyOrderPayment(orderId: String, verified: Boolean) {
        val updated = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(
                    paymentStatus = if (verified) PaymentStatus.VERIFIED else PaymentStatus.PENDING_VERIFICATION,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }
        _orders.value = updated
        saveOrders(updated)
    }

    fun getOrderByToken(token: String): Order? {
        return _orders.value.find { it.trackingToken == token }
    }

    fun getOrderByNumber(orderNumber: String): Order? {
        val clean = orderNumber.trim().uppercase()
        return _orders.value.find { it.orderNumber.uppercase() == clean }
    }

    fun sendTestNotification() {
        notificationHelper.sendTestNotification(_language.value)
    }

    fun hasNotificationPermission(): Boolean {
        return notificationHelper.hasNotificationPermission()
    }

    private fun saveOrders(orders: List<Order>) {
        val arr = JSONArray()
        orders.forEach { o ->
            val obj = JSONObject().apply {
                put("orderId", o.orderId)
                put("orderNumber", o.orderNumber)
                put("trackingToken", o.trackingToken)
                put("customerName", o.customerName)
                put("customerPhone", o.customerPhone)
                put("customerEmail", o.customerEmail)
                put("deliveryAddress", o.deliveryAddress)
                put("locality", o.locality)
                put("landmark", o.landmark)
                put("deliveryNotes", o.deliveryNotes)
                put("specialInstructions", o.specialInstructions)
                put("orderType", o.orderType)
                put("subtotalNpr", o.subtotalNpr)
                put("discountNpr", o.discountNpr)
                put("deliveryFeeNpr", o.deliveryFeeNpr)
                put("totalPayableNpr", o.totalPayableNpr)
                put("paymentMethod", o.paymentMethod.name)
                put("paymentStatus", o.paymentStatus.name)
                put("paymentProofRef", o.paymentProofReference ?: JSONObject.NULL)
                put("orderStatus", o.orderStatus.name)
                put("customerLat", o.customerLat ?: JSONObject.NULL)
                put("customerLng", o.customerLng ?: JSONObject.NULL)
                put("assignedRiderId", o.assignedRiderId ?: JSONObject.NULL)
                put("assignedRiderName", o.assignedRiderName ?: JSONObject.NULL)
                put("assignedRiderPhone", o.assignedRiderPhone ?: JSONObject.NULL)
                put("riderLat", o.riderLat ?: JSONObject.NULL)
                put("riderLng", o.riderLng ?: JSONObject.NULL)
                put("lastRiderUpdate", o.lastRiderUpdate ?: JSONObject.NULL)
                put("createdAt", o.createdAt)
                put("updatedAt", o.updatedAt)

                val itemsArr = JSONArray()
                o.items.forEach { item ->
                    itemsArr.put(JSONObject().apply {
                        put("foodItemId", item.foodItemId)
                        put("nameEn", item.nameEn)
                        put("nameNp", item.nameNp)
                        put("imageUrl", item.imageUrl)
                        put("unitPriceNpr", item.unitPriceNpr)
                        put("quantity", item.quantity)
                        put("specialInstructions", item.specialInstructions)
                    })
                }
                put("items", itemsArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString("saved_orders", arr.toString()).apply()
    }

    private fun loadSavedOrders(): List<Order> {
        val saved = prefs.getString("saved_orders", null) ?: return emptyList()
        return try {
            val arr = JSONArray(saved)
            val list = mutableListOf<Order>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val itemsArr = o.getJSONArray("items")
                val itemsList = mutableListOf<OrderItemSnapshot>()
                for (j in 0 until itemsArr.length()) {
                    val it = itemsArr.getJSONObject(j)
                    itemsList.add(
                        OrderItemSnapshot(
                            foodItemId = it.getString("foodItemId"),
                            nameEn = it.getString("nameEn"),
                            nameNp = it.optString("nameNp", ""),
                            imageUrl = it.getString("imageUrl"),
                            unitPriceNpr = it.getDouble("unitPriceNpr"),
                            quantity = it.getInt("quantity"),
                            specialInstructions = it.optString("specialInstructions", "")
                        )
                    )
                }

                list.add(
                    Order(
                        orderId = o.getString("orderId"),
                        orderNumber = o.getString("orderNumber"),
                        trackingToken = o.getString("trackingToken"),
                        customerName = o.getString("customerName"),
                        customerPhone = o.getString("customerPhone"),
                        customerEmail = o.optString("customerEmail", ""),
                        deliveryAddress = o.getString("deliveryAddress"),
                        locality = o.optString("locality", "Urlabari"),
                        landmark = o.optString("landmark", ""),
                        deliveryNotes = o.optString("deliveryNotes", ""),
                        specialInstructions = o.optString("specialInstructions", ""),
                        orderType = o.optString("orderType", "delivery"),
                        items = itemsList,
                        subtotalNpr = o.getDouble("subtotalNpr"),
                        discountNpr = o.optDouble("discountNpr", 0.0),
                        deliveryFeeNpr = o.optDouble("deliveryFeeNpr", 0.0),
                        totalPayableNpr = o.getDouble("totalPayableNpr"),
                        paymentMethod = try { PaymentMethod.valueOf(o.getString("paymentMethod")) } catch (e: Exception) { PaymentMethod.COD },
                        paymentStatus = try { PaymentStatus.valueOf(o.getString("paymentStatus")) } catch (e: Exception) { PaymentStatus.PENDING_VERIFICATION },
                        paymentProofReference = if (o.isNull("paymentProofRef")) null else o.getString("paymentProofRef"),
                        orderStatus = try { OrderStatus.valueOf(o.getString("orderStatus")) } catch (e: Exception) { OrderStatus.PENDING },
                        customerLat = if (o.isNull("customerLat")) null else o.getDouble("customerLat"),
                        customerLng = if (o.isNull("customerLng")) null else o.getDouble("customerLng"),
                        assignedRiderId = if (o.isNull("assignedRiderId")) null else o.getString("assignedRiderId"),
                        assignedRiderName = if (o.isNull("assignedRiderName")) null else o.getString("assignedRiderName"),
                        assignedRiderPhone = if (o.isNull("assignedRiderPhone")) null else o.getString("assignedRiderPhone"),
                        riderLat = if (o.isNull("riderLat")) null else o.getDouble("riderLat"),
                        riderLng = if (o.isNull("riderLng")) null else o.getDouble("riderLng"),
                        lastRiderUpdate = if (o.isNull("lastRiderUpdate")) null else o.getLong("lastRiderUpdate"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun createInitialDemoOrders(): List<Order> {
        val sampleItem1 = MeghaHotelDefaults.initialMenuItems[1] // Steam momo
        val sampleItem2 = MeghaHotelDefaults.initialMenuItems[4] // Chicken Chowmein

        val snapshot1 = OrderItemSnapshot(
            foodItemId = sampleItem1.id,
            nameEn = sampleItem1.nameEn,
            nameNp = sampleItem1.nameNp,
            imageUrl = sampleItem1.imageUrl,
            unitPriceNpr = sampleItem1.priceNpr,
            quantity = 2
        )
        val snapshot2 = OrderItemSnapshot(
            foodItemId = sampleItem2.id,
            nameEn = sampleItem2.nameEn,
            nameNp = sampleItem2.nameNp,
            imageUrl = sampleItem2.imageUrl,
            unitPriceNpr = sampleItem2.priceNpr,
            quantity = 1
        )

        return listOf(
            Order(
                orderId = "ord_demo_active",
                orderNumber = "MH-20261024-LIVE",
                trackingToken = "trk_demo_active_live1",
                customerName = "Ramesh Adhikari",
                customerPhone = "9842012345",
                customerEmail = "ramesh@example.com",
                deliveryAddress = "Urlabari Chowk, near Red Cross Office",
                locality = "Urlabari-7",
                landmark = "Red Cross Building",
                deliveryNotes = "Please call when you reach the gate",
                orderType = "delivery",
                items = listOf(snapshot1, snapshot2),
                subtotalNpr = 500.0,
                discountNpr = 50.0,
                deliveryFeeNpr = 50.0,
                totalPayableNpr = 500.0,
                paymentMethod = PaymentMethod.COD,
                paymentStatus = PaymentStatus.PENDING_VERIFICATION,
                orderStatus = OrderStatus.ON_THE_WAY,
                customerLat = 26.669800,
                customerLng = 87.639500,
                assignedRiderId = "rider_bikash",
                assignedRiderName = "Bikash Rai",
                assignedRiderPhone = "9819329559",
                riderLat = 26.667200,
                riderLng = 87.638400,
                lastRiderUpdate = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis() - 25 * 60 * 1000,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- AUTHENTICATION & USER SESSIONS ---
    fun login(email: String, password: String):Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()

        // Admin account: admin@megahotel.com (password: megahotel@123)
        if ((cleanEmail == "admin@megahotel.com" || cleanEmail == "admin@meghahotel.np") &&
            (password == "megahotel@123" || password == "admin123")) {
            val user = UserProfile(
                id = "admin_01",
                email = cleanEmail,
                fullName = "Megha Hotel Manager",
                phone = "9819329559",
                role = UserRole.ADMIN,
                profileImageUrl = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792239079/megha-hotel/settings/oclrqkivkbfgfjt6awfi.png"
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        // Kitchen Staff account: kitchen@megahotel.com (password: megahotel@123)
        if ((cleanEmail == "kitchen@megahotel.com" || cleanEmail == "kitchen@meghahotel.np") &&
            (password == "megahotel@123" || password == "kitchen123")) {
            val user = UserProfile(
                id = "kitchen_01",
                email = cleanEmail,
                fullName = "Head Chef Prakash",
                phone = "9819329559",
                role = UserRole.KITCHEN
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        // Delivery Rider 1: rider@megahotel.com or rider1 / bikash (password: megahotel@123)
        if ((cleanEmail in setOf("rider@megahotel.com", "rider1@megahotel.com", "bikash@megahotel.com", "bikash@meghahotel.np")) &&
            (password == "megahotel@123" || password == "rider123")) {
            val user = UserProfile(
                id = "rider_bikash",
                email = cleanEmail,
                fullName = "Bikash Rai",
                phone = "9819329559",
                role = UserRole.RIDER
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        // Delivery Rider 2: rider2@megahotel.com or sandesh (password: megahotel@123)
        if ((cleanEmail in setOf("rider2@megahotel.com", "sandesh@megahotel.com", "sandesh@meghahotel.np")) &&
            (password == "megahotel@123" || password == "rider123")) {
            val user = UserProfile(
                id = "rider_sandesh",
                email = cleanEmail,
                fullName = "Sandesh Shrestha",
                phone = "9812387437",
                role = UserRole.RIDER
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        // Any generic rider email (rider*@megahotel.com) with password megahotel@123
        if ((cleanEmail.startsWith("rider") || cleanEmail.startsWith("driver")) &&
            cleanEmail.endsWith("@megahotel.com") && password == "megahotel@123") {
            val user = UserProfile(
                id = "rider_${UUID.randomUUID().toString().take(6)}",
                email = cleanEmail,
                fullName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() } + " (Rider)",
                phone = "9819329559",
                role = UserRole.RIDER
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        // Staff accounts must not fall through to customer login on wrong password
        val staffEmails = setOf(
            "admin@megahotel.com", "admin@meghahotel.np",
            "kitchen@megahotel.com", "kitchen@meghahotel.np",
            "rider@megahotel.com", "rider1@megahotel.com", "rider2@megahotel.com",
            "bikash@megahotel.com", "bikash@meghahotel.np",
            "sandesh@megahotel.com", "sandesh@meghahotel.np"
        )
        if (cleanEmail in staffEmails || (cleanEmail.endsWith("@megahotel.com") && (cleanEmail.startsWith("admin") || cleanEmail.startsWith("kitchen") || cleanEmail.startsWith("rider")))) {
            return Result.failure(Exception("गलत पासवर्ड! सबै कर्मचारी खाताको पासवर्ड 'megahotel@123' हो। (Invalid staff credentials)"))
        }

        // Customer login
        if (cleanEmail.contains("@") && password.length >= 6) {
            val user = UserProfile(
                id = "cust_${UUID.randomUUID().toString().take(8)}",
                email = cleanEmail,
                fullName = cleanEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                phone = "98XXXXXXXX",
                role = UserRole.CUSTOMER
            )
            _currentUser.value = user
            saveUser(user)
            return Result.success(user)
        }

        return Result.failure(Exception("Invalid email or password"))
    }

    fun continueAsGuest() {
        val guest = UserProfile(
            id = "guest_${UUID.randomUUID().toString().take(8)}",
            email = "",
            fullName = "Guest Customer",
            phone = "",
            role = UserRole.GUEST
        )
        _currentUser.value = guest
        saveUser(guest)
    }

    fun logout() {
        continueAsGuest()
    }

    fun updateUserProfile(name: String, phone: String, email: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            fullName = name.ifBlank { current.fullName },
            phone = phone.ifBlank { current.phone },
            email = email.ifBlank { current.email }
        )
        _currentUser.value = updated
        saveUser(updated)
    }

    fun addSavedAddress(address: SavedAddress) {
        val current = _currentUser.value ?: return
        val existing = current.savedAddresses.filterNot { it.id == address.id }
        val updatedList = if (address.isDefault) {
            existing.map { it.copy(isDefault = false) } + address
        } else {
            existing + address
        }
        val updatedUser = current.copy(savedAddresses = updatedList)
        _currentUser.value = updatedUser
        saveUser(updatedUser)
    }

    private fun saveUser(user: UserProfile) {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("email", user.email)
            put("fullName", user.fullName)
            put("phone", user.phone)
            put("role", user.role.name)
            put("profileImageUrl", user.profileImageUrl)
        }
        prefs.edit().putString("saved_user", obj.toString()).apply()
    }

    private fun loadSavedUser(): UserProfile {
        val saved = prefs.getString("saved_user", null)
        if (saved != null) {
            try {
                val obj = JSONObject(saved)
                return UserProfile(
                    id = obj.getString("id"),
                    email = obj.optString("email", ""),
                    fullName = obj.getString("fullName"),
                    phone = obj.optString("phone", ""),
                    role = UserRole.valueOf(obj.getString("role")),
                    profileImageUrl = obj.optString("profileImageUrl", "")
                )
            } catch (e: Exception) {
                // fall back to guest
            }
        }
        return UserProfile(
            id = "guest_default",
            email = "",
            fullName = "Guest Customer",
            phone = "",
            role = UserRole.GUEST
        )
    }

    // --- SPLASH SETTINGS ---
    fun updateSplashConfig(config: SplashConfig) {
        _splashConfig.value = config
        val obj = JSONObject().apply {
            put("imageUrl", config.imageUrl)
            put("title", config.title)
            put("subtitle", config.subtitle)
            put("durationSeconds", config.durationSeconds)
            put("backgroundColorHex", config.backgroundColorHex)
            put("creditText", config.creditText)
        }
        prefs.edit().putString("splash_config", obj.toString()).apply()
    }

    private fun loadSplashConfig(): SplashConfig {
        val saved = prefs.getString("splash_config", null) ?: return SplashConfig()
        return try {
            val obj = JSONObject(saved)
            SplashConfig(
                imageUrl = obj.optString("imageUrl", "https://res.cloudinary.com/nfolm8hx/image/upload/v1792239079/megha-hotel/settings/oclrqkivkbfgfjt6awfi.png"),
                title = obj.optString("title", "Megha Hotel"),
                subtitle = obj.optString("subtitle", "Good Food. Great Moments."),
                durationSeconds = obj.optInt("durationSeconds", 5),
                backgroundColorHex = obj.optString("backgroundColorHex", "#1B4332"),
                creditText = obj.optString("creditText", "Engineered by BAM Studio")
            )
        } catch (e: Exception) {
            SplashConfig()
        }
    }

    // --- DELIVERY SETTINGS ---
    fun updateDeliverySettings(settings: DeliverySettings) {
        _deliverySettings.value = settings
        val obj = JSONObject().apply {
            put("baseDeliveryChargeNpr", settings.baseDeliveryChargeNpr)
            put("freeDeliveryOverNpr", settings.freeDeliveryOverNpr)
            put("extraKmChargeNpr", settings.extraKmChargeNpr)
            put("maxRadiusKm", settings.maxRadiusKm)
            put("isRestaurantOpen", settings.isRestaurantOpen)
            put("restaurantPhone", settings.restaurantPhone)
            put("restaurantWhatsApp", settings.restaurantWhatsApp)
        }
        prefs.edit().putString("delivery_settings", obj.toString()).apply()
    }

    private fun loadDeliverySettings(): DeliverySettings {
        val saved = prefs.getString("delivery_settings", null) ?: return DeliverySettings()
        return try {
            val obj = JSONObject(saved)
            DeliverySettings(
                baseDeliveryChargeNpr = obj.optDouble("baseDeliveryChargeNpr", 50.0),
                freeDeliveryOverNpr = obj.optDouble("freeDeliveryOverNpr", 1000.0),
                extraKmChargeNpr = obj.optDouble("extraKmChargeNpr", 15.0),
                maxRadiusKm = obj.optDouble("maxRadiusKm", 5.0),
                isRestaurantOpen = obj.optBoolean("isRestaurantOpen", true),
                restaurantPhone = obj.optString("restaurantPhone", "9819329559"),
                restaurantWhatsApp = obj.optString("restaurantWhatsApp", "9779812387437")
            )
        } catch (e: Exception) {
            DeliverySettings()
        }
    }
}
