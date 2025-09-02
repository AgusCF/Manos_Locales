package com.undef.manoslocales.viewmodel

import android.util.Log
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
    val repository: UserRepository,
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

    private val _changePasswordResult = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordResult: StateFlow<Result<Unit>?> = _changePasswordResult.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = preferencesManager.notificationsEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Control de carga para no repetir innecesariamente
    private var userLoaded = false
    private var attemptedWithoutToken = false

    /**
     * Asegura que el usuario se cargue solo una vez si hay token válido.
     */
    fun ensureUserLoaded() {
        if (userLoaded) return
        val token = repository.getToken()
        val userId = repository.getUserId()
        Log.i("DebugDev", "ensureUserLoaded: token=$token userId=$userId")
        if (token.isNullOrBlank() || userId == null) {
            if (!attemptedWithoutToken) {
                Log.i("DebugDev", "No hay token o userId, no se carga usuario todavía")
                attemptedWithoutToken = true
            }
            return
        }
        loadCurrentUserInternal()
    }

    private fun loadCurrentUserInternal() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fetched = repository.getCurrentUser()
                Log.i("DebugDev", "loadCurrentUserInternal: fetched=$fetched")
                if (fetched != null) {
                    _user.value = fetched
                    userLoaded = true
                } else {
                    _errorMessage.value = "No se pudo obtener el usuario"
                    Log.w("DebugDev", "No se pudo obtener el usuario (fetched=null)")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el usuario: ${e.localizedMessage ?: "desconocido"}"
                Log.e("DebugDev", "Exception loading current user", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fuerza recarga (por ejemplo tras login).
     */
    fun refreshUserIfLoggedIn() {
        userLoaded = false
        attemptedWithoutToken = false
        ensureUserLoaded()
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.logout()
                userLoaded = false
                attemptedWithoutToken = false
                _user.value = null
                _logoutSuccess.value = true
                Log.i("DebugDev", "Cierre de sesión exitoso")
            } catch (e: Exception) {
                _errorMessage.value = "Error al cerrar sesión: ${e.localizedMessage ?: "desconocido"}"
                _logoutSuccess.value = false
                Log.e("DebugDev", "Error en logout", e)
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
                    Log.e("DebugDev", "updateUser devolvió false para $updatedUser")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar usuario: ${e.localizedMessage ?: "desconocido"}"
                Log.e("DebugDev", "Exception en updateUser", e)
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

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val success = repository.changePassword(newPassword)
                if (success) {
                    _changePasswordResult.value = Result.success(Unit)
                } else {
                    _changePasswordResult.value = Result.failure(Exception("Cambio fallido"))
                    _errorMessage.value = "Error al cambiar contraseña"
                }
            } catch (e: Exception) {
                _changePasswordResult.value = Result.failure(e)
                _errorMessage.value =
                    "Error al cambiar contraseña: ${e.localizedMessage ?: "desconocido"}"
                Log.e("DebugDev", "Exception changing password", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearLogoutFlag() {
        _logoutSuccess.value = false
    }

    fun clearChangePasswordResult() {
        _changePasswordResult.value = null
    }
}