package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallSession
import com.example.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CallViewModel(private val callRepository: CallRepository) : ViewModel() {

    val currentCall: StateFlow<CallSession?> = callRepository.currentCall
    private var timerJob: Job? = null

    fun startCall(callerName: String, isVideo: Boolean) {
        callRepository.startCall(callerName, isVideo)
        startTimer()
    }

    fun acceptCall() {
        callRepository.acceptCall()
    }

    fun endCall() {
        timerJob?.cancel()
        timerJob = null
        callRepository.endCall()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                callRepository.tickDuration()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
