package com.undef.manoslocales.data.repository

import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.remote.RetrofitInstance

class ProductRepository {

    suspend fun getAllProducts(): List<Product> {
        return RetrofitInstance.api.getAllProducts()
    }

    suspend fun getProductById(id: Int): Product {
        return RetrofitInstance.api.getProductById(id)
    }

    suspend fun getProductsByCategory(category: String): List<Product> {
        return RetrofitInstance.api.getProductsByCategory(category)
    }

    suspend fun createProduct(product: Product): Product {
        return RetrofitInstance.api.createProduct(product)
    }

    suspend fun updateProduct(id: Int, product: Product): Product {
        return RetrofitInstance.api.updateProduct(id, product)
    }

    suspend fun deleteProduct(id: Int) {
        RetrofitInstance.api.deleteProduct(id)
    }
}