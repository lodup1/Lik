package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.ActiveCall
import com.example.data.model.CallType
import com.example.data.repository.CallRepository
import kotlinx.coroutines.flow.StateFlow

class CallViewModel(private val callRepository: CallRepository) : ViewModel() {

    val activeCall: StateFlow<ActiveCall?> = callRepository.activeCall

    fun startVoiceCall() {
        callRepository.initiateCall(CallType.VOICE)
    }

    fun startVideoCall() {
        callRepository.initiateCall(CallType.VIDEO)
    }

    fun simulatePartnerIncomingCall(callType: CallType = CallType.VIDEO) {
        callRepository.simulateIncomingCall(callType)
    }

    fun acceptCall() {
        callRepository.acceptIncomingCall()
    }

    fun rejectCall() {
        callRepository.rejectCall()
    }

    fun endCall() {
        callRepository.endCall()
    }

    fun toggleMute() {
        callRepository.toggleMute()
    }

    fun toggleVideo() {
        callRepository.toggleVideo()
    }

    fun toggleFrontCamera() {
        callRepository.toggleFrontCamera()
    }

    fun toggleSpeaker() {
        callRepository.toggleSpeaker()
    }
}
