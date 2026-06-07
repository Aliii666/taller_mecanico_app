package com.example.tallermecanico.data

import com.example.tallermecanico.repository.Result
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

object NetworkUtils {

    /**
     * Ejecuta una llamada de red de forma segura mapeando los errores HTTP y excepciones a Result.
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    // Si es exitoso pero vacío (ej. 204 No Content), devolvemos Success con Unit o casteado
                    @Suppress("UNCHECKED_CAST")
                    Result.Success(Unit as T)
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                val message = mapHttpError(errorCode, errorBody)
                Result.Error(message)
            }
        } catch (e: SocketTimeoutException) {
            Result.Error("Tiempo de espera agotado. El servidor tardó demasiado en responder.")
        } catch (e: IOException) {
            Result.Error("Error de conexión: Por favor, verifique su conexión a internet.")
        } catch (e: Exception) {
            Result.Error("Ocurrió un error inesperado: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Mapea códigos de estado HTTP a mensajes amigables y legibles en español.
     */
    private fun mapHttpError(code: Int, errorBody: String?): String {
        return when (code) {
            400 -> {
                // Intenta parsear los mensajes de error específicos de Django
                parseDjangoErrors(errorBody) ?: "Datos inválidos: Verifique los campos ingresados."
            }
            401 -> "Sesión no autorizada o token vencido. Por favor, inicie sesión nuevamente."
            403 -> "Acceso denegado: No tiene permisos suficientes para realizar esta acción."
            404 -> "El recurso solicitado no fue encontrado en el servidor (404)."
            500 -> "Error interno del servidor (500). Por favor, inténtelo más tarde."
            else -> "Error del servidor ($code): ${errorBody ?: "Error desconocido"}"
        }
    }

    /**
     * Parsea respuestas de error típicas de Django REST Framework:
     * - {"detail": "mensaje"}
     * - {"campo": ["error1", "error2"]}
     */
    private fun parseDjangoErrors(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorBody)
            if (json.has("detail")) {
                return json.getString("detail")
            }
            if (json.has("error")) {
                return json.getString("error")
            }
            // Si es un objeto de errores de validación de campos, extrae el primero
            val keys = json.keys()
            if (keys.hasNext()) {
                val firstKey = keys.next()
                val array = json.optJSONArray(firstKey)
                if (array != null && array.length() > 0) {
                    val fieldName = firstKey.replaceFirstChar { it.uppercase() }
                    return "$fieldName: ${array.getString(0)}"
                }
                val msg = json.optString(firstKey)
                if (msg.isNotBlank()) {
                    return "${firstKey.replaceFirstChar { it.uppercase() }}: $msg"
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
