package com.undef.manoslocales.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.undef.manoslocales.data.model.Product

class FavoritesViewModel : ViewModel() {
    // Lista reactiva de productos favoritos
    private val _favorites = mutableStateListOf<Product>()
    val favorites: List<Product> get() = _favorites

    fun toggleFavorite(product: Product) {
        if (_favorites.contains(product)) {
            _favorites.remove(product)
        } else {
            _favorites.add(product)
        }
    }

    fun isFavorite(product: Product): Boolean {
        return _favorites.contains(product)
    }
}