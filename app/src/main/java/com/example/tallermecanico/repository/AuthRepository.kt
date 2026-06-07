package com.example.tallermecanico.repository

import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.data.SessionManager
import com.example.tallermecanico.model.AuthResponse
import com.example.tallermecanico.model.Usuario

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class AuthRepository(private val sessionManager: SessionManager) {

    private val api = RetrofitInstance.api

    // ── Login ─────────────────────────────────────────────────────────────────
    // POST /api/auth/login/  →  { access, refresh, user: { id, username, email, role } }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(mapOf("email" to email, "password" to password))
            if (response.isSuccessful) {
                val body = response.body()!!
                sessionManager.saveSession(
                    accessToken  = body.access,
                    refreshToken = body.refresh,
                    userId       = body.user.id,
                    username     = body.user.username,
                    email        = body.user.email,
                    role         = body.user.roleName   // extrae el string del objeto RolObject
                )
                Result.Success(body)
            } else {
                val msg = response.errorBody()?.string()
                Result.Error(parseError(response.code(), msg))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }

    // ── Registro ──────────────────────────────────────────────────────────────
    // POST /api/auth/register/  →  { access, refresh, user }
    // El backend asigna role "client" por defecto desde este endpoint.

    suspend fun registro(
        username: String,
        email: String,
        password: String,
        password2: String
    ): Result<AuthResponse> {
        return try {
            val body = mapOf(
                "username"  to username,
                "email"     to email,
                "password"  to password,
                "password2" to password2
            )
            val response = api.register(body)
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                sessionManager.saveSession(
                    accessToken  = authResponse.access,
                    refreshToken = authResponse.refresh,
                    userId       = authResponse.user.id,
                    username     = authResponse.user.username,
                    email        = authResponse.user.email,
                    role         = authResponse.user.roleName   // extrae el string del objeto RolObject
                )
                Result.Success(authResponse)
            } else {
                val errBody = response.errorBody()?.string() ?: ""
                Result.Error(parseError(response.code(), errBody))
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }

    // ── Perfil ────────────────────────────────────────────────────────────────
    // GET /api/auth/me/

    suspend fun getMe(): Result<Usuario> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("No autorizado (${response.code()})")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de red")
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    suspend fun logout() = sessionManager.clearSession()

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun parseError(code: Int, body: String?): String {
        return when (code) {
            400  -> "Datos inválidos: ${body ?: ""}"
            401  -> "Credenciales incorrectas"
            403  -> "Sin permisos para esta acción"
            404  -> "Recurso no encontrado"
            else -> "Error del servidor ($code)"
        }
    }
}
