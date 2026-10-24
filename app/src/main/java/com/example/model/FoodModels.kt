package com.example.model

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    NEPALI("ne", "नेपाली")
}

enum class UserRole(val label: String, val roleName: String) {
    GUEST("Guest", "अतिथि"),
    CUSTOMER("Customer", "ग्राहक"),
    ADMIN("Admin", "प्रशासक"),
    KITCHEN("Kitchen Staff", "भान्छा"),
    RIDER("Delivery Rider", "डिलिभरी राइडर")
}

enum class OrderStatus(val titleEn: String, val titleNp: String) {
    PENDING("Order Placed", "अर्डर प्राप्त भयो"),
    CONFIRMED("Confirmed", "पुष्टि भयो"),
    PREPARING("Preparing Food", "खाना बन्दैछ"),
    READY("Ready for Pickup", "तयार भयो"),
    ASSIGNED("Rider Assigned", "राइडर तोकियो"),
    ON_THE_WAY("On the Way", "बाटोमा छ"),
    DELIVERED("Delivered", "डिलिभर भयो"),
    CANCELLED("Cancelled", "रद्द भयो")
}

enum class PaymentMethod(val labelEn: String, val labelNp: String) {
    COD("Cash on Delivery (COD)", "नगद भुक्तानी (COD)"),
    QR("QR Payment (Fonepay / eSewa)", "क्यूआर भुक्तानी (Fonepay / eSewa)")
}

enum class PaymentStatus(val labelEn: String, val labelNp: String) {
    PENDING_VERIFICATION("Pending Verification", "प्रमाणीकरण बाँकी"),
    VERIFIED("Payment Verified", "भुक्तानी प्रमाणित"),
    PAID("Paid", "सम्पन्न")
}

data class FoodCategory(
    val id: String,
    val nameEn: String,
    val nameNp: String,
    val sortOrder: Int = 0
)

data class FoodItem(
    val id: String,
    val nameEn: String,
    val nameNp: String,
    val descriptionEn: String,
    val descriptionNp: String,
    val categoryId: String,
    val priceNpr: Double,
    val discountPriceNpr: Double? = null,
    val imageUrl: String,
    val isVeg: Boolean = false,
    val isPopular: Boolean = false,
    val isFeatured: Boolean = false,
    val isAvailable: Boolean = true,
    val preparationTimeMinutes: Int = 15,
    val stockQuantity: Int = 50,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val effectivePrice: Double
        get() = discountPriceNpr ?: priceNpr

    fun localizedName(language: AppLanguage): String =
        if (language == AppLanguage.NEPALI && nameNp.isNotBlank()) nameNp else nameEn

    fun localizedDescription(language: AppLanguage): String =
        if (language == AppLanguage.NEPALI && descriptionNp.isNotBlank()) descriptionNp else descriptionEn
}

data class CartItem(
    val foodItem: FoodItem,
    val quantity: Int = 1,
    val specialInstructions: String = ""
) {
    val totalAmount: Double
        get() = foodItem.effectivePrice * quantity
}

data class OrderItemSnapshot(
    val foodItemId: String,
    val nameEn: String,
    val nameNp: String,
    val imageUrl: String,
    val unitPriceNpr: Double,
    val quantity: Int,
    val specialInstructions: String = ""
) {
    val totalNpr: Double
        get() = unitPriceNpr * quantity
}

data class Order(
    val orderId: String,
    val orderNumber: String,
    val trackingToken: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String = "",
    val deliveryAddress: String,
    val locality: String = "Urlabari",
    val landmark: String = "",
    val deliveryNotes: String = "",
    val specialInstructions: String = "",
    val orderType: String = "delivery", // "delivery" or "takeaway"
    val items: List<OrderItemSnapshot>,
    val subtotalNpr: Double,
    val discountNpr: Double = 0.0,
    val deliveryFeeNpr: Double = 0.0,
    val totalPayableNpr: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING_VERIFICATION,
    val paymentProofReference: String? = null,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val customerLat: Double? = null,
    val customerLng: Double? = null,
    val assignedRiderId: String? = null,
    val assignedRiderName: String? = null,
    val assignedRiderPhone: String? = null,
    val riderLat: Double? = null,
    val riderLng: Double? = null,
    val lastRiderUpdate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PromotionCoupon(
    val code: String,
    val titleEn: String,
    val titleNp: String,
    val discountPercentage: Double = 0.0,
    val flatDiscountNpr: Double = 0.0,
    val minOrderAmountNpr: Double = 300.0,
    val isActive: Boolean = true
)

data class SavedAddress(
    val id: String,
    val title: String,
    val fullAddress: String,
    val locality: String = "Urlabari",
    val landmark: String = "",
    val lat: Double = 26.664175,
    val lng: Double = 87.637234,
    val isDefault: Boolean = false
)

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val role: UserRole = UserRole.CUSTOMER,
    val profileImageUrl: String = "",
    val savedAddresses: List<SavedAddress> = emptyList()
)

data class SplashConfig(
    val imageUrl: String = "https://res.cloudinary.com/nfolm8hx/image/upload/v1792239079/megha-hotel/settings/oclrqkivkbfgfjt6awfi.png",
    val title: String = "Megha Hotel",
    val subtitle: String = "Good Food. Great Moments.",
    val durationSeconds: Int = 5,
    val backgroundColorHex: String = "#1B4332",
    val creditText: String = "Engineered by BAM Studio"
)

data class DeliverySettings(
    val restaurantName: String = "Megha Hotel",
    val restaurantLocation: String = "Urlabari, Morang, Nepal",
    val restaurantPhone: String = "9819329559",
    val restaurantWhatsApp: String = "9779812387437",
    val restaurantLat: Double = 26.665420913346072,
    val restaurantLng: Double = 87.63826396825684,
    val freeDeliveryRadiusKm: Double = 2.0,
    val fixedDeliveryChargeNpr: Double = 100.0,
    val baseDeliveryChargeNpr: Double = 100.0,
    val minOrderAmountNpr: Double = 500.0,
    val freeDeliveryOverNpr: Double = 1000.0,
    val extraKmChargeNpr: Double = 0.0,
    val baseRadiusKm: Double = 2.0,
    val maxRadiusKm: Double = 15.0,
    val deliveryAreas: List<String> = listOf("Urlabari", "Damak"),
    val isRestaurantOpen: Boolean = true
)

