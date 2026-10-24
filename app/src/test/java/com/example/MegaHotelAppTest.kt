package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MeghaRepository
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MegaHotelAppTest {

    private lateinit var context: Context
    private lateinit var repository: MeghaRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = MeghaRepository(context)
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Mega Hotel", appName)
    }

    @Test
    fun testInitialMenuLoadedFromVerifiedWebsite() {
        val menu = repository.menuItems.value
        assertTrue("Menu should contain verified dishes", menu.isNotEmpty())
        assertEquals(12, menu.size)

        // Verify specific dishes from meghahotel.vercel.app
        assertTrue(menu.any { it.nameEn == "Chicken Wings" && it.priceNpr == 500.0 })
        assertTrue(menu.any { it.nameEn == "Steam Momo (Chicken)" && it.priceNpr == 150.0 })
        assertTrue(menu.any { it.nameEn == "Jhol Momo" && it.priceNpr == 160.0 })
        assertTrue(menu.any { it.nameEn == "Chicken Chowmein" && it.priceNpr == 200.0 })
        assertTrue(menu.any { it.nameEn == "Chicken Roast" && it.priceNpr == 200.0 })
    }

    @Test
    fun testCartOperationsAndTotals() {
        repository.clearCart()
        val steamMomo = repository.menuItems.value.first { it.nameEn == "Steam Momo (Chicken)" } // Rs. 150
        val chowmein = repository.menuItems.value.first { it.nameEn == "Chicken Chowmein" } // Rs. 200

        repository.addToCart(steamMomo, 2) // 2 * 150 = 300
        repository.addToCart(chowmein, 1)  // 1 * 200 = 200

        assertEquals(2, repository.cartItems.value.size)
        assertEquals(500.0, repository.calculateSubtotal(), 0.01)

        // Apply MEGHA50 coupon (Rs. 50 off on orders >= 400)
        val couponResult = repository.applyCoupon("MEGHA50")
        assertTrue(couponResult.isSuccess)
        assertEquals(50.0, repository.calculateDiscount(), 0.01)

        // Update quantity
        repository.updateCartQuantity(steamMomo.id, 1) // 1 * 150 = 150; subtotal now 350
        assertEquals(350.0, repository.calculateSubtotal(), 0.01)
    }

    @Test
    fun testDeliveryChargeRules() {
        // Center location: 26.665420913346072, 87.63826396825684
        // Within 2km (e.g. Urlabari 26.664175, 87.637234) -> FREE delivery (Rs. 0)
        val freeChargeWithin2km = repository.calculateDeliveryCharge(26.664175, 87.637234)
        assertEquals(0.0, freeChargeWithin2km, 0.01)

        // Beyond 2km (e.g. Damak 26.6631, 87.6975 is ~6km away) -> Fixed delivery charge Rs. 100
        val damakCharge = repository.calculateDeliveryCharge(26.6631, 87.6975)
        assertEquals(100.0, damakCharge, 0.01)

        // Null coordinates -> Fixed delivery charge Rs. 100
        val unknownLocCharge = repository.calculateDeliveryCharge(null, null)
        assertEquals(100.0, unknownLocCharge, 0.01)
    }

    @Test
    fun testMinimumOrderAmountEnforced() = runBlocking {
        repository.clearCart()
        val momo = repository.menuItems.value.first { it.nameEn == "Steam Momo (Chicken)" }
        repository.addToCart(momo, 2) // 2 * 150 = 300 < 500

        val result = repository.placeOrder(
            customerName = "Bikash Nepal",
            customerPhone = "9819329559",
            customerEmail = "bikash@example.com",
            deliveryAddress = "Urlabari-5, Morang",
            locality = "Urlabari",
            landmark = "Near Hospital",
            deliveryNotes = "Call gate",
            specialInstructions = "Extra spicy chutney",
            orderType = "delivery",
            paymentMethod = PaymentMethod.COD,
            customerLat = 26.665421,
            customerLng = 87.638264
        )

        assertFalse("Order below Rs. 500 must be rejected", result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("500") == true)
    }

    @Test
    fun testOrderPlacementAndTrackingToken() = runBlocking {
        repository.clearCart()
        val momo = repository.menuItems.value.first { it.nameEn == "Steam Momo (Chicken)" }
        repository.addToCart(momo, 4) // 4 * 150 = 600 >= 500

        val result = repository.placeOrder(
            customerName = "Bikash Nepal",
            customerPhone = "9819329559",
            customerEmail = "bikash@example.com",
            deliveryAddress = "Urlabari-5, Morang",
            locality = "Urlabari",
            landmark = "Near Hospital",
            deliveryNotes = "Call gate",
            specialInstructions = "Extra spicy chutney",
            orderType = "delivery",
            paymentMethod = PaymentMethod.COD,
            customerLat = 26.665421,
            customerLng = 87.638264
        )

        assertTrue(result.isSuccess)
        val order = result.getOrThrow()
        assertTrue(order.orderNumber.startsWith("MH-"))
        assertTrue(order.trackingToken.startsWith("trk_"))
        assertEquals(OrderStatus.PENDING, order.orderStatus)
        assertEquals(600.0, order.subtotalNpr, 0.01)
        assertEquals(1, order.items.size)

        // Verify retrieval by token
        val retrieved = repository.getOrderByToken(order.trackingToken)
        assertNotNull(retrieved)
        assertEquals(order.orderNumber, retrieved?.orderNumber)
    }

    @Test
    fun testStaffAndAdminAuthentication() {
        // Admin: admin@megahotel.com (password: megahotel@123)
        val adminRes = repository.login("admin@megahotel.com", "megahotel@123")
        assertTrue(adminRes.isSuccess)
        assertEquals(UserRole.ADMIN, adminRes.getOrNull()?.role)

        // Kitchen Staff: kitchen@megahotel.com (password: megahotel@123)
        val kitchenRes = repository.login("kitchen@megahotel.com", "megahotel@123")
        assertTrue(kitchenRes.isSuccess)
        assertEquals(UserRole.KITCHEN, kitchenRes.getOrNull()?.role)

        // Rider: rider@megahotel.com (password: megahotel@123)
        val riderRes1 = repository.login("rider@megahotel.com", "megahotel@123")
        assertTrue(riderRes1.isSuccess)
        assertEquals(UserRole.RIDER, riderRes1.getOrNull()?.role)

        // Rider 2: rider2@megahotel.com (password: megahotel@123)
        val riderRes2 = repository.login("rider2@megahotel.com", "megahotel@123")
        assertTrue(riderRes2.isSuccess)
        assertEquals(UserRole.RIDER, riderRes2.getOrNull()?.role)

        // Invalid Password for staff
        val badRes = repository.login("admin@megahotel.com", "wrongpassword")
        assertFalse(badRes.isSuccess)
    }

    @Test
    fun testNotificationSystem() {
        assertNotNull(repository.notificationHelper)
        // Verify sendTestNotification executes safely without crashing
        repository.sendTestNotification()
    }
}
