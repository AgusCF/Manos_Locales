package com.undef.manoslocales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.model.Product
import com.undef.manoslocales.data.model.Favorite
import com.undef.manoslocales.data.remote.ApiService
import com.undef.manoslocales.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {

    // Lista reactiva de productos favoritos
    private val _favorites = MutableStateFlow<List<Product>>(emptyList())
    val favorites: StateFlow<List<Product>> = _favorites

    // Estados de carga y errores
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        Log.i("FavoritesVM", "FavoritesViewModel inicializado")
        loadFavoritesFromApi()
    }

    fun toggleFavorite(product: Product) {
        Log.d("FavoritesVM", "toggleFavorite llamado - Producto ID: ${product.id}, Nombre: ${product.name}")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("FavoritesVM", "Obteniendo userId...")
                val userId = userRepository.getUserId()
                Log.d("FavoritesVM", "UserId obtenido: $userId")

                if (userId == null) {
                    Log.w("FavoritesVM", "Usuario no logueado - no se puede manejar favorito")
                    _errorMessage.value = "Usuario no logueado"
                    return@launch
                }

                val isCurrentlyFavorite = _favorites.value.any { it.id == product.id }
                Log.d("FavoritesVM", "Estado actual del favorito: $isCurrentlyFavorite")

                if (isCurrentlyFavorite) {
                    Log.i("FavoritesVM", "Removiendo favorito - UserId: $userId, ProductId: ${product.id}")
                    removeFavoriteFromApi(userId, product.id)
                } else {
                    Log.i("FavoritesVM", "Agregando favorito - UserId: $userId, ProductId: ${product.id}")
                    addFavoriteToApi(userId, product.id)
                }

                // 🔄 **ACTUALIZACIÓN CRÍTICA: Recargar favoritos después de cualquier cambio**
                Log.d("FavoritesVM", "Recargando favoritos después del toggle...")
                loadFavoritesFromApi()

            } catch (e: Exception) {
                Log.e("FavoritesVM", "EXCEPCIÓN en toggleFavorite: ${e.message}", e)
                _errorMessage.value = "Error al actualizar favoritos: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d("FavoritesVM", "toggleFavorite finalizado")
            }
        }
    }

    private suspend fun addFavoriteToApi(userId: Int, productId: Int) {
        Log.d("FavoritesVM", "addFavoriteToApi - UserId: $userId, ProductId: $productId")

        try {
            val favorite = Favorite(userId = userId, productId = productId)
            Log.v("FavoritesVM", "Creando objeto Favorite: $favorite")

            // VERIFICAR SI EL PRODUCTO EXISTE EN LA BD
            try {
                Log.d("FavoritesVM", "Verificando si el producto existe...")
                val productCheck = apiService.getProductById(productId)
                Log.d("FavoritesVM", "Producto verificado: ${productCheck.name}")
            } catch (e: Exception) {
                Log.e("FavoritesVM", "ERROR: Producto no encontrado en BD - ID: $productId")
                throw Exception("Producto no existe en la base de datos")
            }

            Log.i("FavoritesVM", "Llamando a apiService.addToFavorites...")
            val response = apiService.addToFavorites(favorite)
            Log.d("FavoritesVM", "addToFavorites respuesta: $response")

            Log.i("FavoritesVM", "Favorito agregado exitosamente")

        } catch (e: Exception) {
            Log.e("FavoritesVM", "ERROR en addFavoriteToApi: ${e.message}", e)

            // AGREGAR MÁS INFORMACIÓN SOBRE EL ERROR 500
            if (e is retrofit2.HttpException) {
                Log.e("FavoritesVM", "CÓDIGO HTTP: ${e.code()}")
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("FavoritesVM", "CUERPO DEL ERROR: $errorBody")
                } catch (bodyEx: Exception) {
                    Log.w("FavoritesVM", "No se pudo obtener el cuerpo del error")
                }
            }
            throw Exception("Error al agregar favorito: ${e.message}")
        }
    }

    private suspend fun removeFavoriteFromApi(userId: Int, productId: Int) {
        Log.d("FavoritesVM", "removeFavoriteFromApi - UserId: $userId, ProductId: $productId")

        try {
            Log.d("FavoritesVM", "Obteniendo lista de favoritos del usuario...")
            val userFavorites = apiService.getFavorites(userId)
            Log.d("FavoritesVM", "Favoritos obtenidos: ${userFavorites.size} elementos")

            val favoriteToRemove = userFavorites.find { it.productId == productId }
            Log.v("FavoritesVM", "FavoriteToRemove encontrado: $favoriteToRemove")

            if (favoriteToRemove != null) {
                Log.i("FavoritesVM", "Eliminando favorito por ID: ${favoriteToRemove.id}")
                apiService.removeFavorite(favoriteToRemove.id!!)
                Log.i("FavoritesVM", "Favorito eliminado por ID exitosamente")
            } else {
                Log.i("FavoritesVM", "No se encontró favorito específico, eliminando por usuario y producto")
                apiService.removeFavoriteByUserAndProduct(userId.toString(), productId)
                Log.i("FavoritesVM", "Favorito eliminado por usuario y producto exitosamente")
            }

            Log.i("FavoritesVM", "Favorito removido exitosamente")

        } catch (e: Exception) {
            Log.e("FavoritesVM", "ERROR en removeFavoriteFromApi: ${e.message}", e)
            throw Exception("Error al remover favorito: ${e.message}")
        }
    }

    fun loadFavoritesFromApi() {
        Log.d("FavoritesVM", "loadFavoritesFromApi llamado")

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("FavoritesVM", "Obteniendo userId para cargar favoritos...")
                val userId = userRepository.getUserId()
                Log.d("FavoritesVM", "UserId para cargar favoritos: $userId")

                if (userId == null) {
                    Log.w("FavoritesVM", "Usuario no logueado - no se pueden cargar favoritos")
                    _errorMessage.value = "Usuario no logueado"
                    _favorites.value = emptyList() // 🔄 Limpiar lista si no hay usuario
                    return@launch
                }

                Log.i("FavoritesVM", "Llamando a apiService.getFavorites($userId)...")
                val favoriteList = apiService.getFavorites(userId)
                Log.d("FavoritesVM", "Favoritos obtenidos de API: ${favoriteList.size} elementos")

                // Convertir los favoritos a productos
                val favoriteProducts = mutableListOf<Product>()
                Log.d("FavoritesVM", "Convirtiendo favoritos a productos...")

                for (favorite in favoriteList) {
                    try {
                        Log.v("FavoritesVM", "Obteniendo producto ID: ${favorite.productId}")
                        val product = apiService.getProductById(favorite.productId)
                        Log.v("FavoritesVM", "Producto obtenido: ${product.name}")
                        favoriteProducts.add(product)
                    } catch (e: Exception) {
                        Log.e("FavoritesVM", "ERROR obteniendo producto ${favorite.productId}: ${e.message}")
                    }
                }

                Log.i("FavoritesVM", "Productos favoritos finales: ${favoriteProducts.size} elementos")
                _favorites.value = favoriteProducts
                Log.d("FavoritesVM", "Favoritos cargados exitosamente en el StateFlow")

            } catch (e: Exception) {
                Log.e("FavoritesVM", "ERROR en loadFavoritesFromApi: ${e.message}", e)
                _errorMessage.value = "Error al cargar favoritos: ${e.message}"
                _favorites.value = emptyList() // 🔄 Limpiar lista en caso de error
            } finally {
                _isLoading.value = false
                Log.d("FavoritesVM", "loadFavoritesFromApi finalizado")
            }
        }
    }

    // 🔄 **NUEVO: Método público para forzar recarga**
    fun refreshFavorites() {
        Log.i("FavoritesVM", "refreshFavorites llamado explícitamente")
        loadFavoritesFromApi()
    }

    // 🔄 **NUEVO: Método para cargar favoritos con ID específico**
    fun loadFavorites(userId: Int) {
        Log.d("FavoritesVM", "loadFavorites con userId específico: $userId")
        viewModelScope.launch {
            try {
                val favoriteList = apiService.getFavorites(userId)
                val favoriteProducts = mutableListOf<Product>()

                for (favorite in favoriteList) {
                    try {
                        val product = apiService.getProductById(favorite.productId)
                        favoriteProducts.add(product)
                    } catch (e: Exception) {
                        Log.e("FavoritesVM", "Error obteniendo producto ${favorite.productId}")
                    }
                }

                _favorites.value = favoriteProducts
                Log.i("FavoritesVM", "Favoritos cargados para userId $userId: ${favoriteProducts.size} elementos")
            } catch (e: Exception) {
                Log.e("FavoritesVM", "ERROR en loadFavorites: ${e.message}")
                _errorMessage.value = "Error al cargar favoritos: ${e.message}"
            }
        }
    }

    fun isFavorite(product: Product): Boolean {
        val result = _favorites.value.any { it.id == product.id }
        Log.v("FavoritesVM", "isFavorite - Producto ID: ${product.id}, Resultado: $result")
        return result
    }

    fun clearError() {
        Log.d("FavoritesVM", "clearError llamado")
        _errorMessage.value = null
    }

    // 🔄 **NUEVO: Método para obtener el estado actual inmediatamente**
    fun getCurrentFavorites(): List<Product> {
        return _favorites.value
    }
}