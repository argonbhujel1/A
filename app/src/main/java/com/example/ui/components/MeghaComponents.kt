package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeghaTopAppBar(
    title: String = "Megha Hotel",
    subtitle: String = "Urlabari, Morang",
    language: AppLanguage,
    cartCount: Int,
    currentUser: UserProfile?,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onLanguageToggle: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.shadow(2.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Logo",
                        tint = GoldAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                        if (currentUser != null && currentUser.role != UserRole.GUEST && currentUser.role != UserRole.CUSTOMER) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentUser.role.label,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        actions = {
            // Language Toggle
            OutlinedButton(
                onClick = onLanguageToggle,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(34.dp)
                    .testTag("language_toggle_button"),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.ENGLISH) "नेपाली" else "EN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 12.sp
                    )
                )
            }

            // Dark / Light Theme Toggle
            IconButton(
                onClick = onToggleDarkTheme,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                    tint = if (isDarkTheme) GoldAccent else EmeraldPrimary
                )
            }

            // Cart icon with badge
            IconButton(
                onClick = onCartClick,
                modifier = Modifier.testTag("cart_icon_button")
            ) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(
                                containerColor = RedBrand,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Shopping Cart",
                        tint = EmeraldPrimary
                    )
                }
            }

            // Profile / Role icon
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("profile_icon_button")
            ) {
                Icon(
                    imageVector = if (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.KITCHEN || currentUser?.role == UserRole.RIDER) {
                        Icons.Default.AdminPanelSettings
                    } else {
                        Icons.Default.AccountCircle
                    },
                    contentDescription = "User Profile",
                    tint = if (currentUser?.role == UserRole.ADMIN) GoldDark else EmeraldPrimary
                )
            }
        }
    )
}

