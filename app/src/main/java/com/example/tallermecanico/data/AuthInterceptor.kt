package com.example.tallermecanico.data

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * OkHttp Interceptor con dos responsabilidades:
 *
 * 1. Adjunta "Authorization: Bearer <access>" a cada petición autenticada.
 * 2. Si el servidor devuelve 401 (token expirado), intenta renovarlo de forma
 *    transparente usando el refresh token, guarda el nuevo access en DataStore
 *    y reintenta la petición original — sin que el usuario se dé cuenta.
 *
 * El bloque synchronized evita condiciones de carrera cuando múltiples
 * corutinas lanzan peticiones simultáneas con el token expirado.
 */
class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    // Cliente OkHttp limpio (sin este interceptor) exclusivo para el refresh.
    // Evita el bucle infinito: refresh → 401 → refresh → 401...
    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sessionManager.getAccessToken()

        // ── 1. Añadir token a la petición saliente ────────────────────────
        val originalRequest = chain.request()
        val authenticatedRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // ── 2. Si el server responde 401, intentar renovar el token ──────
        if (response.code == 401) {
            synchronized(this) {
                // Leer el token actual DENTRO del bloque sincronizado.
                // Si otro hilo ya lo renovó mientras esperábamos, usarlo directamente.
                val currentToken = sessionManager.getAccessToken()
                if (currentToken != accessToken && !currentToken.isNullOrBlank()) {
                    response.close()
                    return chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                    )
                }

                val refreshToken = sessionManager.getRefreshToken()
                if (refreshToken.isNullOrBlank()) {
                    // Sin refresh token: forzar logout
                    runBlocking { sessionManager.clearSession() }
                    return response
                }

                // Construir un Retrofit temporal con el cliente limpio
                val refreshRetrofit = Retrofit.Builder()
                    .baseUrl(RetrofitInstance.BASE_URL)
                    .client(refreshClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val refreshService = refreshRetrofit.create(RefreshService::class.java)

                val refreshResponse = try {
                    refreshService
                        .refreshToken(mapOf("refresh" to refreshToken))
                        .execute()
                } catch (e: Exception) {
                    // Red caída durante el refresh: devolver la respuesta 401 original
                    return response
                }

                return if (refreshResponse.isSuccessful) {
                    val newAccess = refreshResponse.body()?.get("access") as? String
                    if (!newAccess.isNullOrBlank()) {
                        // Guardar nuevo access conservando refresh y rol actuales
                        runBlocking {
                            sessionManager.updateAccessToken(newAccess)
                        }
                        response.close()
                        // Reintentar petición original con el nuevo token
                        chain.proceed(
                            originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newAccess")
                                .build()
                        )
                    } else {
                        runBlocking { sessionManager.clearSession() }
                        response
                    }
                } else {
                    // Refresh falló (token revocado, expirado) → forzar logout
                    runBlocking { sessionManager.clearSession() }
                    response
                }
            }
        }

        return response
    }
}
