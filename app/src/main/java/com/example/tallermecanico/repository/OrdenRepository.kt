package com.example.tallermecanico.repository

import com.example.tallermecanico.data.NetworkUtils
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.model.OrdenTrabajo
import com.example.tallermecanico.model.PaginatedResponse

class OrdenRepository {

    private val api = RetrofitInstance.api

    suspend fun getOrdenes(
        estado: String? = null,
        page: Int? = null,
        search: String? = null
    ): Result<PaginatedResponse<OrdenTrabajo>> {
        return NetworkUtils.safeApiCall {
            api.getOrdenes(estado = estado, page = page, search = search)
        }
    }

    suspend fun getOrden(id: Int): Result<OrdenTrabajo> {
        return NetworkUtils.safeApiCall {
            api.getOrden(id)
        }
    }

    suspend fun crearOrden(
        vehiculoId: Int,
        mecanicoId: Int?,
        observaciones: String
    ): Result<OrdenTrabajo> {
        val body = mutableMapOf<String, Any?>(
            "vehiculo"      to vehiculoId,
            "observaciones" to observaciones
        )
        if (mecanicoId != null) body["mecanico"] = mecanicoId
        return NetworkUtils.safeApiCall {
            api.crearOrden(body)
        }
    }

    /**
     * Llama al endpoint custom PUT /api/ordenes/{id}/estado/
     * @param estado  "pendiente" | "en_proceso" | "terminado"
     */
    suspend fun cambiarEstado(
        id: Int,
        estado: String,
        observaciones: String = ""
    ): Result<OrdenTrabajo> {
        val body = mapOf("estado" to estado, "observaciones" to observaciones)
        return NetworkUtils.safeApiCall {
            api.cambiarEstadoOrden(id, body)
        }
    }

    suspend fun eliminarOrden(id: Int): Result<Unit> {
        return NetworkUtils.safeApiCall {
            api.eliminarOrden(id)
        }
    }
}
