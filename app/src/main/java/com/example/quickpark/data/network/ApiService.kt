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

}
