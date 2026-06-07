package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.model.Servicio
import com.example.tallermecanico.repository.Result
import com.example.tallermecanico.repository.ServicioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicioUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val pageActual: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val successMessage: String? = null,
    val error: String? = null
)

class ServicioViewModel(
    private val repository: ServicioRepository = ServicioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicioUiState())
    val uiState: StateFlow<ServicioUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        cargarServicios(reset = true)
    }

    fun cargarServicios(reset: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isPaginating) return
        if (!reset && !currentState.hasMore) return

        val pageToLoad = if (reset) 1 else currentState.pageActual + 1

        viewModelScope.launch {
            if (reset) {
                _uiState.value = currentState.copy(
                    isLoading = true,
                    pageActual = 1,
                    hasMore = true,
                    error = null
                )
            } else {
                _uiState.value = currentState.copy(
                    isPaginating = true,
                    error = null
                )
            }

            when (val result = repository.getServicios(page = pageToLoad, search = currentState.searchQuery.ifBlank { null })) {
                is Result.Success -> {
                    val paginated = result.data
                    val newServicios = paginated.results
                    val updatedList = if (reset) newServicios else currentState.servicios + newServicios
                    val hasMoreResults = paginated.next != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        servicios = updatedList,
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
            cargarServicios(reset = true)
        }
    }

    fun crear(nombre: String, descripcion: String, precio: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.crearServicio(nombre, descripcion, precio)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Servicio creado con éxito")
                    cargarServicios(reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun actualizar(id: Int, nombre: String, descripcion: String, precio: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.actualizarServicio(id, nombre, descripcion, precio)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Servicio actualizado con éxito")
                    cargarServicios(reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun eliminar(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.eliminarServicio(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Servicio eliminado con éxito")
                    cargarServicios(reset = true)
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
