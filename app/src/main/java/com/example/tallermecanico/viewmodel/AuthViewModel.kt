package com.example.tallermecanico.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tallermecanico.data.SessionManager
import com.example.tallermecanico.model.Usuario
import com.example.tallermecanico.repository.AuthRepository
import com.example.tallermecanico.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean  = false,
    val isLoggedIn: Boolean = false,
    val usuario: Usuario?   = null,
    val error: String?      = null
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Role del usuario; reacciona en tiempo real a cambios en DataStore */
    val roleFlow = sessionManager.roleFlow

    init {
        viewModelScope.launch {
            val loggedIn = sessionManager.isLoggedInFlow.first()
            _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.login(email.trim(), password)) {
                is Result.Success -> _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    usuario    = result.data.user
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = result.message
                )
            }
        }
    }

    // ── Registro ──────────────────────────────────────────────────────────────
    // El backend asigna role "client" automáticamente desde /register/

    fun registro(username: String, email: String, password: String, password2: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.registro(username.trim(), email.trim(), password, password2)) {
                is Result.Success -> _uiState.value = AuthUiState(
                    isLoggedIn = true,
                    usuario    = result.data.user
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = result.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState(isLoggedIn = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(
        private val repository: AuthRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repository, sessionManager) as T
    }
}
