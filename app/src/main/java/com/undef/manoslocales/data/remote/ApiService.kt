package com.undef.manoslocales.data.remote

import com.undef.manoslocales.data.model.Product
import retrofit2.http.GET

interface ApiService {
    @GET("/api/products")
    suspend fun getProducts(): List<Product>
}