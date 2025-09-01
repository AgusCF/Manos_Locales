package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.local.AuthTokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenProvider: AuthTokenProvider
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoggedIn.value = !tokenProvider.getToken().isNullOrBlank()
        }
    }

    fun logout() {
        tokenProvider.clearAll()
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }

    fun clearAuthState() {
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }
}