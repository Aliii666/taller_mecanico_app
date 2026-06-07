package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.model.Factura
import com.example.tallermecanico.model.Pago
import com.example.tallermecanico.repository.FacturaPagoRepository
import com.example.tallermecanico.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FacturaPagoUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val facturas: List<Factura> = emptyList(),
    val pagos: List<Pago> = emptyList(),
    val pageActual: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val filtroEstadoPago: String? = null,
    val successMessage: String? = null,
    val facturaSaldada: Boolean = false,
    val error: String? = null
)

class FacturaPagoViewModel(
    private val repository: FacturaPagoRepository = FacturaPagoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacturaPagoUiState())
    val uiState: StateFlow<FacturaPagoUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun cargarFacturas(estadoPago: String? = null, reset: Boolean = false) {
        val currentState = _uiState.value
        val actualReset = reset || (estadoPago != currentState.filtroEstadoPago)

        if (currentState.isLoading || currentState.isPaginating) {
            if (!actualReset) return
        }
        if (!actualReset && !currentState.hasMore) return

        val pageToLoad = if (actualReset) 1 else currentState.pageActual + 1

        viewModelScope.launch {
            if (actualReset) {
                _uiState.value = currentState.copy(
                    isLoading = true,
                    pageActual = 1,
                    hasMore = true,
                    filtroEstadoPago = estadoPago,
                    error = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isPaginating = true,
                    error = null
                )
            }

            val query = _uiState.value.searchQuery

            when (val result = repository.getFacturas(estadoPago = estadoPago, page = pageToLoad, search = query.ifBlank { null })) {
                is Result.Success -> {
                    val paginated = result.data
                    val newFacturas = paginated.results
                    val updatedList = if (actualReset) newFacturas else _uiState.value.facturas + newFacturas
                    val hasMoreResults = paginated.next != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        facturas = updatedList,
                        pageActual = pageToLoad,
                        hasMore = hasMoreResults
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            cargarFacturas(estadoPago = _uiState.value.filtroEstadoPago, reset = true)
        }
    }

    fun crearFactura(ordenId: Int, total: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.crearFactura(ordenId, total)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Factura emitida correctamente para la orden #$ordenId"
                    )
                    cargarFacturas(estadoPago = _uiState.value.filtroEstadoPago, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun registrarPago(facturaId: Int, monto: Double, metodoPago: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.registrarPago(facturaId, monto, metodoPago)) {
                is Result.Success -> {
                    val saldada = result.data.facturaSaldada == true
                    _uiState.value = _uiState.value.copy(
                        facturaSaldada = saldada,
                        successMessage = if (saldada) "¡Factura saldada completamente!"
                                         else "Pago registrado correctamente"
                    )
                    cargarFacturas(estadoPago = _uiState.value.filtroEstadoPago, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = result.message
                )
            }
        }
    }

    fun marcarPagada(facturaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.marcarPagada(facturaId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Factura marcada como pagada"
                    )
                    cargarFacturas(estadoPago = _uiState.value.filtroEstadoPago, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = result.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error          = null,
            successMessage = null,
            facturaSaldada = false
        )
    }
}
