package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(displayName: String, username: String) {
        viewModelScope.launch {
            authRepository.loginOrRegister(displayName, username)
        }
    }

    fun updateProfile(displayName: String, avatarEmoji: String, status: String, mood: String) {
        viewModelScope.launch {
            authRepository.updateProfile(displayName, avatarEmoji, status, mood)
        }
    }

    fun setAnniversary(timestamp: Long) {
        viewModelScope.launch {
            authRepository.setAnniversary(timestamp)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
