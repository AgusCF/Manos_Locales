package com.undef.manoslocales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.local.AuthTokenProvider
import com.undef.manoslocales.data.model.Cart
import com.undef.manoslocales.data.model.CartItem
import com.undef.manoslocales.data.model.CartItemResponse
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository,
    private val tokenProvider: AuthTokenProvider
) : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItemResponse>>(emptyList())
    val cart: StateFlow<List<CartItemResponse>> = _cart

    private val userId: Int
        get() = tokenProvider.getUserId() ?: 0

    init {
        loadCart()
    }

    private fun loadCart() = viewModelScope.launch {
        try {
            _cart.value = repository.getCart(userId)
            Log.d("DebugDev", "✅ Carrito cargado: ${_cart.value.size} items")
        } catch (e: Exception) {
            Log.e("DebugDev", "❌ Error cargando carrito", e)
        }
    }

    fun addItemFromProduct(product: Product, onResult: (Result<String>) -> Unit) {
        if (product.stock <= 0) {
            Log.w("DebugDev", "⚠️ No se puede agregar producto sin stock")
            onResult(Result.failure(Exception("Producto sin stock")))
            return
        }

        val item = CartItem(
            productId = product.id,
            name = product.name,
            price = product.price.toDoubleOrNull() ?: 0.0,
            quantity = 1,
            imageUrl = product.imageUrl,
            stock = product.stock
        )

        viewModelScope.launch {
            val result = repository.addItem(userId, item)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Producto agregado al carrito: ${product.name}")
            }.onFailure {
                Log.e("DebugDev", "❌ Error agregando producto al carrito", it)
            }
            onResult(result)
        }
    }

    fun removeItem(productId: Int, onResult: (Result<String>) -> Unit) {
        val item = _cart.value.find { it.product_id == productId }
        if (item == null) {
            onResult(Result.failure(Exception("Producto no encontrado en el carrito")))
            return
        }

        viewModelScope.launch {
            val result = repository.removeItem(item.id)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Producto eliminado del carrito: ID $productId")
            }.onFailure {
                Log.e("DebugDev", "❌ Error eliminando producto del carrito", it)
            }
            onResult(result)
        }
    }

    fun updateQuantity(productId: Int, quantity: Int, onResult: (Result<String>) -> Unit) {
        if (quantity <= 0) {
            Log.w("DebugDev", "⚠️ Cantidad inválida: $quantity")
            onResult(Result.failure(Exception("Cantidad inválida")))
            return
        }

        val item = _cart.value.find { it.product_id == productId }
        if (item == null) {
            onResult(Result.failure(Exception("Producto no encontrado en el carrito")))
            return
        }

        viewModelScope.launch {
            val result = repository.updateQuantity(item.id, quantity)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Cantidad actualizada: Producto $productId → $quantity")
            }.onFailure {
                Log.e("DebugDev", "❌ Error actualizando cantidad", it)
            }
            onResult(result)
        }
    }

    fun clearCart(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = repository.clearCart(userId)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Carrito vaciado")
            }.onFailure {
                Log.e("DebugDev", "❌ Error vaciando carrito", it)
            }
            onResult(result)
        }
    }

    fun validateCart() = viewModelScope.launch {
        try {
            val result = repository.validateCart(userId)
            if (!result.isValid) {
                Log.w("DebugDev", "⚠️ Carrito inválido: ${result.invalidItems.size} items con problema")
                result.invalidItems.forEach {
                    Log.w("DebugDev", "→ ${it.name}: pide ${it.requestedQuantity}, hay ${it.availableStock}")
                }
            } else {
                Log.d("DebugDev", "✅ Carrito válido")
            }
        } catch (e: Exception) {
            Log.e("DebugDev", "❌ Error validando carrito", e)
        }
    }
}