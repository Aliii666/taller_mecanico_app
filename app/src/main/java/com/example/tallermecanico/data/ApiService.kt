package com.example.tallermecanico.data

import com.example.tallermecanico.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH  —  base: /api/auth/
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/auth/register/
     * Body: { "username", "email", "password", "password2" }
     * Nota: el backend crea cuentas con role "client" por defecto desde este endpoint.
     */
    @POST("api/auth/register/")
    suspend fun register(
        @Body request: Map<String, String>
    ): Response<AuthResponse>

    /**
     * POST /api/auth/login/
     * Body: { "email": "...", "password": "..." }
     * Returns: { "access", "refresh", "user": { id, username, email, role } }
     */
    @POST("api/auth/login/")
    suspend fun login(
        @Body request: Map<String, String>
    ): Response<AuthResponse>

    /**
     * GET /api/auth/me/
     * Returns current authenticated user.
     */
    @GET("api/auth/me/")
    suspend fun getMe(): Response<Usuario>

    // ─────────────────────────────────────────────────────────────────────────
    // CLIENTES  —  /api/clientes/
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/clientes/")
    suspend fun getClientes(
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<Cliente>>

    @POST("api/clientes/")
    suspend fun crearCliente(
        @Body cliente: Cliente
    ): Response<Cliente>

    @GET("api/clientes/{id}/")
    suspend fun getCliente(
        @Path("id") id: Int
    ): Response<Cliente>

    @PUT("api/clientes/{id}/")
    suspend fun actualizarCliente(
        @Path("id") id: Int,
        @Body cliente: Cliente
    ): Response<Cliente>

    @DELETE("api/clientes/{id}/")
    suspend fun eliminarCliente(
        @Path("id") id: Int
    ): Response<Unit>

    // ─────────────────────────────────────────────────────────────────────────
    // VEHÍCULOS  —  /api/vehiculos/
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/vehiculos/")
    suspend fun getVehiculos(
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<Vehiculo>>

    @POST("api/vehiculos/")
    suspend fun crearVehiculo(
        @Body vehiculo: Map<String, Any>
    ): Response<Vehiculo>

    @PUT("api/vehiculos/{id}/")
    suspend fun actualizarVehiculo(
        @Path("id") id: Int,
        @Body vehiculo: Map<String, Any>
    ): Response<Vehiculo>

    @DELETE("api/vehiculos/{id}/")
    suspend fun eliminarVehiculo(
        @Path("id") id: Int
    ): Response<Unit>

    // ─────────────────────────────────────────────────────────────────────────
    // SERVICIOS  —  /api/servicios/
    // Lectura: todos los roles. Escritura: solo admin.
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/servicios/")
    suspend fun getServicios(
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<Servicio>>

    @POST("api/servicios/")
    suspend fun crearServicio(
        @Body servicio: Servicio
    ): Response<Servicio>

    @PUT("api/servicios/{id}/")
    suspend fun actualizarServicio(
        @Path("id") id: Int,
        @Body servicio: Servicio
    ): Response<Servicio>

    @DELETE("api/servicios/{id}/")
    suspend fun eliminarServicio(
        @Path("id") id: Int
    ): Response<Unit>

    // ─────────────────────────────────────────────────────────────────────────
    // ÓRDENES DE TRABAJO  —  /api/ordenes/
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/ordenes/")
    suspend fun getOrdenes(
        @Query("estado") estado: String? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<OrdenTrabajo>>

    @POST("api/ordenes/")
    suspend fun crearOrden(
        @Body orden: Map<String, Any?>
    ): Response<OrdenTrabajo>

    @PUT("api/ordenes/{id}/")
    suspend fun actualizarOrden(
        @Path("id") id: Int,
        @Body orden: Map<String, Any?>
    ): Response<OrdenTrabajo>

    @DELETE("api/ordenes/{id}/")
    suspend fun eliminarOrden(
        @Path("id") id: Int
    ): Response<Unit>

    /**
     * PUT /api/ordenes/{id}/estado/
     * Body: { "estado": "en_proceso", "observaciones": "..." }
     */
    @PUT("api/ordenes/{id}/estado/")
    suspend fun cambiarEstadoOrden(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<OrdenTrabajo>

    // ─────────────────────────────────────────────────────────────────────────
    // FACTURAS  —  /api/facturas/
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/facturas/")
    suspend fun getFacturas(
        @Query("estado_pago") estadoPago: String? = null,
        @Query("page") page: Int? = null,
        @Query("search") search: String? = null
    ): Response<PaginatedResponse<Factura>>

    @POST("api/facturas/")
    suspend fun crearFactura(
        @Body body: Map<String, Any>
    ): Response<Factura>

    /**
     * PUT /api/facturas/{id}/marcar-pagada/
     * Acción rápida: cambia estado_pago a "pagado" directamente.
     */
    @PUT("api/facturas/{id}/marcar-pagada/")
    suspend fun marcarFacturaPagada(
        @Path("id") id: Int
    ): Response<Factura>

    // ─────────────────────────────────────────────────────────────────────────
    // PAGOS  —  /api/pagos/
    // Solo GET y POST. Sin PUT ni DELETE.
    // ─────────────────────────────────────────────────────────────────────────

    @GET("api/pagos/")
    suspend fun getPagos(): Response<List<Pago>>

    /**
     * POST /api/pagos/
     * Body: { "factura": Int, "metodo_pago": "efectivo|tarjeta|transferencia", "monto": Double }
     * Si la suma de pagos cubre el total, el backend retorna { "factura_saldada": true }
     */
    @POST("api/pagos/")
    suspend fun registrarPago(
        @Body body: Map<String, Any>
    ): Response<PagoResponse>
}
