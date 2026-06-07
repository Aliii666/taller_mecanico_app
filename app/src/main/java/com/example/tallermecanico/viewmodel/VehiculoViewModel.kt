package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.model.Vehiculo
import com.example.tallermecanico.repository.VehiculoRepository
import com.example.tallermecanico.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VehiculoUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val vehiculos: List<Vehiculo> = emptyList(),
    val pageActual: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val successMessage: String? = null,
    val error: String? = null
)

class VehiculoViewModel(
    private val repository: VehiculoRepository = VehiculoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculoUiState())
    val uiState: StateFlow<VehiculoUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        cargarVehiculos(reset = true)
    }

    fun cargarVehiculos(reset: Boolean = false) {
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

            when (val result = repository.getVehiculos(page = pageToLoad, search = currentState.searchQuery.ifBlank { null })) {
                is Result.Success -> {
                    val paginated = result.data
                    val newVehiculos = paginated.results
                    val updatedList = if (reset) newVehiculos else currentState.vehiculos + newVehiculos
                    val hasMoreResults = paginated.next != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        vehiculos = updatedList,
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
            cargarVehiculos(reset = true)
        }
    }

    fun crear(clienteId: Int, marca: String, modelo: String, placa: String, anio: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.crearVehiculo(clienteId, marca, modelo, placa, anio)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Vehículo registrado con éxito")
                    cargarVehiculos(reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun actualizar(id: Int, clienteId: Int, marca: String, modelo: String, placa: String, anio: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.actualizarVehiculo(id, clienteId, marca, modelo, placa, anio)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Vehículo actualizado con éxito")
                    cargarVehiculos(reset = true)
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
            when (val result = repository.eliminarVehiculo(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Vehículo eliminado con éxito")
                    cargarVehiculos(reset = true)
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
