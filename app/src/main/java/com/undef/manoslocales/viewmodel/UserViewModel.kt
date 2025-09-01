package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.repository.DebugDev
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: DebugDev
) : ViewModel() {

    private val _loginSuccess = MutableStateFlow<Boolean?>(null)
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _registrationSuccess = MutableStateFlow<Boolean?>(null)
    val registrationSuccess: StateFlow<Boolean?> = _registrationSuccess

    fun loginUser(email: String, password: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val (success, message) = repository.loginUser(email, password)
            if (success) {
                _loginSuccess.value = true
                onSuccess?.invoke() // para que se pueda llamar loadCurrentUser() desde fuera
            } else {
                _errorMessage.value = message
                _loginSuccess.value = false
            }
            _isLoading.value = false
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        return repository.registerUser(username, email, password)
    }

    fun clearLoginResult() {
        _loginSuccess.value = null
    }
}