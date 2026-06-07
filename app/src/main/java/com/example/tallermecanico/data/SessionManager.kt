package com.example.tallermecanico.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "taller_session")

/**
 * Gestiona la sesión del usuario usando DataStore Preferences.
 *
 * Lectura síncrona (getAccessToken / getRefreshToken) → usada por AuthInterceptor
 * en el hilo de OkHttp donde no se puede lanzar una corutina.
 *
 * Lectura reactiva (Flows) → usada por ViewModels para reaccionar a cambios.
 */
class SessionManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_ACCESS   = stringPreferencesKey("access_token")
        val KEY_REFRESH  = stringPreferencesKey("refresh_token")
        val KEY_ROLE     = stringPreferencesKey("user_role")
        val KEY_USER_ID  = stringPreferencesKey("user_id")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_EMAIL    = stringPreferencesKey("email")
    }

    // ── Flows reactivos (para ViewModels / Compose) ───────────────────────────

    val accessTokenFlow: Flow<String?> = dataStore.data.map { it[KEY_ACCESS] }
    val roleFlow: Flow<String?>        = dataStore.data.map { it[KEY_ROLE] }
    val isLoggedInFlow: Flow<Boolean>  = dataStore.data.map { !it[KEY_ACCESS].isNullOrBlank() }

    // ── Lectura síncrona (para AuthInterceptor en hilo OkHttp) ───────────────

    fun getAccessToken(): String? = runBlocking {
        dataStore.data.first()[KEY_ACCESS]
    }

    fun getRefreshToken(): String? = runBlocking {
        dataStore.data.first()[KEY_REFRESH]
    }

    fun getRole(): String? = runBlocking {
        dataStore.data.first()[KEY_ROLE]
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /** Guarda toda la sesión tras un login o registro exitoso. */
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: Int,
        username: String,
        email: String,
        role: String
    ) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS]   = accessToken
            prefs[KEY_REFRESH]  = refreshToken
            prefs[KEY_USER_ID]  = userId.toString()
            prefs[KEY_USERNAME] = username
            prefs[KEY_EMAIL]    = email
            prefs[KEY_ROLE]     = role
        }
    }

    /**
     * Actualiza solo el access token después de un refresh exitoso.
     * El refresh token y el rol no cambian, no hace falta tocarlos.
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS] = newAccessToken
        }
    }

    /** Elimina todos los datos al cerrar sesión. */
    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
