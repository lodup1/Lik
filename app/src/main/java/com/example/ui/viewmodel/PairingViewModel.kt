package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PairingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairingViewModel(private val pairingRepository: PairingRepository) : ViewModel() {

    private val _pairingState = MutableStateFlow<PairingUiState>(PairingUiState.Idle)
    val pairingState: StateFlow<PairingUiState> = _pairingState.asStateFlow()

    fun pairWithPartner(code: String) {
        viewModelScope.launch {
            _pairingState.value = PairingUiState.Loading
            val result = pairingRepository.pairWithCode(code)
            result.fold(
                onSuccess = { msg ->
                    _pairingState.value = PairingUiState.Success(msg)
                },
                onFailure = { err ->
                    _pairingState.value = PairingUiState.Error(err.message ?: "Failed to pair")
                }
            )
        }
    }

    fun resetState() {
        _pairingState.value = PairingUiState.Idle
    }

    fun unpair() {
        viewModelScope.launch {
            pairingRepository.unpair()
            _pairingState.value = PairingUiState.Idle
        }
    }
}

sealed class PairingUiState {
    object Idle : PairingUiState()
    object Loading : PairingUiState()
    data class Success(val message: String) : PairingUiState()
    data class Error(val error: String) : PairingUiState()
}
