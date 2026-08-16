package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PairingInfo
import com.example.data.repository.PairingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PairingUiState {
    object Idle : PairingUiState()
    object Loading : PairingUiState()
    data class Error(val message: String) : PairingUiState()
    data class Success(val pairingInfo: PairingInfo) : PairingUiState()
}

class PairingViewModel(private val pairingRepository: PairingRepository) : ViewModel() {

    val pairingInfo: StateFlow<PairingInfo> = pairingRepository.pairingInfo

    private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Idle)
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    fun pairWithCode(code: String) {
        if (code.isBlank()) {
            _uiState.value = PairingUiState.Error("Please enter your partner's pair code.")
            return
        }

        _uiState.value = PairingUiState.Loading
        viewModelScope.launch {
            val res = pairingRepository.pairWithCode(code)
            res.onSuccess { info ->
                _uiState.value = PairingUiState.Success(info)
            }.onFailure { ex ->
                _uiState.value = PairingUiState.Error(ex.message ?: "Pairing failed.")
            }
        }
    }

    fun quickConnectDemo() {
        _uiState.value = PairingUiState.Loading
        viewModelScope.launch {
            val info = pairingRepository.createQuickDemoPairing("My Partner ❤️")
            _uiState.value = PairingUiState.Success(info)
        }
    }

    fun updatePartnerDp(avatarUrl: String?) {
        pairingRepository.updatePartnerDp(avatarUrl)
    }

    fun updatePartnerStatusMood(status: String?) {
        pairingRepository.updatePartnerStatusMood(status)
    }

    fun updateAnniversaryDate(anniversaryMs: Long) {
        pairingRepository.updateAnniversaryDate(anniversaryMs)
    }

    fun unpair() {
        pairingRepository.unpair()
        _uiState.value = PairingUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is PairingUiState.Error) {
            _uiState.value = PairingUiState.Idle
        }
    }
}
