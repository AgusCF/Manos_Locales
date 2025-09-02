package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.local.AuthTokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenProvider: AuthTokenProvider
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                delay(500)
                val token = tokenProvider.getToken()
                _isLoggedIn.value = !token.isNullOrBlank()
            } finally {
                _isInitialized.value = true
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenProvider.clearAll()
            _isLoggedIn.value = false
        }
    }

    fun clearAuthState() {
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }
}