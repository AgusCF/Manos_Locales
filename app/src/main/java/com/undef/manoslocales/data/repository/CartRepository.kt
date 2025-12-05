package com.undef.manoslocales.data.repository

import android.util.Log
import com.undef.manoslocales.data.model.CartAddRequest
import com.undef.manoslocales.data.model.CartItem
import com.undef.manoslocales.data.model.CartItemResponse
import com.undef.manoslocales.data.model.CartUpdateRequest
import com.undef.manoslocales.data.model.CartValidationResponse
import com.undef.manoslocales.data.remote.RetrofitInstance
import retrofit2.HttpException
import javax.inject.Inject

class CartRepository @Inject constructor(RetrofitInstance: RetrofitInstance) {

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
            // Log payload JSON to debug mismatches with backend expectations
            try {
                val gson = com.google.gson.Gson()
                val json = gson.toJson(request)
                Log.d("DebugDev", "addToCart payload: $json")
            } catch (e: Exception) {
                Log.w("DebugDev", "No se pudo serializar payload de addToCart", e)
            }
            val response = RetrofitInstance.api.addToCart(request)
            if (response.isSuccessful) {
                Log.d("DebugDev", "addToCart OK: code=${response.code()}")
                Result.success("Producto agregado al carrito")
            } else {
                val code = response.code()
                val body = try { response.errorBody()?.string() } catch (e: Exception) { null }
                Log.w("DebugDev", "addToCart FAILED: code=$code, body=$body")

                val errorMsg = when (code) {
                    400 -> "No hay suficiente stock"
                    404 -> "Producto no encontrado"
                    401 -> "No autorizado. Inicia sesión nuevamente"
                    else -> "Error al agregar al carrito (code=$code)"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: HttpException) {
            val errorMsg = when (e.code()) {
                400 -> "No hay suficiente stock"
                404 -> "Producto no encontrado"
                else -> "Error al agregar al carrito"
            }
            try {
                val errBody = e.response()?.errorBody()?.string()
                Log.e("DebugDev", "addToCart HttpException: code=${e.code()} body=$errBody", e)
            } catch (_: Exception) {}
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