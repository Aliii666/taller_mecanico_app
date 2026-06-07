package com.example.tallermecanico.model

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────
// AUTH
//
// Respuesta real del backend Django:
// POST /api/auth/login/ y /api/auth/register/
// {
//   "access": "...",
//   "refresh": "...",
//   "user": {
//     "id": 1,
//     "username": "...",
//     "email": "...",
//     "role": { "id": 1, "name": "admin" }   ← objeto, NO string
//   }
// }
// ─────────────────────────────────────────────

/**
 * El campo "role" del backend es un objeto { "id": Int, "name": String }.
 * Los valores posibles de "name": "admin" | "mechanic" | "client"
 */
data class RolObject(
    val id: Int? = null,
    val name: String = ""
)

data class Usuario(
    val id: Int,
    val username: String,
    val email: String,
    val role: RolObject          // objeto anidado del backend
) {
    /** Acceso conveniente al nombre del rol como String */
    val roleName: String get() = role.name
}

data class AuthResponse(
    val access: String,
    val refresh: String,
    val user: Usuario
)

// ─────────────────────────────────────────────
// CLIENTES
// ─────────────────────────────────────────────

data class Cliente(
    val id: Int? = null,
    val nombre: String,
    val telefono: String? = "",
    val direccion: String? = "",
    val correo: String? = ""
)

// ─────────────────────────────────────────────
// VEHÍCULOS
// ─────────────────────────────────────────────

data class Vehiculo(
    val id: Int? = null,
    val cliente: Int,
    val marca: String,
    val modelo: String,
    val placa: String,
    val anio: Int,
    @SerializedName("cliente_detalle")
    val clienteDetalle: Cliente? = null
)

// ─────────────────────────────────────────────
// SERVICIOS
// ─────────────────────────────────────────────

data class Servicio(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String,
    val precio: Double
)

// ─────────────────────────────────────────────
// ÓRDENES DE TRABAJO
// ─────────────────────────────────────────────

data class OrdenTrabajo(
    val id: Int? = null,
    val vehiculo: Int,
    val mecanico: Int? = null,
    @SerializedName("fecha_ingreso")
    val fechaIngreso: String,
    val estado: String,                   // "pendiente" | "en_proceso" | "terminado"
    val observaciones: String? = null,
    @SerializedName("vehiculo_detalle")
    val vehiculoDetalle: Vehiculo? = null,
    @SerializedName("mecanico_detalle")
    val mecanicoDetalle: Usuario? = null
)

// ─────────────────────────────────────────────
// FACTURAS
// ─────────────────────────────────────────────

data class Factura(
    val id: Int? = null,
    val orden: Int,
    val total: Double,
    @SerializedName("fecha_emision")
    val fechaEmision: String,
    @SerializedName("estado_pago")
    val estadoPago: String,               // "pendiente" | "pagado"
    @SerializedName("orden_detalle")
    val ordenDetalle: OrdenTrabajo? = null
)

// ─────────────────────────────────────────────
// PAGOS
// ─────────────────────────────────────────────

data class Pago(
    val id: Int? = null,
    val factura: Int,
    @SerializedName("metodo_pago")
    val metodoPago: String,               // "efectivo" | "tarjeta" | "transferencia"
    val monto: Double,
    @SerializedName("fecha_pago")
    val fechaPago: String? = null
)

// Respuesta especial del POST /api/pagos/ cuando la factura queda saldada
data class PagoResponse(
    val id: Int? = null,
    val factura: Int? = null,
    @SerializedName("metodo_pago")
    val metodoPago: String? = null,
    val monto: Double? = null,
    @SerializedName("fecha_pago")
    val fechaPago: String? = null,
    @SerializedName("factura_saldada")
    val facturaSaldada: Boolean? = null   // true cuando el pago cubre el total
)

data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
