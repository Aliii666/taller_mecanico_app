package com.example.tallermecanico.repository

import com.example.tallermecanico.data.NetworkUtils
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.model.PaginatedResponse
import com.example.tallermecanico.model.Servicio

class ServicioRepository {

    private val api = RetrofitInstance.api

    suspend fun getServicios(page: Int? = null, search: String? = null): Result<PaginatedResponse<Servicio>> {
        return NetworkUtils.safeApiCall {
            api.getServicios(page = page, search = search)
        }
    }

    suspend fun crearServicio(nombre: String, descripcion: String, precio: Double): Result<Servicio> {
        val servicio = Servicio(nombre = nombre, descripcion = descripcion, precio = precio)
        return NetworkUtils.safeApiCall {
            api.crearServicio(servicio)
        }
    }

    suspend fun actualizarServicio(id: Int, nombre: String, descripcion: String, precio: Double): Result<Servicio> {
        val servicio = Servicio(id = id, nombre = nombre, descripcion = descripcion, precio = precio)
        return NetworkUtils.safeApiCall {
            api.actualizarServicio(id, servicio)
        }
    }

    suspend fun eliminarServicio(id: Int): Result<Unit> {
        return NetworkUtils.safeApiCall {
            api.eliminarServicio(id)
        }
    }
}
