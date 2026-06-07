package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.model.OrdenTrabajo
import com.example.tallermecanico.repository.OrdenRepository
import com.example.tallermecanico.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdenUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val ordenes: List<OrdenTrabajo> = emptyList(),
    val pageActual: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val filtroEstado: String? = null,
    val successMessage: String? = null,
    val error: String? = null
)

class OrdenViewModel(
    private val repository: OrdenRepository = OrdenRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdenUiState())
    val uiState: StateFlow<OrdenUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun cargarOrdenes(estado: String? = null, reset: Boolean = false) {
        val currentState = _uiState.value
        
        // Si el estado cambia, reseteamos la paginación
        val actualReset = reset || (estado != currentState.filtroEstado)
        
        if (currentState.isLoading || currentState.isPaginating) {
            // Permitimos continuar si estamos explícitamente forzando un reset
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
                    filtroEstado = estado,
                    error = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isPaginating = true,
                    error = null
                )
            }

            // Volvemos a leer el estado actualizado para obtener el query de búsqueda
            val query = _uiState.value.searchQuery

            when (val result = repository.getOrdenes(estado = estado, page = pageToLoad, search = query.ifBlank { null })) {
                is Result.Success -> {
                    val paginated = result.data
                    val newOrdenes = paginated.results
                    val updatedList = if (actualReset) newOrdenes else _uiState.value.ordenes + newOrdenes
                    val hasMoreResults = paginated.next != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        ordenes = updatedList,
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
            cargarOrdenes(estado = _uiState.value.filtroEstado, reset = true)
        }
    }

    fun cambiarEstado(id: Int, nuevoEstado: String, observaciones: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.cambiarEstado(id, nuevoEstado, observaciones)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Estado actualizado a: ${nuevoEstado.replace("_", " ")}"
                    )
                    cargarOrdenes(estado = _uiState.value.filtroEstado, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun crearOrden(vehiculoId: Int, mecanicoId: Int?, observaciones: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.crearOrden(vehiculoId, mecanicoId, observaciones)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Orden creada correctamente"
                    )
                    cargarOrdenes(estado = _uiState.value.filtroEstado, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun eliminarOrden(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.eliminarOrden(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Orden eliminada"
                    )
                    cargarOrdenes(estado = _uiState.value.filtroEstado, reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
