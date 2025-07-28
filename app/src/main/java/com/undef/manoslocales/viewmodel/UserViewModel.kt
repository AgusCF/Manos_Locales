package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log


class UserViewModel : ViewModel() {

    private val _loginSuccess = MutableStateFlow<Boolean?>(null)
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _registrationSuccess = MutableStateFlow<Boolean?>(null)
    val registrationSuccess: StateFlow<Boolean?> = _registrationSuccess

    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        return try {
            val user = User(username = username, email = email, password = password, tel = "")
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.newUser(user)
            }
            if (response.isSuccessful) {
                Pair(true, null)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Pair(false, "Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            Pair(false, "Error al registrar usuario: ${e.localizedMessage ?: e.message}")
        }
    }
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = User(username = "", email = email, password = password, tel = "")
                val response = RetrofitInstance.api.loginUser(user)

                // Imprimir código y body para debug
                println("Código de respuesta: ${response.code()}")
                println("Body de la respuesta: ${response.body()}")
                // Dentro de tu función
                Log.d("UserViewModel", "Código de respuesta: ${response.code()}")
                Log.d("UserViewModel", "Body de la respuesta: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    _loginSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _errorMessage.value = "Error del servidor: $errorBody"
                    _loginSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar sesión: ${e.localizedMessage ?: e.message}"
                _loginSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
}
