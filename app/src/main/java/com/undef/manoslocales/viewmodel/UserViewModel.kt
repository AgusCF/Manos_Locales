package com.undef.manoslocales.viewmodel

import android.app.Application
import android.content.Context
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
import androidx.lifecycle.AndroidViewModel
import com.undef.manoslocales.data.remote.RetrofitInstance.api


class UserViewModel(application: Application) : AndroidViewModel(application) {

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
            val user = User(username = username, email = email, password = password)
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

            val credentials = mapOf("email" to email, "password" to password)
            try {
                val response = api.loginUser(credentials)
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("auth_token", token).apply()
                        _loginSuccess.value = true
                    } else {
                        _errorMessage.value = "Token no recibido"
                        _loginSuccess.value = false
                    }
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

    private fun saveTokenToPreferences(token: String) {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun getTokenFromPreferences(): String? {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

}
