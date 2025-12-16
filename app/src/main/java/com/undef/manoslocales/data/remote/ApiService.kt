package com.undef.manoslocales.data.remote

import com.undef.manoslocales.data.model.Cart
import com.undef.manoslocales.data.model.CartAddRequest
import com.undef.manoslocales.data.model.CartItemResponse
import com.undef.manoslocales.data.model.CartUpdateRequest
import com.undef.manoslocales.data.model.CartValidationResponse
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.model.UpdateUserRequest
import com.undef.manoslocales.data.model.Favorite
import com.undef.manoslocales.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Rutas públicas
    @POST("login")
    suspend fun loginUser(@Body credentials: Map<String, String>): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body user: User): User

    @GET("/")
    suspend fun checkApiStatus(): String

    // Rutas de usuarios
    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("users/by-tel")
    suspend fun getUserByTel(@Query("tel") tel: String): User

    @GET("users/by-mail")
    suspend fun getUserByMail(@Query("email") email: String): User

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int?): User

    @POST("users/newUser")
    suspend fun newUser(@Body user: User): Response<Unit>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<User>

    @POST("users/register")
    suspend fun registerUser(@Body user: User)

    @PUT("users/newPass/{id}")
    suspend fun updatedPassword(
        @Path("id") id: String,
        @Body request: PasswordChangeRequest
    ): Response<Void>

    // Rutas de productos
    @GET("products")
    suspend fun getAllProducts(): List<Product>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): List<Product>

    @POST("products/newProduct")
    suspend fun createProduct(@Body product: Product): Product

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Product

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Unit

    // Rutas de favoritos
    @POST("fav/add")
    suspend fun addToFavorites(@Body favorite: Favorite): Favorite

    @GET("fav/{userId}")
    suspend fun getFavorites(@Path("userId") userid: Int): List<Favorite>

    @GET("fav/check/{userId}/{productId}")
    suspend fun checkFavorite(@Path("userId") userid: Int, @Path("productId") productid: Int): Boolean

    @DELETE("fav/remove/{id}")
    suspend fun removeFavorite(@Path("id") id: Int): Unit

    @DELETE("fav/user/{userId}/product/{productId}")
    suspend fun removeFavoriteByUserAndProduct(@Path("userId") userId: String, @Path("productId") productid: Int): Unit

    @DELETE("fav/clear/{userId}")
    suspend fun clearFavorites(@Path("userId") userid: Int): Unit

    // ✅ Rutas de carrito (cart)
    @GET("cart/{userId}")
    suspend fun getCart(@Path("userId") userId: Int): List<CartItemResponse>

    @POST("cart/add")
    suspend fun addToCart(@Body request: CartAddRequest): Response<Unit>

    @PUT("cart/update/{id}")
    suspend fun updateCartItem(
        @Path("id") id: Int,
        @Body request: CartUpdateRequest
    ): Response<Unit>

    @DELETE("cart/remove/{id}")
    suspend fun removeFromCart(
        @Path("id") id: Int
    ): Response<Unit>

    @DELETE("cart/clear/{userId}")
    suspend fun clearCart(@Path("userId") userId: Int): Response<Unit>

    @GET("cart/validate/{userId}")
    suspend fun validateCart(@Path("userId") userId: Int): CartValidationResponse
}