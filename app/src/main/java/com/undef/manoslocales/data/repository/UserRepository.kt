package com.undef.manoslocales.data.repository

import android.content.Context
import android.util.Log
import com.undef.manoslocales.data.local.AuthTokenProvider
import com.undef.manoslocales.data.model.User
import com.undef.manoslocales.data.model.LoginResponse
import com.undef.manoslocales.data.remote.ApiService
import com.undef.manoslocales.data.remote.PasswordChangeRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException
import retrofit2.Response

@Singleton
class DebugDev @Inject constructor(
    private val api: ApiService,
    private val tokenProvider: AuthTokenProvider,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    }

    suspend fun loginUser(email: String, password: String): Pair<Boolean, String?> {
        Log.i("DebugDev", "Iniciando login para email=$email")
        return try {
            val credentials = mapOf("email" to email, "password" to password)
            val response: Response<LoginResponse> = api.loginUser(credentials)
            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.token
                if (!token.isNullOrBlank()) {
                    tokenProvider.saveToken(token)
                    // intentar obtener userId por email del backend
                    val user = try {
                        api.getUserByMail(email)
                    } catch (e: Exception) {
                        Log.w("DebugDev", "No se pudo obtener usuario por email", e)
                        null
                    }
                    val userId = user?.id
                    if (userId != null) {
                        tokenProvider.saveUserId(userId)
                        Log.i("DebugDev", "Login successful, token e id guardados (id=$userId)")
                        Pair(true, null)
                    } else {
                        Log.w("DebugDev", "Login exitoso pero no se obtuvo userId")
                        Pair(true, null) // aún permitimos login pero sin userId
                    }
                } else {
                    Log.w("DebugDev", "Login fallido: token no recibido")
                    Pair(false, "Token no recibido")
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("DebugDev", "Login fallido: error del servidor: $errorBody")
                Pair(false, "Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception durante login", e)
            Pair(false, "Error al iniciar sesión: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Pair<Boolean, String?> {
        return try {
            val user = User(username = username, email = email, password = password)
            val response = api.newUser(user)
            if (response.isSuccessful) {
                Pair(true, null)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Pair(false, "Error del servidor: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception during register", e)
            Pair(false, "Error al registrar usuario: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun getCurrentUser(): User? {
        val userId = tokenProvider.getUserId()
        if (userId == null) {
            Log.i("DebugDev", "No hay userId, no se puede obtener el usuario")
            return null
        }

        Log.i("DebugDev", "Intentando obtener usuario por ID: $userId")
        return try {
            val user = api.getUserById(userId)
            Log.i("DebugDev", "Usuario obtenido: $user")
            user
        } catch (e: HttpException) {
            Log.e("DebugDev", "getCurrentUser failed: ${e.code()} ${e.response()?.errorBody()?.string()}", e)
            null
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception fetching current user by id", e)
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        val id = user.id ?: run {
            Log.w("DebugDev", "No se puede actualizar usuario sin id")
            return false
        }
        return try {
            val response: Response<Void> = api.updateUser(id.toString(), user)
            if (!response.isSuccessful) {
                Log.w("DebugDev", "updateUser no fue exitoso: ${response.code()} ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception updating user", e)
            false
        }
    }

    suspend fun changePassword(newPassword: String): Boolean {
        val userId = tokenProvider.getUserId()
        if (userId == null) {
            Log.w("DebugDev", "No hay userId para cambiar contraseña")
            return false
        }
        return try {
            val request = PasswordChangeRequest(password = newPassword)
            val response = api.updatedPassword(userId.toString(), request)
            if (!response.isSuccessful) {
                Log.w(
                    "DebugDev",
                    "changePassword no fue exitoso: ${response.code()} ${response.errorBody()?.string()}"
                )
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception changing password", e)
            false
        }
    }

    fun logout() {
        tokenProvider.clearAll()
    }

    suspend fun clearUser() {
        // Lógica para borrar datos del usuario, por ejemplo, de una base de datos local
    }

    fun getToken(): String? = tokenProvider.getToken()
    fun getUserId(): Int? = tokenProvider.getUserId()

    suspend fun getUserById(id: Int?): User? {
        if (id == null) return null
        return try {
            api.getUserById(id)
        } catch (e: Exception) {
            Log.e("DebugDev", "Exception fetching user by id", e)
            null
        }
    }

    fun saveNotificationSetting(enabled: Boolean) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
    }

    fun getNotificationSetting(): Boolean {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
    }
}