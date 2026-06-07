package com.example.tallermecanico.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interfaz exclusiva para el token refresh.
 * Usa Call síncrono (no suspend) para ejecutarse dentro del interceptor OkHttp.
 *
 * POST /api/auth/token/refresh/
 * Body:    { "refresh": "<refresh_token>" }
 * Returns: { "access": "<nuevo_access_token>" }
 */
interface RefreshService {
    @POST("api/auth/token/refresh/")
    fun refreshToken(
        @Body body: Map<String, String>
    ): Call<Map<String, Any>>
}
