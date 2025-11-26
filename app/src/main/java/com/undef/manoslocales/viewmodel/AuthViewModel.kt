package com.undef.manoslocales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.local.AuthTokenProvider
import com.undef.manoslocales.data.model.GoogleUser
import com.undef.manoslocales.data.repository.UserRepository
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
    private val tokenProvider: AuthTokenProvider
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
            val token = tokenProvider.getToken()
            val userId = tokenProvider.getUserId()
            _isLoggedIn.value = token != null && userId != null
            _isInitialized.value = true
            Log.d("DebugDev", "🔍 Auth status - LoggedIn: ${_isLoggedIn.value}")
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
    fun signInWithGoogle(googleUser: GoogleUser) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d("DebugDev", "🎯 Iniciando Google Sign-In...")

                // Agregar timeout general
                val result = try {
                    withTimeout(30000) { // ✅ 30 segundos máximo
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
                        val success = repository.saveGoogleSession(it.id ?: 0)
                        if (success) {
                            _isLoggedIn.value = true
                            Log.d("DebugDev", "✅ Google Sign-In COMPLETADO")
                        } else {
                            _errorMessage.value = "Error al guardar sesión"
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = when {
                        error?.message?.contains("timeout", true) == true ->
                            "Timeout: El servidor no respondió a tiempo"
                        error?.message?.contains("socket", true) == true ->
                            "Error de conexión. Verifica tu internet"
                        else -> "Error en Google Sign-In: ${error?.message ?: "Desconocido"}"
                    }
                    _errorMessage.value = errorMessage
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                Log.e("DebugDev", "💥 Excepción en Google Sign-In", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Función para logout
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _isLoggedIn.value = false
            _errorMessage.value = null
            Log.d("DebugDev", "🚪 Usuario hizo logout")
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