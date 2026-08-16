package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserAccount
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Authenticated(val user: UserAccount) : AuthUiState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _usernameAvailability = MutableStateFlow<Boolean?>(null)
    val usernameAvailability: StateFlow<Boolean?> = _usernameAvailability.asStateFlow()

    fun checkUsernameAvailability(username: String) {
        val trimmed = username.trim()
        if (trimmed.length < 2) {
            _usernameAvailability.value = null
            return
        }
        viewModelScope.launch {
            val available = authRepository.isUsernameAvailable(trimmed)
            _usernameAvailability.value = available
        }
    }

    fun clearUsernameAvailability() {
        _usernameAvailability.value = null
    }

    fun register(username: String, passwordPlain: String, dateOfBirth: String) {
        if (username.isBlank() || username.length < 2) {
            _uiState.value = AuthUiState.Error("Username must be at least 2 characters.")
            return
        }
        if (passwordPlain.isBlank() || passwordPlain.length < 4) {
            _uiState.value = AuthUiState.Error("Password must be at least 4 characters.")
            return
        }
        if (dateOfBirth.isBlank()) {
            _uiState.value = AuthUiState.Error("Please select your Date of Birth.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.register(username, passwordPlain, dateOfBirth)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { ex ->
                _uiState.value = AuthUiState.Error(ex.message ?: "Registration failed.")
            }
        }
    }

    fun login(username: String, passwordPlain: String) {
        if (username.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your username.")
            return
        }
        if (passwordPlain.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.login(username, passwordPlain)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { ex ->
                _uiState.value = AuthUiState.Error(ex.message ?: "Login failed.")
            }
        }
    }

    fun updateProfile(displayName: String, avatarEmoji: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            authRepository.updateProfile(displayName, avatarEmoji, avatarUrl)
        }
    }

    fun updateStatusMood(status: String?) {
        authRepository.updateStatusMood(status)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
