package com.undef.manoslocales.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.remote.RetrofitInstance
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            isLoading = true
            try {
                products = RetrofitInstance.api.getProducts()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}