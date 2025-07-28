package com.undef.manoslocales.data.remote

import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.model.Favorite
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Rutas públicas
    @POST("login")
    suspend fun loginUser(@Body user: User): Response<User>

    @POST("register")
    suspend fun register(@Body user: User): User

    @GET("/")
    suspend fun checkApiStatus(): String

    // Rutas de usuarios
    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("users/by-tel")
    suspend fun getUserByTel(@Query("tel") tel: String): User

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): User

    @POST("users/newUser")
    suspend fun newUser(@Body user: User): Response<Unit>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): User

    @POST("users/register")
    suspend fun registerUser(@Body user: User)

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
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Product

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Unit

    // Rutas de favoritos
    @POST("fav/add")
    suspend fun addToFavorites(@Body favorite: Favorite): Favorite

    @GET("fav/{userId}")
    suspend fun getFavorites(@Path("userId") userId: String): List<Favorite>

    @GET("fav/check/{userId}/{productId}")
    suspend fun checkFavorite(@Path("userId") userId: String, @Path("productId") productId: String): Boolean

    @DELETE("fav/remove/{id}")
    suspend fun removeFavorite(@Path("id") id: String): Unit

    @DELETE("fav/user/{userId}/product/{productId}")
    suspend fun removeFavoriteByUserAndProduct(@Path("userId") userId: String, @Path("productId") productId: String): Unit

    @DELETE("fav/clear/{userId}")
    suspend fun clearFavorites(@Path("userId") userId: String): Unit
}