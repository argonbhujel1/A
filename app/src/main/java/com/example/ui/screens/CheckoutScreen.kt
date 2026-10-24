package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppLanguage
import com.example.model.PaymentMethod
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices

@Composable
fun CheckoutScreen(
    currentUser: UserProfile?,
    subtotal: Double,
    discount: Double,
    deliveryFee: Double,
    finalTotal: Double,
    customerLat: Double,
    customerLng: Double,
    language: AppLanguage,
    onLocationUpdate: (Double, Double) -> Unit,
    onPlaceOrder: (
        name: String,
        phone: String,
        email: String,
        address: String,
        locality: String,
        landmark: String,
        notes: String,
        special: String,
        orderType: String,
        paymentMethod: PaymentMethod,
        paymentProofRef: String?
    ) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(currentUser?.fullName?.takeIf { it != "Guest Customer" } ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone?.takeIf { !it.contains("X") } ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var address by remember { mutableStateOf("Urlabari Chowk, Morang") }
    var locality by remember { mutableStateOf("Urlabari-7") }
    var landmark by remember { mutableStateOf("") }
    var deliveryNotes by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }

    var orderType by remember { mutableStateOf("delivery") } // "delivery" or "takeaway"
    var paymentMethod by remember { mutableStateOf(PaymentMethod.COD) }
    var qrTransactionId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // GPS location fetcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        onLocationUpdate(loc.latitude, loc.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // ignore
            }
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
                        text = if (language == AppLanguage.NEPALI) "डिलिभरी तथा चेकआउट" else "Checkout & Delivery",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RedBrand,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.NEPALI) "कुल तिर्नुपर्ने रकम:" else "Total Amount:",
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
                            onClick = {
                                // Validation
                                if (name.trim().isBlank()) {
                                    errorMessage = if (language == AppLanguage.NEPALI) "कृपया आफ्नो पूरा नाम लेख्नुहोस्।" else "Please enter your full name."
                                    return@Button
                                }
                                val cleanPhone = phone.trim().replace(" ", "").replace("-", "")
                                if (cleanPhone.length < 10) {
                                    errorMessage = if (language == AppLanguage.NEPALI) "कृपया १० अंकको मान्य नेपाली फोन नम्बर (उदा. ९८XXXXXXXX) राख्नुहोस्।" else "Please enter a valid 10-digit Nepali phone number (e.g. 98XXXXXXXX)."
                                    return@Button
                                }
                                if (orderType == "delivery" && address.trim().isBlank()) {
                                    errorMessage = if (language == AppLanguage.NEPALI) "कृपया डिलिभरी ठेगाना लेख्नुहोस्।" else "Please enter delivery address."
                                    return@Button
                                }
                                if (paymentMethod == PaymentMethod.QR && qrTransactionId.trim().isBlank()) {
                                    errorMessage = if (language == AppLanguage.NEPALI) "कृपया क्युआर भुक्तानी पश्चात ट्रान्जेक्सन आइडी (Txn ID) उल्लेख गर्नुहोस्।" else "Please provide QR Transaction Reference ID."
                                    return@Button
                                }

                                errorMessage = null
                                isSubmitting = true
                                onPlaceOrder(
                                    name.trim(),
                                    cleanPhone,
                                    email.trim(),
                                    address.trim(),
                                    locality.trim(),
                                    landmark.trim(),
                                    deliveryNotes.trim(),
                                    specialInstructions.trim(),
                                    orderType,
                                    paymentMethod,
                                    if (paymentMethod == PaymentMethod.QR) qrTransactionId.trim() else null
                                )
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("confirm_order_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (language == AppLanguage.NEPALI) "अर्डर निश्चित गर्नुहोस् ✓" else "Confirm Order ✓",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Type Selector (Delivery vs Takeaway)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.NEPALI) "अर्डरको प्रकार छान्नुहोस्" else "Select Order Type",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { orderType = "delivery" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (orderType == "delivery") EmeraldPrimary else CreamSurfaceVariant,
                                border = if (orderType == "delivery") null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = if (orderType == "delivery") Color.White else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "घरमा डिलिभरी" else "Home Delivery",
                                        fontWeight = FontWeight.Bold,
                                        color = if (orderType == "delivery") Color.White else TextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { orderType = "takeaway" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (orderType == "takeaway") EmeraldPrimary else CreamSurfaceVariant,
                                border = if (orderType == "takeaway") null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = if (orderType == "takeaway") Color.White else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "होटलबाट टेकअवे" else "Takeaway",
                                        fontWeight = FontWeight.Bold,
                                        color = if (orderType == "takeaway") Color.White else TextPrimary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customer Contact Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "ग्राहक सम्पर्क विवरण" else "Contact Information",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (language == AppLanguage.NEPALI) "पूरा नाम *" else "Full Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_name_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (language == AppLanguage.NEPALI) "सम्पर्क फोन नम्बर * (उदा. ९८XXXXXXXX)" else "Contact Phone * (e.g. 98XXXXXXXX)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (language == AppLanguage.NEPALI) "इमेल (ऐच्छिक / बिलका लागि)" else "Email (Optional, for receipt)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Delivery Details Card (if orderType == "delivery")
            if (orderType == "delivery") {
                item {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RedBrand)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "डिलिभरी ठेगाना" else "Delivery Address",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                // Use GPS Button
                                TextButton(
                                    onClick = {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            try {
                                                val fused = LocationServices.getFusedLocationProviderClient(context)
                                                fused.lastLocation.addOnSuccessListener { loc ->
                                                    if (loc != null) onLocationUpdate(loc.latitude, loc.longitude)
                                                }
                                            } catch (e: SecurityException) {}
                                        } else {
                                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "मेरो GPS लिनुहोस्" else "Use GPS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Delivery Area Selection Chips (Urlabari & Damak)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "डेलिभरी क्षेत्र छान्नुहोस् (उर्लाबारी वा दमक):" else "Select Delivery Zone (Urlabari or Damak):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isUrlabari = locality.contains("Urlabari", ignoreCase = true) || locality.contains("उर्लाबारी") || (customerLat in 26.660..26.675 && customerLng in 87.630..87.645)
                                val isDamak = locality.contains("Damak", ignoreCase = true) || locality.contains("दमक") || (customerLat in 26.650..26.680 && customerLng in 87.680..87.715)

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            locality = "Urlabari"
                                            if (address.isBlank() || address.contains("Damak")) {
                                                address = "Urlabari Chowk, Morang"
                                            }
                                            onLocationUpdate(26.665420913346072, 87.63826396825684)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isUrlabari) EmeraldContainer else CreamSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isUrlabari) EmeraldPrimary else BorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📍 उर्लाबारी (Urlabari)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isUrlabari) EmeraldDark else TextPrimary
                                        )
                                        Text(
                                            text = "२ कि.मी भित्र नि:शुल्क (FREE)",
                                            fontSize = 10.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            locality = "Damak"
                                            if (address.isBlank() || address.contains("Urlabari")) {
                                                address = "Damak Chowk, Jhapa"
                                            }
                                            onLocationUpdate(26.6631, 87.6975)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDamak) EmeraldContainer else CreamSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isDamak) EmeraldPrimary else BorderSubtle)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📍 दमक (Damak)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isDamak) EmeraldDark else TextPrimary
                                        )
                                        Text(
                                            text = "फिक्स्ड रु. १००",
                                            fontSize = 10.sp,
                                            color = RedBrand,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Dynamic Delivery Status Notice
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = if (deliveryFee == 0.0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (deliveryFee == 0.0) Icons.Default.CheckCircle else Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = if (deliveryFee == 0.0) Color(0xFF2E7D32) else GoldDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (deliveryFee == 0.0)
                                                "🎉 २ कि.मी भित्र नि:शुल्क डेलिभरी (डेलिभरी: रु. ०)"
                                            else
                                                "🛵 फिक्स्ड डेलिभरी शुल्क: रु. १००",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (deliveryFee == 0.0) Color(0xFF1B5E20) else TextPrimary
                                        )
                                        Text(
                                            text = "केन्द्र: Megha Hotel (26.66542, 87.63826) • सेवा क्षेत्र: उर्लाबारी तथा दमक",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text(if (language == AppLanguage.NEPALI) "डिलिभरी घर/स्थान ठेगाना *" else "Complete Street Address *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_address_field"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = locality,
                                    onValueChange = { locality = it },
                                    label = { Text(if (language == AppLanguage.NEPALI) "टोल / क्षेत्र (Urlabari/Damak)" else "Area / Ward") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = landmark,
                                    onValueChange = { landmark = it },
                                    label = { Text(if (language == AppLanguage.NEPALI) "नजिकको चिनो" else "Landmark") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = deliveryNotes,
                                onValueChange = { deliveryNotes = it },
                                label = { Text(if (language == AppLanguage.NEPALI) "डिलिभरी निर्देशन (उदा. गेटमा घन्टी बजाउनुहोस्)" else "Delivery Notes (e.g. Ring bell at gate)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "GPS: ${"%.5f".format(customerLat)}, ${"%.5f".format(customerLng)} (${if (deliveryFee == 0.0) "Within 2km Free Zone" else "Fixed Rs. 100 Zone"})",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // Payment Method Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = GoldDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.NEPALI) "भुक्तानी विधि छान्नुहोस्" else "Payment Method",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // COD Option
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = PaymentMethod.COD },
                            shape = RoundedCornerShape(10.dp),
                            color = if (paymentMethod == PaymentMethod.COD) EmeraldContainer.copy(alpha = 0.5f) else CreamSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (paymentMethod == PaymentMethod.COD) EmeraldPrimary else BorderSubtle
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = paymentMethod == PaymentMethod.COD,
                                    onClick = { paymentMethod = PaymentMethod.COD },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "नगद भुक्तानी (Cash on Delivery)" else "Cash on Delivery (COD)",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "खाना प्राप्त भएपछि राइडरलाई नगद दिनुहोस्" else "Pay in cash when food arrives at your door",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // QR Option
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { paymentMethod = PaymentMethod.QR },
                            shape = RoundedCornerShape(10.dp),
                            color = if (paymentMethod == PaymentMethod.QR) GoldContainer.copy(alpha = 0.5f) else CreamSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (paymentMethod == PaymentMethod.QR) GoldAccent else BorderSubtle
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = paymentMethod == PaymentMethod.QR,
                                    onClick = { paymentMethod = PaymentMethod.QR },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldDark)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "क्युआर भुक्तानी (Fonepay / eSewa QR)" else "QR Payment (Fonepay / eSewa)",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (language == AppLanguage.NEPALI) "मोबाइल बैंकिङ वा eSewa बाट स्क्यान गरी तिर्नुहोस्" else "Scan & Pay via Mobile Banking or eSewa",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )
                                }
                            }
                        }

                        // If QR Selected: Show QR Code visual & Txn ID entry
                        if (paymentMethod == PaymentMethod.QR) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Megha Hotel - Fonepay QR",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // QR Graphic Placeholder
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(Color.White)
                                        .border(2.dp, EmeraldPrimary, RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "Fonepay QR Code",
                                        modifier = Modifier.fillMaxSize(),
                                        tint = EmeraldPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Payable: Rs. ${finalTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RedBrand
                                    )
                                )
                                Text(
                                    text = "Account: Megha Hotel Restaurant (Urlabari)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = qrTransactionId,
                                    onValueChange = { qrTransactionId = it },
                                    label = { Text(if (language == AppLanguage.NEPALI) "ट्रान्जेक्सन आइडी (Txn Reference) *" else "Transaction Reference ID *") },
                                    placeholder = { Text("e.g. FP20261024-9982") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("qr_txn_id_field"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
