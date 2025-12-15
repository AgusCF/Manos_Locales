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

    private val userId: Int?
        get() = tokenProvider.getUserId()

    init {
        loadCart()
    }

    private fun loadCart() = viewModelScope.launch {
        try {
            val uid = userId
            if (uid == null) {
                Log.w("DebugDev", "No hay userId - no se carga carrito")
                _cart.value = emptyList()
                return@launch
            }
            _cart.value = repository.getCart(uid)
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
            val uid = userId
            if (uid == null || uid <= 0) {
                val err = Exception("Usuario no logueado")
                Log.w("DebugDev", "Intento de agregar al carrito sin userId")
                onResult(Result.failure(err))
                return@launch
            }
            val result = repository.addItem(uid, item)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Producto agregado al carrito: ${product.name}")
            }.onFailure {
                Log.e("DebugDev", "❌ Error agregando producto al carrito", it)
            }
            onResult(result)
        }
    }

    fun removeItem(itemId: Int, onResult: (Result<String>) -> Unit) {
        Log.d("DebugDev", "removeItem llamado con itemId=$itemId")
        viewModelScope.launch {
            val uid = userId
            if (uid == null) {
                Log.w("DebugDev", "Usuario no logueado")
                onResult(Result.failure(Exception("Usuario no logueado")))
                return@launch
            }
            val result = repository.removeItem(itemId)
            result.onSuccess {
                Log.d("DebugDev", "✅ Producto eliminado del carrito: itemId $itemId")
                loadCart()
            }.onFailure { error ->
                Log.e("DebugDev", "❌ Error eliminando producto del carrito", error)
            }
            onResult(result)
        }
    }

    fun updateQuantity(itemId: Int, quantity: Int, onResult: (Result<String>) -> Unit) {
        Log.d("DebugDev", "updateQuantity llamado con itemId=$itemId, newQty=$quantity")
        if (quantity <= 0) {
            Log.w("DebugDev", "⚠️ Cantidad inválida: $quantity")
            onResult(Result.failure(Exception("Cantidad debe ser mayor a 0")))
            return
        }

        viewModelScope.launch {
            val uid = userId
            if (uid == null) {
                Log.w("DebugDev", "Usuario no logueado")
                onResult(Result.failure(Exception("Usuario no logueado")))
                return@launch
            }
            val result = repository.updateQuantity(itemId, quantity)
            result.onSuccess {
                Log.d("DebugDev", "✅ Cantidad actualizada: itemId $itemId → $quantity")
                loadCart()
            }.onFailure { error ->
                Log.e("DebugDev", "❌ Error actualizando cantidad", error)
            }
            onResult(result)
        }
    }

    fun clearCart(onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val uid = userId
            if (uid == null) {
                onResult(Result.failure(Exception("Usuario no logueado")))
                return@launch
            }
            val result = repository.clearCart(uid)
            result.onSuccess {
                loadCart()
                Log.d("DebugDev", "✅ Carrito vaciado")
            }.onFailure {
                Log.e("DebugDev", "❌ Error vaciando carrito", it)
            }
            onResult(result)
        }
    }
}