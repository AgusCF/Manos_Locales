package com.undef.manoslocales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.manoslocales.data.local.preference.PreferencesManager
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess: StateFlow<Boolean> = _logoutSuccess.asStateFlow()

    // Resultado del cambio de contraseña (success / failure message)
    private val _changePasswordResult = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordResult: StateFlow<Result<Unit>?> = _changePasswordResult.asStateFlow()

    // Estado de notificaciones leído de PreferencesManager
    val notificationsEnabled: StateFlow<Boolean> = preferencesManager.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _user.value = repository.getCurrentUser()
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el usuario: ${e.localizedMessage ?: "desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.logout()
                _logoutSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage ?: "desconocido"}"
                _logoutSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = repository.updateUser(updatedUser)
                if (success) {
                    _user.value = updatedUser
                } else {
                    _errorMessage.value = "Error al actualizar el usuario"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar usuario: ${e.localizedMessage ?: "desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    suspend fun changePassword(newPassword: String): Boolean {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            val success = repository.changePassword(newPassword)
            if (success) {
                _changePasswordResult.value = Result.success(Unit)
            } else {
                _changePasswordResult.value = Result.failure(Exception("Cambio fallido"))
                _errorMessage.value = "Error al cambiar contraseña"
            }
            success
        } catch (e: Exception) {
            _changePasswordResult.value = Result.failure(e)
            _errorMessage.value = "Error al cambiar contraseña: ${e.localizedMessage ?: "desconocido"}"
            false
        } finally {
            _isLoading.value = false
        }
    }
}