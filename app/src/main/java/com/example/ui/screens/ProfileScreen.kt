package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.SavedAddress
import com.example.model.UserProfile
import com.example.model.UserRole
import com.example.ui.components.BAMStudioFooter
import com.example.ui.components.SocialLoginButton
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    currentUser: UserProfile?,
    language: AppLanguage,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onToggleLanguage: () -> Unit,
    onLoginClick: (String, String) -> Unit,
    onLogoutClick: () -> Unit,
    onOpenAuthLogin: () -> Unit = {},
    onUpdateProfile: (String, String, String) -> Unit,
    onAddAddress: (SavedAddress) -> Unit,
    onOpenStaffPortal: () -> Unit,
    onSocialUnavailableClick: () -> Unit,
    onTestNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser?.fullName ?: "Guest Customer",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (currentUser?.role == UserRole.GUEST) {
                                        if (language == AppLanguage.NEPALI) "अतिथि प्रयोगकर्ता (लगइन विना)" else "Guest User"
                                    } else {
                                        currentUser?.email ?: ""
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )
                                Surface(
                                    color = if (currentUser?.role == UserRole.ADMIN) GoldContainer else EmeraldContainer,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = currentUser?.role?.label ?: "Guest",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUser?.role == UserRole.ADMIN) EmeraldDark else EmeraldPrimary
                                        )
                                    )
                                }
                            }
                        }

                        if (currentUser?.role != UserRole.GUEST) {
                            IconButton(onClick = { showEditProfileDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Staff & Role Dashboard Shortcut (If logged in as Admin, Kitchen, or Rider)
        if (currentUser?.role != null && currentUser.role != UserRole.GUEST && currentUser.role != UserRole.CUSTOMER) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStaffPortal() },
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = EmeraldDark
                                )
                            }
                            Column {
                                Text(
                                    text = "${currentUser.role.label} Dashboard",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "व्यवस्थापन प्यानल खोल्नुहोस्" else "Open Management Console",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GoldLight, fontSize = 11.sp)
                                )
                            }
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        // Social Sign-in section (Displaying disabled buttons with coming soon & exact informational prompt)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "सामाजिक सञ्जाल लगइन" else "Social Sign In",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.NEPALI) "नयाँ खाता वा सामाजिक खाताबाट साइन इन गर्नुहोस्।" else "Link an account or continue as Guest.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Button
                    SocialLoginButton(
                        providerName = "Google",
                        icon = Icons.Default.Mail,
                        brandColor = Color(0xFFDB4437),
                        onUnavailableClick = onSocialUnavailableClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Facebook Button
                    SocialLoginButton(
                        providerName = "Facebook",
                        icon = Icons.Default.Share,
                        brandColor = Color(0xFF1877F2),
                        onUnavailableClick = onSocialUnavailableClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // X Button
                    SocialLoginButton(
                        providerName = "X (Twitter)",
                        icon = Icons.Default.Tag,
                        brandColor = Color(0xFF000000),
                        onUnavailableClick = onSocialUnavailableClick
                    )
                }
            }
        }

        // App Preferences (Language & Settings)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.NEPALI) "एप भाषा तथा सेटिङ" else "Preferences & Language",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleLanguage() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "भाषा (Language)" else "App Language",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = language.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSubtle)

                    // Dark / Light Theme Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDarkTheme) GoldAccent else EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "डार्क मोड (Dark Theme)" else "Dark Theme",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("profile_dark_mode_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSubtle)

                    // Notification Alerts & Test
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "अर्डर सूचना (Notifications)" else "Order Notifications",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "प्रत्यक्ष सूचना प्रणाली सक्रिय छ ✓" else "Real-time alerts active ✓",
                                    style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontSize = 11.sp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onTestNotification,
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "सूचना जाँच" else "Test Alert",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSubtle)

                    // Open Main Login / Role Switcher Page
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAuthLogin() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "लगइन तथा भूमिका परिवर्तन (Login Page)" else "Sign In / Switch Role (Login Page)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }

                    // Logout option
                    if (currentUser?.role != UserRole.GUEST) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLogoutClick() }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = RedBrand)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.NEPALI) "लगआउट (अतिथि बन्नुहोस्)" else "Sign Out (Switch to Guest)",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = RedBrand, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Restaurant & BAM Studio Credits
        item {
            BAMStudioFooter(language = language)
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(currentUser?.fullName ?: "") }
        var editPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
        var editEmail by remember { mutableStateOf(currentUser?.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.NEPALI) "प्रोफाइल सम्पादन" else "Edit Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editPhone, editEmail)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


