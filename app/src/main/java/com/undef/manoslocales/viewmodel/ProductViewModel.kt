package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.remote.RetrofitInstance
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    // Lista reactiva de productos
    private val _products = MutableStateFlow(emptyList<Product>())
    val products = _products

    // Producto seleccionado
    private var _selectedProduct: Product? = null
    val selectedProduct: Product? get() = _selectedProduct

    // Estados de carga y errores
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage

    fun refreshProducts() {
        loadProducts() // O tu método existente
    }

    // Método para obtener todos los productos del backend
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitInstance.api.getAllProducts()
                _products.value = response
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Método para obtener un producto por ID del backend
    fun fetchProductById(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitInstance.api.getProductById(productId)
                _selectedProduct = response
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}