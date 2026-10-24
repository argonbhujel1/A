package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.PromotionCoupon
import com.example.ui.theme.*

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    subtotal: Double,
    discount: Double,
    deliveryFee: Double,
    finalTotal: Double,
    appliedCoupon: PromotionCoupon?,
    language: AppLanguage,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onProceedToCheckout: () -> Unit,
    onBrowseMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var couponInput by remember { mutableStateOf("") }

    if (cartItems.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = "Empty Cart",
                    tint = TextMuted,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (language == AppLanguage.NEPALI) "तपाईंको कार्ट खाली छ" else "Your cart is empty",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (language == AppLanguage.NEPALI) "मेनुबाट आफ्नो मनपर्ने स्वादिष्ट परिकारहरू थप्नुहोस्।" else "Explore the Megha Hotel menu and add your favorite dishes.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBrowseMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("browse_menu_button")
                ) {
                    Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.NEPALI) "मेनु हेर्नुहोस्" else "Browse Menu",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "अर्डर गरिएका परिकारहरू (${cartItems.sumOf { it.quantity }})" else "Your Items (${cartItems.sumOf { it.quantity }})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                items(cartItems) { item ->
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CreamSurfaceVariant)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.foodItem.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.foodItem.localizedName(language),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.foodItem.localizedName(language),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Rs. ${item.foodItem.effectivePrice.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = RedBrand,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (item.specialInstructions.isNotBlank()) {
                                    Text(
                                        text = "Note: ${item.specialInstructions}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item.foodItem.id, item.quantity - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = if (item.quantity == 1) RedBrand else EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = { onUpdateQuantity(item.foodItem.id, item.quantity + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Coupon Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "छुट कुपन कोड" else "Have a Coupon?",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${appliedCoupon.code} Applied ✓",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                            )
                                            Text(
                                                text = if (language == AppLanguage.NEPALI) appliedCoupon.titleNp else appliedCoupon.titleEn,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF388E3C),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        TextButton(onClick = onRemoveCoupon) {
                                            Text(
                                                text = if (language == AppLanguage.NEPALI) "हटाउनुहोस्" else "Remove",
                                                color = RedBrand,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it },
                                        placeholder = { Text("MEGHA50") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("coupon_input_field"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EmeraldPrimary,
                                            unfocusedBorderColor = BorderSubtle
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (couponInput.isNotBlank()) {
                                                onApplyCoupon(couponInput)
                                                couponInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("apply_coupon_button")
                                    ) {
                                        Text(if (language == AppLanguage.NEPALI) "लागू गर्नुहोस्" else "Apply")
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "बिल विवरण" else "Bill Details",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "जम्मा रकम (सबटोटल)" else "Subtotal",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "Rs. ${subtotal.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            if (discount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "कुपन छुट" else "Coupon Discount",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF2E7D32))
                                    )
                                    Text(
                                        text = "- Rs. ${discount.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "डिलिभरी शुल्क" else "Delivery Fee",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = if (deliveryFee == 0.0) "FREE" else "Rs. ${deliveryFee.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (deliveryFee == 0.0) Color(0xFF2E7D32) else TextPrimary
                                    )
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "कुल तिर्नुपर्ने रकम" else "Total Payable",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldPrimary
                                    )
                                )
                                Text(
                                    text = "Rs. ${finalTotal.toInt()}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RedBrand
                                    )
                                )
                            }
                        }
                    }
                }
                // Minimum Order Rs. 500 Notice
                if (subtotal < 500.0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RedLight),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RedBrand.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = RedBrand,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "न्यूनतम अर्डर रकम: रु. ५००" else "Minimum Order: Rs. 500",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RedBrand
                                        )
                                    )
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "अर्डर अगाडि बढाउन थप रु. ${(500.0 - subtotal).toInt()} को परिकार थप्नुहोस्।" else "Please add items worth Rs. ${(500.0 - subtotal).toInt()} more to proceed.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom checkout action bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (subtotal < 500.0) {
                        Text(
                            text = if (language == AppLanguage.NEPALI) "न्यूनतम अर्डर रु. ५०० पुगेपछि मात्र अर्डर स्वीकार गरिन्छ" else "Orders accepted only above Rs. 500 minimum",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RedBrand,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "तिर्नुपर्ने रकम:" else "Total Amount:",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                            )
                            Text(
                                text = "Rs. ${finalTotal.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RedBrand
                                )
                            )
                        }

                        Button(
                            onClick = onProceedToCheckout,
                            enabled = subtotal >= 500.0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                disabledContainerColor = BorderSubtle
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("proceed_checkout_button")
                        ) {
                            Text(
                                text = if (subtotal >= 500.0) {
                                    if (language == AppLanguage.NEPALI) "चेकआउट गर्नुहोस् →" else "Proceed to Checkout →"
                                } else {
                                    if (language == AppLanguage.NEPALI) "न्यूनतम रु. ५०० आवश्यक" else "Min Rs. 500 Required"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (subtotal >= 500.0) Color.White else TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
