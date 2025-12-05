package com.undef.manoslocales.data.repository

import android.util.Log
import com.undef.manoslocales.data.model.Cart
import com.undef.manoslocales.data.model.CartAddRequest
import com.undef.manoslocales.data.model.CartItem
import com.undef.manoslocales.data.model.CartItemResponse
import com.undef.manoslocales.data.model.CartUpdateRequest
import com.undef.manoslocales.data.model.CartValidationResponse
import com.undef.manoslocales.data.remote.RetrofitInstance
import retrofit2.HttpException
import javax.inject.Inject

class CartRepository @Inject constructor(
) {

    suspend fun getCart(userId: Int): List<CartItemResponse> {
        return RetrofitInstance.api.getCart(userId)
    }

    suspend fun addItem(userId: Int, item: CartItem): Result<String> {
        return try {
            Log.d("DebugDev", "📦 Enviando addItem → userId=$userId, productId=${item.productId}, quantity=${item.quantity}")
            val request = CartAddRequest(
                userId = userId,
                productId = item.productId,
                quantity = item.quantity
            )
            val response = RetrofitInstance.api.addToCart(request)
            if (response.isSuccessful) {
                Result.success("Producto agregado al carrito")
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "No hay suficiente stock"
                    404 -> "Producto no encontrado"
                    else -> "Error al agregar al carrito"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "No hay suficiente stock"
                404 -> "Producto no encontrado"
                else -> "Error al agregar al carrito"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun updateQuantity(itemId: Int, quantity: Int): Result<String> {
        return try {
            val request = CartUpdateRequest(quantity = quantity)
            val response = RetrofitInstance.api.updateCartItem(itemId, request)
            if (response.isSuccessful) {
                Result.success("Cantidad actualizada")
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "No hay suficiente stock"
                    404 -> "Ítem no encontrado en el carrito"
                    else -> "Error al actualizar cantidad"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "No hay suficiente stock"
                404 -> "Ítem no encontrado en el carrito"
                else -> "Error al actualizar cantidad"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun removeItem(itemId: Int): Result<String> {
        return try {
            val response = RetrofitInstance.api.removeFromCart(itemId)
            if (response.isSuccessful) {
                Result.success("Producto eliminado del carrito")
            } else {
                Result.failure(Exception("Error al eliminar producto del carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun clearCart(userId: Int): Result<String> {
        return try {
            val response = RetrofitInstance.api.clearCart(userId)
            if (response.isSuccessful) {
                Result.success("Carrito vaciado")
            } else {
                Result.failure(Exception("Error al vaciar el carrito"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun validateCart(userId: Int): CartValidationResponse {
        return RetrofitInstance.api.validateCart(userId)
    }
}