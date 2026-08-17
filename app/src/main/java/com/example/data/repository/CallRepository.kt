package com.example.data.repository

import com.example.data.model.CallSession
import com.example.data.model.CallStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallRepository {
    private val _currentCall = MutableStateFlow<CallSession?>(null)
    val currentCall: StateFlow<CallSession?> = _currentCall.asStateFlow()

    fun startCall(callerName: String, isVideo: Boolean) {
        _currentCall.value = CallSession(
            callId = UUID.randomUUID().toString(),
            callerName = callerName,
            isVideo = isVideo,
            isIncoming = false,
            durationSeconds = 0,
            status = CallStatus.RINGING
        )
    }

    fun acceptCall() {
        _currentCall.value = _currentCall.value?.copy(status = CallStatus.CONNECTED)
    }

    fun endCall() {
        _currentCall.value = _currentCall.value?.copy(status = CallStatus.ENDED)
        _currentCall.value = null
    }

    fun tickDuration() {
        _currentCall.value?.let {
            if (it.status == CallStatus.CONNECTED) {
                _currentCall.value = it.copy(durationSeconds = it.durationSeconds + 1)
            }
        }
    }
}
