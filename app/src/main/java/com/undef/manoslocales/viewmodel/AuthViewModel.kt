package com.undef.manoslocales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.undef.manoslocales.data.local.AuthTokenProvider
import com.undef.manoslocales.data.local.preference.PreferencesManager
import com.undef.manoslocales.data.model.GoogleUser
import com.undef.manoslocales.data.repository.UserRepository
import com.undef.manoslocales.ui.theme.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository,
    private val tokenProvider: AuthTokenProvider,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // ✅ Función para verificar si ya está logueado al iniciar la app
    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                // ⏰ Pequeño delay para asegurar que el token esté disponible
                kotlinx.coroutines.delay(500)

                val token = tokenProvider.getToken()
                val userId = tokenProvider.getUserId()

                // Verificación más robusta
                val isValidSession = token != null &&
                        userId != null &&
                        token.isNotBlank() &&
                        userId > 0

                _isLoggedIn.value = isValidSession
                _isInitialized.value = true

                Log.d("DebugDev", "🔍 Auth status - Token: ${token?.take(10)}..., UserId: $userId")
                Log.d("DebugDev", "🔍 Auth status - LoggedIn: ${_isLoggedIn.value}")
            } catch (e: Exception) {
                Log.e("DebugDev", "❌ Error en checkAuthStatus: ${e.message}")
                _isLoggedIn.value = false
                _isInitialized.value = true
            }
        }
    }

    // ✅ Función para refresh
    fun refresh() {
        viewModelScope.launch {
            val token = tokenProvider.getToken()
            val userId = tokenProvider.getUserId()
            _isLoggedIn.value = token != null && userId != null
            Log.d("DebugDev", "🔄 Auth refresh - LoggedIn: ${_isLoggedIn.value}")
        }
    }

    // ✅ Función para Google Sign-In
    fun signInWithGoogle(googleUser: GoogleUser, navController: NavController) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("DebugDev", "🎯 Iniciando Google Sign-In para usuario: ${googleUser.email}")

                // Validar datos del usuario Google
                if (googleUser.email.isNullOrEmpty()) {
                    _errorMessage.value = "Error: No se pudo obtener el email de Google"
                    Log.e("DebugDev", "❌ Email de Google está vacío")
                    return@launch
                }

                // Agregar timeout general
                val result = try {
                    withTimeout(25000) { // 25 segundos máximo
                        repository.quickGoogleAuth(
                            username = googleUser.displayName ?: "Usuario Google",
                            email = googleUser.email
                        )
                    }
                } catch (timeout: TimeoutCancellationException) {
                    _errorMessage.value = "El servidor tardó demasiado en responder. Intenta nuevamente."
                    Log.e("DebugDev", "⏰ Timeout general en Google Sign-In")
                    return@launch
                }

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    user?.let {
                        Log.d("DebugDev", "✅ Autenticación exitosa, guardando sesión para usuario ID: ${it.id}")

                        val success = repository.saveGoogleSession(it.id ?: 0)
                        if (success) {
                            _isLoggedIn.value = true
                            Log.i("DebugDev", "✅ Google Sign-In COMPLETADO - Usuario: ${googleUser.email}")

                            loadUserProfile()
                            //onLoginSuccess(navController) // No va genera blucle infinito
                            // ✅ No navegues desde aquí – solo cambia el estado
                            Log.d("DebugDev", "✅ Login exitoso – esperando navegación desde AccessScreen")
                        } else {
                            _errorMessage.value = "Error al guardar la sesión local"
                            Log.e("DebugDev", "❌ Error al guardar sesión en preferences")
                        }
                    } ?: run {
                        _errorMessage.value = "Error: Datos de usuario incompletos"
                        Log.e("DebugDev", "❌ Usuario nulo en respuesta")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = when {
                        error is TimeoutCancellationException ->
                            "Timeout: El servidor no respondió a tiempo"

                        error?.message?.contains("timeout", true) == true ->
                            "Timeout: El servidor no respondió a tiempo"

                        error?.message?.contains("socket", true) == true ->
                            "Error de conexión. Verifica tu internet"

                        error?.message?.contains("network", true) == true ->
                            "Error de red. Verifica tu conexión a internet"

                        error?.message?.contains("401", true) == true ->
                            "Error de autenticación. Token de Google inválido"

                        error?.message?.contains("400", true) == true ->
                            "Solicitud incorrecta. Verifica los datos"

                        error?.message?.contains("500", true) == true ->
                            "Error del servidor. Intenta más tarde"

                        else -> "Error en Google Sign-In: ${error?.message ?: "Desconocido"}"
                    }
                    _errorMessage.value = errorMessage
                    Log.e("DebugDev", "❌ Error en Google Sign-In: ${error?.message}", error)
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e is TimeoutCancellationException -> "Timeout: Operación muy larga"
                    e.message?.contains("network", true) == true -> "Error de red"
                    e.message?.contains("connection", true) == true -> "Sin conexión"
                    else -> "Error inesperado: ${e.message ?: "Desconocido"}"
                }
                _errorMessage.value = errorMsg
                Log.e("DebugDev", "💥 Excepción en Google Sign-In", e)
            } finally {
                _isLoading.value = false
                Log.d("DebugDev", "🔚 Google Sign-In proceso finalizado")
            }
        }
    }

    // 🔄 Función opcional para cargar perfil de usuario
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("DebugDev", "🔄 Cargando perfil de usuario...")
                // Aquí puedes cargar datos adicionales del usuario si es necesario
            } catch (e: Exception) {
                Log.e("DebugDev", "Error cargando perfil: ${e.message}")
            }
        }
    }

    fun onLoginSuccess(navController: NavController) {
        viewModelScope.launch {
            navController.navigate(Screen.Feed.route) {
                popUpTo(0) { inclusive = true } // ✅ Limpia toda la pila
                launchSingleTop = true
            }
            Log.d("DebugDev", "✅ Navegando a Feed desde AuthViewModel")
        }
    }

    // ✅ Función para logout
    fun logout(onComplete: () -> Unit = {}) {
        Log.d("DebugDev", "🚪 Iniciando logout - limpiando token y preferencias")
        viewModelScope.launch {
            try {
                // 1️⃣ Limpiar token
                tokenProvider.clearToken()
                Log.d("DebugDev", "✅ Token limpiado")
//                biometricEnabledFlow
                // 2️⃣ Limpiar DataStore COMPLETO (incluyendo biometría)
                Log.d("DebugDev", "🧹 Limpiando todas las preferencias del DataStore")
                preferencesManager.clearAll()
                Log.d("DebugDev", "✅ DataStore completamente limpiado")
                
                // 3️⃣ Actualizar estado
                _isLoggedIn.value = false
                Log.d("DebugDev", "✅ Token limpiado, DataStore limpiado")
                Log.i("DebugDev", "✅ Cierre de sesión exitoso")
                
                // 4️⃣ Ejecutar callback
                onComplete()
                Log.d("DebugDev", "🚪 Usuario hizo logout")
            } catch (e: Exception) {
                Log.e("DebugDev", "❌ Error en logout", e)
            }
        }
    }

    // ✅ Función para limpiar estado de autenticación
    fun clearAuthState() {
        _errorMessage.value = null
        Log.d("DebugDev", "🧹 Estado de auth limpiado")
    }

    // ✅ Función para limpiar errores
    fun clearError() {
        _errorMessage.value = null
    }
}