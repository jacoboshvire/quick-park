package com.example.quickpark.data.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/* =========================
   AUTH
========================= */

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: LoggedInUser
)

data class LoggedInUser(
    @SerializedName("_id")
    val id: String,
    val fullname: String,
    val email: String
)

/* =========================
   PROFILE
========================= */

data class UserProfile(
    @SerializedName("_id")
    val id: String,
    val fullname: String,
    val username: String,
    val email: String,
    val avatar: String?
)

data class UpdateUserResponse(
    val message: String,
    val user: UserProfile
)

/* =========================
   SELLERS
========================= */

data class SellerResponse(
    @SerializedName("Seller")
    val sellers: List<SellerItem>,
    val page: Int,
    val limit: Int
)

data class SellerItem(
    @SerializedName("_id")
    val id: String,
    val locations: String,
    val price: Int,
    val lat: Double,
    @SerializedName("long")
    val lng: Double,
    val image: String,
    val user: SellerUser
)

data class SellerUser(
    @SerializedName("_id")
    val id: String,
    val fullname: String,
    val email: String,
    val username: String,
    val avatar: String
)

data class BookingResponse(
    val message: String,
    val booking: Booking
)

data class Booking(
    val _id: String,
    val status: String,
    val sellerPost: String,
    val buyer: String
)

data class ApiMessage(
    val message: String
)

data class NotificationItem(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val body: String,
    val read: Boolean,
    val type: String,
    val createdAt: String,
    val data: Map<String, String>
)

/* =========================
   API SERVICE
========================= */

interface ApiService {

    // 🔐 LOGIN
    @POST("user/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // GET LOGGED-IN USER
    @GET("user/me")
    suspend fun getMe(): Response<UserProfile>

    // UPDATE USER (TEXT + IMAGE)
    @Multipart
    @PUT("user/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part?
    ): Response<UpdateUserResponse>

    // GET PARKING SPACES
    @GET("sellerpost")
    suspend fun getSellers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<SellerResponse>

    @Multipart
    @POST("sellerpost")
    suspend fun createSellerPost(
        @Part image: MultipartBody.Part,
        @Part("locations") locations: RequestBody,
        @Part("postalcode") postalcode: RequestBody,
        @Part("phonenumber") phonenumber: RequestBody,
        @Part("price") price: RequestBody,
        @Part("accountname") accountname: RequestBody,
        @Part("accountnumber") accountnumber: RequestBody,
        @Part("sortcode") sortcode: RequestBody,
        @Part("timeNeeded") timeNeeded: RequestBody,
        @Part("duration") duration: RequestBody,
        token: String
    ): Response<Any>
    
    @GET("sellerpost/{id}")
    suspend fun getSellerById(
        @Path("id") id: String
    ): Response<SellerItem>

    @POST("booking/book/{sellerId}")
    suspend fun bookParking(
        @Header("Authorization") token: String,
        @Path("sellerId") sellerId: String
    ): Response<BookingResponse>

    @PUT("booking/booking/{id}/accept")
    suspend fun acceptBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: String
    ): Response<ApiMessage>

    @PUT("booking/booking/{id}/reject")
    suspend fun rejectBooking(
        @Header("Authorization") token: String,
        @Path("id") bookingId: String
    ): Response<ApiMessage>

    @GET("notification")
    suspend fun getNotifications(): Response<List<NotificationItem>>

    @PUT("notification/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): Response<Unit>

    @DELETE("notification/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String
    ): Response<Unit>
}
