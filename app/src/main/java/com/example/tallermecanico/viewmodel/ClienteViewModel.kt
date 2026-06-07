package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.model.Cliente
import com.example.tallermecanico.repository.ClienteRepository
import com.example.tallermecanico.repository.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClienteUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val clientes: List<Cliente> = emptyList(),
    val pageActual: Int = 1,
    val hasMore: Boolean = true,
    val searchQuery: String = "",
    val successMessage: String? = null,
    val error: String? = null
)

class ClienteViewModel(
    private val repository: ClienteRepository = ClienteRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        obtenerClientes(reset = true)
    }

    /**
     * Carga clientes del backend.
     * @param reset Si es verdadero, limpia la lista y vuelve a la página 1.
     */
    fun obtenerClientes(reset: Boolean = false) {
        val currentState = _uiState.value
        
        // Evita cargas duplicadas
        if (currentState.isLoading || currentState.isPaginating) {
            if (!reset) return
        }
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

            when (val result = repository.getClientes(page = pageToLoad, search = currentState.searchQuery.ifBlank { null })) {
                is Result.Success -> {
                    val paginated = result.data
                    val newClientes = paginated.results
                    val updatedList = if (reset) newClientes else currentState.clientes + newClientes
                    
                    // Si next es nulo, significa que ya no hay más páginas en el backend
                    val hasMoreResults = paginated.next != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        clientes = updatedList,
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

    /**
     * Actualiza el query de búsqueda y recarga los datos desde la página 1 con debounce de 500ms.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce para no saturar el servidor en cada pulsación
            obtenerClientes(reset = true)
        }
    }

    fun crearCliente(nombre: String, telefono: String, direccion: String, correo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val cliente = Cliente(nombre = nombre, telefono = telefono, direccion = direccion, correo = correo)
            when (val result = repository.crearCliente(cliente)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Cliente creado exitosamente")
                    obtenerClientes(reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun actualizarCliente(id: Int, nombre: String, telefono: String, direccion: String, correo: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val cliente = Cliente(id = id, nombre = nombre, telefono = telefono, direccion = direccion, correo = correo)
            when (val result = repository.actualizarCliente(id, cliente)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Cliente actualizado exitosamente")
                    obtenerClientes(reset = true)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun eliminarCliente(id: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.eliminarCliente(id)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Cliente eliminado exitosamente")
                    obtenerClientes(reset = true)
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
