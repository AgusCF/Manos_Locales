package com.undef.manoslocales.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.undef.manoslocales.data.model.Product

class FavoritesViewModel : ViewModel() {
    // Lista reactiva de productos favoritos
    private val _favorites = mutableStateListOf<Product>()
    val favorites: List<Product> get() = _favorites

    fun toggleFavorite(product: Product) {
        if (_favorites.any { it.id == product.id }) {
            _favorites.removeAll { it.id == product.id }
        } else {
            _favorites.add(product)
        }
    }

    fun isFavorite(product: Product): Boolean {
        return _favorites.any { it.id == product.id }
    }

    fun isFavoriteById(productId: Int): Boolean {
        return _favorites.any { it.id == productId }
    }
}
