package com.undef.manoslocales.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.remote.ApiService
import kotlinx.coroutines.launch

class UserViewModel(private val apiService: ApiService) : ViewModel() {
    var currentUser by mutableStateOf<User?>(null)
        private set

    var loginError by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = apiService.login(User( username = "",email = email, password = password,tel = ""))//Mala practica
                currentUser = user
                loginError = null
            } catch (e: Exception) {
                loginError = "Email o contraseña incorrecta"
                currentUser = null
            }
        }
    }
}
