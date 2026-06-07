package com.example.tallermecanico.repository

import com.example.tallermecanico.data.NetworkUtils
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.model.Cliente
import com.example.tallermecanico.model.PaginatedResponse

class ClienteRepository {

    private val api = RetrofitInstance.api

    suspend fun getClientes(page: Int? = null, search: String? = null): Result<PaginatedResponse<Cliente>> {
        return NetworkUtils.safeApiCall {
            api.getClientes(page = page, search = search)
        }
    }

    suspend fun crearCliente(cliente: Cliente): Result<Cliente> {
        return NetworkUtils.safeApiCall {
            api.crearCliente(cliente)
        }
    }

    suspend fun getCliente(id: Int): Result<Cliente> {
        return NetworkUtils.safeApiCall {
            api.getCliente(id)
        }
    }

    suspend fun actualizarCliente(id: Int, cliente: Cliente): Result<Cliente> {
        return NetworkUtils.safeApiCall {
            api.actualizarCliente(id, cliente)
        }
    }

    suspend fun eliminarCliente(id: Int): Result<Unit> {
        return NetworkUtils.safeApiCall {
            api.eliminarCliente(id)
        }
    }
}
