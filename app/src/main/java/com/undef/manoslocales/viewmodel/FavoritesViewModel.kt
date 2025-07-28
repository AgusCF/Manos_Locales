package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.undef.manoslocales.data.model.Product
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    // Lista reactiva de productos favoritos
    private val _favorites = MutableStateFlow(emptyList<Product>())
    val favorites = _favorites

    // Estados de carga y errores
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage

    fun toggleFavorite(product: Product) {
        val currentFavorites = _favorites.value.toMutableList()
        if (currentFavorites.contains(product)) {
            currentFavorites.remove(product)
        } else {
            currentFavorites.add(product)
        }
        _favorites.value = currentFavorites
    }

    fun isFavorite(product: Product): Boolean {
        return _favorites.value.contains(product)
    }

    // Ejemplo de función para simular carga de favoritos
    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Simulación de carga de datos
                Thread.sleep(2000) // Simulación de retraso
                // Aquí podrías cargar los favoritos desde una API o base de datos
                _favorites.value = listOf(/* cargar productos desde el backend */)
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los favoritos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}