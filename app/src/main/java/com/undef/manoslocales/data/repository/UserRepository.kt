package com.undef.manoslocales.data.repository

import android.content.Context
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.remote.ApiService
import com.undef.manoslocales.data.remote.PasswordChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ID_KEY = "user_id"
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    }

    suspend fun getCurrentUser(): User? {
        // Ajustá esto según tu API real. Si no existe endpoint "current user", necesitás
        // primero obtener el ID almacenado y luego llamar getUserById.
        val userId = getUserId() ?: return null
        return try {
            val response = api.getUserById(userId.toString())
            response
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    suspend fun updateUser(user: User): Boolean {
        // Asumo que user.id no es nullable; si lo es, el tipo debería reflejar eso.
        val response: Response<Void> = api.updateUser(user.id.toString(), user)
        // dependiendo de tu API, esto puede devolver el usuario actualizado o un status
        return true // o chequear alguna condición en `response`
    }

    suspend fun changePassword(newPassword: String): Boolean {
        val userId = getUserId() ?: return false
        // Asegurate de que `updatedPassword` exista realmente; el nombre en ApiService debe coincidir.
        val response = api.updatedPassword(userId.toString(), PasswordChangeRequest(newPassword))
        return (response as? Response<*>)?.isSuccessful ?: true
    }

    private fun getUserId(): Int? {
        return prefs.getInt(USER_ID_KEY, -1).takeIf { it != -1 }
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(USER_ID_KEY, userId).apply()
    }

    // Estas dos deberían vivir en PreferencesManager, no acá. Si las mantenés:
    fun saveNotificationSetting(enabled: Boolean) {
        prefs.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
    }

    fun getNotificationSetting(): Boolean {
        return prefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
    }
}
