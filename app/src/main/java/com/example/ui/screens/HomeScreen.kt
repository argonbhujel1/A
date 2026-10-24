package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.BAMStudioFooter
import com.example.ui.components.FoodItemCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    menuItems: List<FoodItem>,
    categories: List<FoodCategory>,
    activeOrders: List<Order>,
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategoryClick: (String?) -> Unit,
    onItemClick: (FoodItem) -> Unit,
    onAddToCart: (FoodItem) -> Unit,
    onTrackOrderClick: (Order) -> Unit,
    onViewAllMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeOngoingOrder = activeOrders.firstOrNull {
        it.orderStatus != OrderStatus.DELIVERED && it.orderStatus != OrderStatus.CANCELLED
    }

    val popularItems = remember(menuItems) {
        menuItems.filter { it.isPopular }.take(6)
    }

    val featuredItems = remember(menuItems) {
        menuItems.filter { it.isFeatured }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Restaurant Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(EmeraldPrimary, EmeraldLight)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "खुल्ला छ" else "OPEN NOW",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldDark
                                    )
                                )
                            }
                            Text(
                                text = "08:00 AM – 09:00 PM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (language == AppLanguage.NEPALI) "मेघा होटल तथा रेस्टुरेन्ट" else "Megha Hotel & Restaurant",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = if (language == AppLanguage.NEPALI) "उर्लाबारी, मोरङ • स्वादिलो तथा ताजा परिकार" else "Urlabari, Morang • Good Food. Great Moments.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldLight
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeliveryDining,
                                    contentDescription = "Delivery radius",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "५ कि.मी. सम्म डिलिभरी" else "Delivery within 5 km",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "9819329559",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Order Tracker Card (if any)
        if (activeOngoingOrder != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onTrackOrderClick(activeOngoingOrder) }
                        .testTag("active_order_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Moped,
                                    contentDescription = "Active Delivery",
                                    tint = GoldAccent
                                )
                            }
                            Column {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "सक्रिय अर्डर: ${activeOngoingOrder.orderNumber}" else "Active Order: ${activeOngoingOrder.orderNumber}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "लाइभ ट्र्याकिङ हेर्नुहोस्" else "Tap for live GPS tracking",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        StatusBadge(status = activeOngoingOrder.orderStatus, language = language)
                    }
                }
            }
        }

        // Promotional Coupon Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = "Promo",
                            tint = RedBrand,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "कोड प्रयोग गर्नुहोस्: MEGHA50" else "Use Coupon: MEGHA50",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                            Text(
                                text = if (language == AppLanguage.NEPALI) "रु. ४०० भन्दा माथि रु. ५० छुट" else "Get Rs. 50 OFF on orders above Rs. 400",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_search_bar"),
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "खाना खोज्नुहोस् (उदा. मोमो, चाउमिन)..." else "Search dishes (e.g. Momo, Wings, Chowmein)...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = EmeraldPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Categories section
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "परिकारका वर्गहरू" else "Categories",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (language == AppLanguage.NEPALI) "सबै हेर्नुहोस्" else "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        ),
                        modifier = Modifier.clickable { onViewAllMenuClick() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Pill
                    FilterChip(
                        selected = true,
                        onClick = { onViewAllMenuClick() },
                        label = { Text(if (language == AppLanguage.NEPALI) "सबै" else "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    categories.forEach { cat ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategoryClick(cat.id) },
                            label = { Text(if (language == AppLanguage.NEPALI) cat.nameNp else cat.nameEn) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = BorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Popular Items
        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Text(
                    text = if (language == AppLanguage.NEPALI) "धेरै रुचाइएका परिकारहरू" else "Popular Dishes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        items(popularItems) { item ->
            FoodItemCard(
                item = item,
                language = language,
                onItemClick = { onItemClick(item) },
                onAddToCart = { onAddToCart(item) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }

        // Featured Items Section Header
        if (featuredItems.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "विशेष परिकारहरू" else "Chef's Recommendations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            items(featuredItems.take(4)) { item ->
                FoodItemCard(
                    item = item,
                    language = language,
                    onItemClick = { onItemClick(item) },
                    onAddToCart = { onAddToCart(item) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }

        // BAM Studio Footer credit
        item {
            BAMStudioFooter(language = language)
        }
    }
}
