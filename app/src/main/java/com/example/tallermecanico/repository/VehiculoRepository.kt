package com.example.tallermecanico.repository

import com.example.tallermecanico.data.NetworkUtils
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.model.PaginatedResponse
import com.example.tallermecanico.model.Vehiculo

class VehiculoRepository {

    private val api = RetrofitInstance.api

    suspend fun getVehiculos(page: Int? = null, search: String? = null): Result<PaginatedResponse<Vehiculo>> {
        return NetworkUtils.safeApiCall {
            api.getVehiculos(page = page, search = search)
        }
    }

    suspend fun crearVehiculo(
        clienteId: Int, marca: String, modelo: String, placa: String, anio: Int
    ): Result<Vehiculo> {
        val body = mapOf<String, Any>(
            "cliente" to clienteId,
            "marca"   to marca,
            "modelo"  to modelo,
            "placa"   to placa,
            "anio"    to anio
        )
        return NetworkUtils.safeApiCall {
            api.crearVehiculo(body)
        }
    }

    suspend fun actualizarVehiculo(
        id: Int, clienteId: Int, marca: String, modelo: String, placa: String, anio: Int
    ): Result<Vehiculo> {
        val body = mapOf<String, Any>(
            "cliente" to clienteId,
            "marca"   to marca,
            "modelo"  to modelo,
            "placa"   to placa,
            "anio"    to anio
        )
        return NetworkUtils.safeApiCall {
            api.actualizarVehiculo(id, body)
        }
    }

    suspend fun eliminarVehiculo(id: Int): Result<Unit> {
        return NetworkUtils.safeApiCall {
            api.eliminarVehiculo(id)
        }
    }
}
