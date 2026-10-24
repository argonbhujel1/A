package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    menuItems: List<FoodItem>,
    categories: List<FoodCategory>,
    orders: List<Order>,
    splashConfig: SplashConfig,
    deliverySettings: DeliverySettings,
    language: AppLanguage,
    onAddFoodItem: (FoodItem) -> Unit,
    onUpdateFoodItem: (FoodItem) -> Unit,
    onDeleteFoodItem: (String) -> Unit,
    onResetMenuToDefaults: () -> Unit,
    onAssignRider: (orderId: String, riderId: String, riderName: String, riderPhone: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus) -> Unit,
    onVerifyPayment: (orderId: String, verified: Boolean) -> Unit,
    onUpdateSplashConfig: (SplashConfig) -> Unit,
    onUpdateDeliverySettings: (DeliverySettings) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Menu, 1: Orders, 2: Settings
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Control Panel",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = EmeraldPrimary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Menu (${menuItems.size})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Orders (${orders.size})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Settings", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_food_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Food Item")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // Menu Management Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All Dishes (${menuItems.size})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                OutlinedButton(
                                    onClick = onResetMenuToDefaults,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset from Website", fontSize = 11.sp)
                                }
                            }
                        }

                        items(menuItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.nameEn,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (item.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (item.isAvailable) "Available" else "Sold Out",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (item.isAvailable) Color(0xFF2E7D32) else RedBrand,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Category: ${item.categoryId} • Rs. ${item.effectivePrice.toInt()}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { itemToEdit = item }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary)
                                        }
                                        IconButton(onClick = { onDeleteFoodItem(item.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedBrand)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Orders Management Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = order.orderNumber,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary
                                                )
                                            )
                                            Text(
                                                text = "${order.customerName} • ${order.customerPhone}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }
                                        StatusBadge(status = order.orderStatus, language = language)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Address: ${order.deliveryAddress}, ${order.locality}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                    )

                                    Text(
                                        text = "Total: Rs. ${order.totalPayableNpr.toInt()} • Payment: ${order.paymentMethod.name} (${order.paymentStatus.name})",
                                        style = MaterialTheme.typography.bodySmall.copy(color = RedBrand, fontWeight = FontWeight.Bold)
                                    )

                                    if (order.paymentProofReference != null) {
                                        Text(
                                            text = "QR Txn ID: ${order.paymentProofReference}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                                    // Status update buttons & Rider assignment
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Assign Rider Dropdown / Buttons
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    onAssignRider(order.orderId, "rider_bikash", "Bikash Rai", "9819329559")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Assign Bikash", fontSize = 10.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onAssignRider(order.orderId, "rider_sandesh", "Sandesh Shrestha", "9812387437")
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Assign Sandesh", fontSize = 10.sp)
                                            }
                                        }

                                        // Status forward
                                        Button(
                                            onClick = {
                                                val nextStatus = when (order.orderStatus) {
                                                    OrderStatus.PENDING -> OrderStatus.CONFIRMED
                                                    OrderStatus.CONFIRMED -> OrderStatus.PREPARING
                                                    OrderStatus.PREPARING -> OrderStatus.READY
                                                    OrderStatus.READY -> OrderStatus.ON_THE_WAY
                                                    OrderStatus.ASSIGNED -> OrderStatus.ON_THE_WAY
                                                    OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
                                                    else -> OrderStatus.DELIVERED
                                                }
                                                onUpdateOrderStatus(order.orderId, nextStatus)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Next Status →", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Settings Tab (Splash, Delivery, Restaurant)
                    var splashTitle by remember { mutableStateOf(splashConfig.title) }
                    var splashSubtitle by remember { mutableStateOf(splashConfig.subtitle) }
                    var splashImage by remember { mutableStateOf(splashConfig.imageUrl) }
                    var baseDeliveryFee by remember { mutableStateOf(deliverySettings.baseDeliveryChargeNpr.toString()) }
                    var freeDeliveryThreshold by remember { mutableStateOf(deliverySettings.freeDeliveryOverNpr.toString()) }
                    var isRestaurantOpen by remember { mutableStateOf(deliverySettings.isRestaurantOpen) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Splash Screen Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = splashTitle,
                                        onValueChange = { splashTitle = it },
                                        label = { Text("Splash Title") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = splashSubtitle,
                                        onValueChange = { splashSubtitle = it },
                                        label = { Text("Splash Subtitle") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = splashImage,
                                        onValueChange = { splashImage = it },
                                        label = { Text("Splash Image URL") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onUpdateSplashConfig(
                                                splashConfig.copy(
                                                    title = splashTitle,
                                                    subtitle = splashSubtitle,
                                                    imageUrl = splashImage
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Save Splash Settings")
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Delivery Rules & Operations", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = baseDeliveryFee,
                                        onValueChange = { baseDeliveryFee = it },
                                        label = { Text("Base Delivery Fee (Rs.)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = freeDeliveryThreshold,
                                        onValueChange = { freeDeliveryThreshold = it },
                                        label = { Text("Free Delivery Over (Rs.)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Restaurant Open for Orders")
                                        Switch(
                                            checked = isRestaurantOpen,
                                            onCheckedChange = { isRestaurantOpen = it }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onUpdateDeliverySettings(
                                                deliverySettings.copy(
                                                    baseDeliveryChargeNpr = baseDeliveryFee.toDoubleOrNull() ?: 50.0,
                                                    freeDeliveryOverNpr = freeDeliveryThreshold.toDoubleOrNull() ?: 1000.0,
                                                    isRestaurantOpen = isRestaurantOpen
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Save Delivery Rules")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Food Item Dialog
    if (showAddDialog || itemToEdit != null) {
        val editing = itemToEdit
        var nameEn by remember { mutableStateOf(editing?.nameEn ?: "") }
        var nameNp by remember { mutableStateOf(editing?.nameNp ?: "") }
        var price by remember { mutableStateOf(editing?.priceNpr?.toInt()?.toString() ?: "") }
        var categoryId by remember { mutableStateOf(editing?.categoryId ?: "momo") }
        var descEn by remember { mutableStateOf(editing?.descriptionEn ?: "") }
        var imageUrl by remember { mutableStateOf(editing?.imageUrl ?: "") }
        var isVeg by remember { mutableStateOf(editing?.isVeg ?: false) }
        var isPopular by remember { mutableStateOf(editing?.isPopular ?: false) }
        var isAvailable by remember { mutableStateOf(editing?.isAvailable ?: true) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                itemToEdit = null
            },
            title = {
                Text(if (editing != null) "Edit Food Item" else "Add New Food Item", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = { Text("English Name *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = nameNp,
                            onValueChange = { nameNp = it },
                            label = { Text("Nepali Name (नेपाली नाम)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price in NPR (Rs.) *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = categoryId,
                            onValueChange = { categoryId = it },
                            label = { Text("Category (momo, chicken, snacks, etc.)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("Image URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = descEn,
                            onValueChange = { descEn = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isVeg, onCheckedChange = { isVeg = it })
                            Text("Vegetarian")
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isPopular, onCheckedChange = { isPopular = it })
                            Text("Mark Popular")
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAvailable, onCheckedChange = { isAvailable = it })
                            Text("Available in Stock")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = price.toDoubleOrNull() ?: 100.0
                        val newItem = FoodItem(
                            id = editing?.id ?: "food_${System.currentTimeMillis()}",
                            nameEn = nameEn.ifBlank { "Custom Dish" },
                            nameNp = nameNp,
                            descriptionEn = descEn,
                            descriptionNp = "",
                            categoryId = categoryId,
                            priceNpr = parsedPrice,
                            imageUrl = imageUrl.ifBlank { "https://meghahotel.vercel.app/static/uploads/menu/chicken-wings.jpg" },
                            isVeg = isVeg,
                            isPopular = isPopular,
                            isAvailable = isAvailable
                        )
                        if (editing != null) {
                            onUpdateFoodItem(newItem)
                        } else {
                            onAddFoodItem(newItem)
                        }
                        showAddDialog = false
                        itemToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Dish")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    itemToEdit = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun KitchenStaffScreen(
    orders: List<Order>,
    language: AppLanguage,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val kitchenQueue = remember(orders) {
        orders.filter {
            it.orderStatus == OrderStatus.PENDING ||
            it.orderStatus == OrderStatus.CONFIRMED ||
            it.orderStatus == OrderStatus.PREPARING ||
            it.orderStatus == OrderStatus.READY
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kitchen Order Queue",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        if (kitchenQueue.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SoupKitchen, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("No pending kitchen orders!", fontWeight = FontWeight.Bold)
                    Text("New food preparation orders will appear here automatically.", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(kitchenQueue) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                )
                                StatusBadge(status = order.orderStatus, language = language)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            order.items.forEach { it ->
                                Text(
                                    text = "• ${it.quantity}x ${it.nameEn}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (it.specialInstructions.isNotBlank()) {
                                    Text(
                                        text = "   Note: ${it.specialInstructions}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = RedBrand)
                                    )
                                }
                            }

                            if (order.specialInstructions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Order Note: ${order.specialInstructions}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (order.orderStatus == OrderStatus.PENDING || order.orderStatus == OrderStatus.CONFIRMED) {
                                    Button(
                                        onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.PREPARING) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusPreparing)
                                    ) {
                                        Text("Start Cooking")
                                    }
                                }

                                if (order.orderStatus == OrderStatus.PREPARING) {
                                    Button(
                                        onClick = { onUpdateOrderStatus(order.orderId, OrderStatus.READY) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Mark Ready for Pickup ✓")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiderStaffScreen(
    currentRider: UserProfile?,
    orders: List<Order>,
    language: AppLanguage,
    onUpdateOrderStatus: (orderId: String, status: OrderStatus) -> Unit,
    onUpdateRiderGps: (orderId: String, lat: Double, lng: Double) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assignedDeliveries = remember(orders, currentRider) {
        orders.filter {
            it.assignedRiderId == currentRider?.id ||
            it.orderStatus == OrderStatus.ASSIGNED ||
            it.orderStatus == OrderStatus.ON_THE_WAY
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Rider Delivery Console",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Text(
                            text = "Logged in: ${currentRider?.fullName ?: "Rider"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (assignedDeliveries.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Moped, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("No active assigned deliveries!", fontWeight = FontWeight.Bold)
                    Text("When the Admin assigns an order, it will appear here.", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(assignedDeliveries) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                )
                                StatusBadge(status = order.orderStatus, language = language)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Customer: ${order.customerName} (${order.customerPhone})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Address: ${order.deliveryAddress}, ${order.locality}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            if (order.deliveryNotes.isNotBlank()) {
                                Text(
                                    text = "Notes: ${order.deliveryNotes}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RedBrand)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Amount to collect: Rs. ${order.totalPayableNpr.toInt()} (${order.paymentMethod.name})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (order.orderStatus != OrderStatus.ON_THE_WAY && order.orderStatus != OrderStatus.DELIVERED) {
                                    Button(
                                        onClick = {
                                            onUpdateOrderStatus(order.orderId, OrderStatus.ON_THE_WAY)
                                            // Start GPS update
                                            onUpdateRiderGps(order.orderId, 26.666500, 87.638100)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                    ) {
                                        Text("Accept & Start Delivery")
                                    }
                                }

                                if (order.orderStatus == OrderStatus.ON_THE_WAY) {
                                    Button(
                                        onClick = {
                                            onUpdateOrderStatus(order.orderId, OrderStatus.DELIVERED)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text("Mark as Delivered ✓")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
