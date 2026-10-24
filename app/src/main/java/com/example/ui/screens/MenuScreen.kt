package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.FoodCategory
import com.example.model.FoodItem
import com.example.ui.components.BAMStudioFooter
import com.example.ui.components.FoodItemCard
import com.example.ui.theme.*

@Composable
fun MenuScreen(
    items: List<FoodItem>,
    categories: List<FoodCategory>,
    selectedCategory: String?,
    searchQuery: String,
    vegOnly: Boolean,
    language: AppLanguage,
    onCategorySelect: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onVegOnlyToggle: (Boolean) -> Unit,
    onItemClick: (FoodItem) -> Unit,
    onAddToCart: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_screen")
    ) {
        // Search & Filter header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_search_input"),
                    placeholder = {
                        Text(
                            text = if (language == AppLanguage.NEPALI) "मेनुमा खोज्नुहोस्..." else "Search Megha Hotel menu...",
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
                        focusedContainerColor = CreamBackground,
                        unfocusedContainerColor = CreamBackground
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategorySelect(null) },
                        label = { Text(if (language == AppLanguage.NEPALI) "सबै परिकार" else "All Dishes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )

                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = { onCategorySelect(cat.id) },
                            label = { Text(if (language == AppLanguage.NEPALI) cat.nameNp else cat.nameEn) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == cat.id,
                                borderColor = if (selectedCategory == cat.id) EmeraldPrimary else BorderSubtle
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Veg only switch & item count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "${items.size} परिकार उपलब्ध" else "${items.size} items available",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.NEPALI) "शाकाहारी मात्र" else "Veg Only",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (vegOnly) Color(0xFF2E7D32) else TextSecondary
                            )
                        )
                        Switch(
                            checked = vegOnly,
                            onCheckedChange = onVegOnlyToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D32),
                                uncheckedTrackColor = BorderSubtle
                            ),
                            modifier = Modifier.testTag("veg_only_switch")
                        )
                    }
                }
            }
        }

        // Dish items list
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (language == AppLanguage.NEPALI) "कुनै परिकार भेटिएन" else "No dishes found",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.NEPALI) "फरक नाम वा वर्ग छानेर प्रयास गर्नुहोस्।" else "Try adjusting your search or category filter.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    FoodItemCard(
                        item = item,
                        language = language,
                        onItemClick = { onItemClick(item) },
                        onAddToCart = { onAddToCart(item) }
                    )
                }

                item {
                    BAMStudioFooter(language = language)
                }
            }
        }
    }
}
