package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class RemoteOrderItem(
    @Json(name = "menu_item_id") val menuItemId: Int,
    @Json(name = "variant_id") val variantId: Int? = null,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "addon_ids") val addonIds: List<Int> = emptyList(),
    @Json(name = "special_instructions") val specialInstructions: String? = null
)

data class SubmitOrderRequest(
    @Json(name = "items") val items: List<RemoteOrderItem>,
    @Json(name = "order_type") val orderType: String, // "delivery" or "takeaway"
    @Json(name = "table_token") val tableToken: String? = null,
    @Json(name = "special_instructions") val specialInstructions: String? = null,
    @Json(name = "customer_name") val customerName: String?,
    @Json(name = "customer_phone") val customerPhone: String?,
    @Json(name = "customer_email") val customerEmail: String,
    @Json(name = "delivery_address") val deliveryAddress: String?,
    @Json(name = "delivery_notes") val deliveryNotes: String?,
    @Json(name = "delivery_fee_paisa") val deliveryFeePaisa: Int = 0,
    @Json(name = "customer_lat") val customerLat: String? = null,
    @Json(name = "customer_lng") val customerLng: String? = null
)

data class SubmitOrderResponse(
    @Json(name = "tracking_url") val trackingUrl: String? = null,
    @Json(name = "order_id") val orderId: String? = null,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

data class ServerTimeResponse(
    @Json(name = "bs_year") val bsYear: Int? = null,
    @Json(name = "bs_month") val bsMonth: Int? = null,
    @Json(name = "bs_day") val bsDay: Int? = null,
    @Json(name = "time") val timeString: String? = null
)

interface MeghaApiService {

    @POST("/orders/submit")
    suspend fun submitOrder(
        @Header("X-Idempotency-Key") idempotencyKey: String,
        @Body request: SubmitOrderRequest
    ): Response<SubmitOrderResponse>

    @GET("/api/time")
    suspend fun getServerTime(): Response<ServerTimeResponse>
}

object ApiClient {
    private const val BASE_URL = "https://meghahotel.vercel.app"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: MeghaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MeghaApiService::class.java)
    }
}
