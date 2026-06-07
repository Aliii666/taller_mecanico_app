package com.example.tallermecanico.repository

import com.example.tallermecanico.data.NetworkUtils
import com.example.tallermecanico.data.RetrofitInstance
import com.example.tallermecanico.model.Factura
import com.example.tallermecanico.model.Pago
import com.example.tallermecanico.model.PagoResponse
import com.example.tallermecanico.model.PaginatedResponse

class FacturaPagoRepository {

    private val api = RetrofitInstance.api

    // ── Facturas ──────────────────────────────────────────────────────────────

    suspend fun getFacturas(
        estadoPago: String? = null,
        page: Int? = null,
        search: String? = null
    ): Result<PaginatedResponse<Factura>> {
        return NetworkUtils.safeApiCall {
            api.getFacturas(estadoPago = estadoPago, page = page, search = search)
        }
    }

    suspend fun crearFactura(ordenId: Int, total: Double): Result<Factura> {
        val body = mapOf<String, Any>(
            "orden" to ordenId,
            "total" to total
        )
        return NetworkUtils.safeApiCall {
            api.crearFactura(body)
        }
    }

    suspend fun marcarPagada(id: Int): Result<Factura> {
        return NetworkUtils.safeApiCall {
            api.marcarFacturaPagada(id)
        }
    }

    // ── Pagos ─────────────────────────────────────────────────────────────────

    suspend fun getPagos(): Result<List<Pago>> {
        // Los pagos no están paginados en este caso (o no es requerido explícitamente), pero usamos safeApiCall
        return NetworkUtils.safeApiCall {
            api.getPagos()
        }
    }

    suspend fun registrarPago(
        facturaId: Int,
        monto: Double,
        metodoPago: String
    ): Result<PagoResponse> {
        if (monto <= 0) return Result.Error("El monto debe ser mayor a 0")
        val body = mapOf<String, Any>(
            "factura"     to facturaId,
            "monto"       to monto,
            "metodo_pago" to metodoPago
        )
        return NetworkUtils.safeApiCall {
            api.registrarPago(body)
        }
    }
}