@Composable
fun MeghaBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    cartItemCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .shadow(8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        val navItems = listOf(
            NavigationItem("home", "Home", "होम", Icons.Default.Home, Icons.Outlined.Home),
            NavigationItem("menu", "Menu", "मेनु", Icons.Default.RestaurantMenu, Icons.Outlined.RestaurantMenu),
            NavigationItem("cart", "Cart", "कार्ट", Icons.Default.ShoppingCart, Icons.Outlined.ShoppingCart, cartItemCount),
            NavigationItem("orders", "Orders", "अर्डरहरू", Icons.Default.ReceiptLong, Icons.Outlined.ReceiptLong),
            NavigationItem("profile", "Profile", "प्रोफाइल", Icons.Default.Person, Icons.Outlined.Person)
        )

        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = RedBrand,
                                    contentColor = Color.White
                                ) {
                                    Text(item.badgeCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.labelEn
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.labelEn
                        )
                    }
                },
                label = {
                    Text(
                        text = item.labelEn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldPrimary,
                    selectedTextColor = EmeraldPrimary,
                    indicatorColor = GoldContainer,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

private data class NavigationItem(
    val route: String,
    val labelEn: String,
    val labelNp: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun FoodItemCard(
    item: FoodItem,
    language: AppLanguage,
    onItemClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onItemClick)
            .testTag("food_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image with Coil
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CreamSurfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.localizedName(language),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Popular / Featured ribbon
                if (item.isPopular) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(
                                color = GoldAccent,
                                shape = RoundedCornerShape(bottomEnd = 8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.NEPALI) "लोकप्रिय" else "Popular",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        )
                    }
                }

                // Veg / Non-veg dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color.White, shape = RoundedCornerShape(3.dp))
                        .border(
                            1.dp,
                            if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828),
                            shape = RoundedCornerShape(3.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (item.isVeg) Color(0xFF2E7D32) else Color(0xFFC62828))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = item.localizedName(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.localizedDescription(language).isNotBlank()) {
                    Text(
                        text = item.localizedDescription(language),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Rs. ${item.effectivePrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = RedBrand
                        )
                    )
                    if (item.discountPriceNpr != null && item.discountPriceNpr < item.priceNpr) {
                        Text(
                            text = "Rs. ${item.priceNpr.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                }
            }

            // Add Button
            if (item.isAvailable) {
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_button_${item.id}")
                ) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "थप्नुहोस् +" else "Add +",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            } else {
                Surface(
                    color = CreamSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "सकियो" else "Sold Out",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: OrderStatus,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> StatusPending.copy(alpha = 0.15f) to StatusPending
        OrderStatus.CONFIRMED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.PREPARING -> StatusPreparing.copy(alpha = 0.15f) to StatusPreparing
        OrderStatus.READY -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        OrderStatus.ASSIGNED -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
        OrderStatus.ON_THE_WAY -> StatusOnTheWay.copy(alpha = 0.15f) to StatusOnTheWay
        OrderStatus.DELIVERED -> StatusDelivered.copy(alpha = 0.15f) to StatusDelivered
        OrderStatus.CANCELLED -> StatusCancelled.copy(alpha = 0.15f) to StatusCancelled
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = if (language == AppLanguage.NEPALI) status.titleNp else status.titleEn,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun LiveRadarMap(
    order: Order,
    restaurantLat: Double = 26.664175,
    restaurantLng: Double = 87.637234,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A20))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live map canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw map grid lines
                val gridColor = Color(0x15FFFFFF)
                val step = 40.dp.toPx()
                var x = 0f
                while (x < width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y < height) {
                    drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                    y += step
                }

                // Megha Hotel position (Left side of map)
                val hotelPos = Offset(width * 0.22f, height * 0.65f)

                // Customer delivery destination (Right side of map)
                val destPos = Offset(width * 0.82f, height * 0.32f)

                // Rider position (Interpolated based on status or active coordinates)
                val progress = when (order.orderStatus) {
                    OrderStatus.DELIVERED -> 1.0f
                    OrderStatus.ON_THE_WAY -> 0.62f
                    OrderStatus.ASSIGNED -> 0.15f
                    else -> 0.0f
                }

                val riderPos = Offset(
                    hotelPos.x + (destPos.x - hotelPos.x) * progress,
                    hotelPos.y + (destPos.y - hotelPos.y) * progress - (sin(progress * Math.PI) * 25f).toFloat()
                )

                // Draw route line
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawLine(
                    color = Color(0x40D4AF37),
                    start = hotelPos,
                    end = destPos,
                    strokeWidth = 3f,
                    pathEffect = pathEffect
                )

                // Draw completed route
                drawLine(
                    color = Color(0xFFD4AF37),
                    start = hotelPos,
                    end = riderPos,
                    strokeWidth = 4f
                )

                // Draw Hotel pin
                drawCircle(color = Color(0xFF1B4332), radius = 16f, center = hotelPos)
                drawCircle(color = Color(0xFFD4AF37), radius = 10f, center = hotelPos)

                // Draw Destination pin
                drawCircle(color = Color(0x33C42517), radius = 22f, center = destPos)
                drawCircle(color = Color(0xFFC42517), radius = 10f, center = destPos)

                // Draw Rider Pulse & Icon
                if (order.orderStatus == OrderStatus.ON_THE_WAY || order.orderStatus == OrderStatus.ASSIGNED) {
                    drawCircle(color = Color(0x44D4AF37), radius = pulseRadius, center = riderPos)
                    drawCircle(color = Color(0xFFFFFFFF), radius = 14f, center = riderPos)
                    drawCircle(color = Color(0xFF1B4332), radius = 10f, center = riderPos)
                }
            }

            // Overlay labels
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xCC0B1F17),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44D4AF37))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Text(
                            text = "LIVE GPS TRACKING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xCC0B1F17),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Urlabari Center",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Bottom Map info bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color(0xEE0B1F17)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Restaurant: Megha Hotel, Urlabari",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                        )
                        Text(
                            text = "Destination: ${order.locality.ifBlank { "Urlabari" }}",
                            style = MaterialTheme.typography.bodySmall.copy(color = GoldLight, fontSize = 11.sp)
                        )
                    }
                    if (order.assignedRiderName != null) {
                        Surface(
                            color = EmeraldPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Rider: ${order.assignedRiderName}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BAMStudioFooter(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val url = "https://bamstudio.argan.com.np/"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Engineered by BAM Studio",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Technology by BAM Studio",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                textDecoration = TextDecoration.Underline,
                fontSize = 11.sp
            ),
            modifier = Modifier
                .clickable {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // ignore if browser not available
                    }
                }
                .testTag("bam_studio_footer_link")
        )
    }
}

@Composable
fun SocialLoginButton(
    providerName: String,
    icon: ImageVector,
    brandColor: Color,
    onUnavailableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onUnavailableClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("social_btn_${providerName.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = CreamSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = providerName,
                    tint = brandColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with $providerName",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }
            Surface(
                color = BorderSubtle,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Coming Soon",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
