package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ActiveCall
import com.example.data.model.CallStatus
import com.example.data.model.CallType
import com.example.data.remote.LikRealtimeSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class CallRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val pairingRepository: PairingRepository,
    private val syncManager: LikRealtimeSyncManager
) {
    private val tag = "CallRepository"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    init {
        setupCallSignaling()
    }

    private fun setupCallSignaling() {
        syncManager.onCallSignalReceived = { json ->
            scope.launch {
                try {
                    val type = json.optString("action")
                    val callId = json.optString("callId")
                    val callTypeStr = json.optString("callType", "VOICE")
                    val callType = try { CallType.valueOf(callTypeStr) } catch (e: Exception) { CallType.VOICE }
                    val senderName = json.optString("senderName", "Partner")
                    val senderAvatar = json.optString("senderAvatar", "🌸")
                    val senderId = json.optString("senderId")

                    when (type) {
                        "OFFER" -> {
                            val incomingCall = ActiveCall(
                                callId = callId,
                                callerId = senderId,
                                callerName = senderName,
                                callerAvatar = senderAvatar,
                                receiverId = authRepository.currentUser.value?.id ?: "me",
                                callType = callType,
                                status = CallStatus.INCOMING,
                                isMuted = false,
                                isVideoOff = callType == CallType.VOICE,
                                isFrontCamera = true,
                                isSpeakerOn = callType == CallType.VIDEO,
                                startTime = System.currentTimeMillis()
                            )
                            _activeCall.value = incomingCall
                        }
                        "ANSWER" -> {
                            if (_activeCall.value?.callId == callId) {
                                connectCall(notifyRemote = false)
                            }
                        }
                        "REJECT" -> {
                            if (_activeCall.value?.callId == callId) {
                                timerJob?.cancel()
                                _activeCall.value = _activeCall.value?.copy(status = CallStatus.REJECTED)
                                delay(800)
                                _activeCall.value = null
                            }
                        }
                        "END" -> {
                            if (_activeCall.value?.callId == callId) {
                                timerJob?.cancel()
                                _activeCall.value = _activeCall.value?.copy(status = CallStatus.ENDED)
                                delay(1000)
                                _activeCall.value = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error handling call signal", e)
                }
            }
        }
    }

    fun initiateCall(callType: CallType) {
        val currentUser = authRepository.currentUser.value
        val partner = pairingRepository.pairingInfo.value
        val callId = "call_${UUID.randomUUID().toString().take(8)}"

        val call = ActiveCall(
            callId = callId,
            callerId = currentUser?.id ?: "me",
            callerName = currentUser?.displayName ?: "Me",
            callerAvatar = currentUser?.avatarEmoji ?: "❤️",
            receiverId = partner.partnerId ?: "partner_account",
            callType = callType,
            status = CallStatus.DIALING,
            isMuted = false,
            isVideoOff = callType == CallType.VOICE,
            isFrontCamera = true,
            isSpeakerOn = callType == CallType.VIDEO,
            startTime = System.currentTimeMillis()
        )

        _activeCall.value = call

        // Broadcast offer signal over realtime room
        sendCallSignal("OFFER", callId, callType)

        scope.launch {
            delay(1000)
            if (_activeCall.value?.callId == call.callId && _activeCall.value?.status == CallStatus.DIALING) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatus.RINGING)
            }
        }
    }

    fun simulateIncomingCall(callType: CallType) {
        val partner = pairingRepository.pairingInfo.value

        val call = ActiveCall(
            callId = "call_${UUID.randomUUID().toString().take(8)}",
            callerId = partner.partnerId ?: "partner_account",
            callerName = partner.partnerDisplayName ?: "My Love ❤️",
            callerAvatar = partner.partnerAvatarEmoji ?: "🌸",
            receiverId = authRepository.currentUser.value?.id ?: "me",
            callType = callType,
            status = CallStatus.INCOMING,
            isMuted = false,
            isVideoOff = callType == CallType.VOICE,
            isFrontCamera = true,
            isSpeakerOn = callType == CallType.VIDEO,
            startTime = System.currentTimeMillis()
        )

        _activeCall.value = call
    }

    fun acceptIncomingCall() {
        val current = _activeCall.value ?: return
        if (current.status == CallStatus.INCOMING) {
            connectCall(notifyRemote = true)
        }
    }

    private fun connectCall(notifyRemote: Boolean = true) {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(
            status = CallStatus.CONNECTED,
            startTime = System.currentTimeMillis(),
            durationSeconds = 0
        )
        if (notifyRemote) {
            sendCallSignal("ANSWER", current.callId, current.callType)
        }
        startCallTimer()
    }

    private fun startCallTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_activeCall.value?.status == CallStatus.CONNECTED) {
                delay(1000)
                val current = _activeCall.value
                if (current != null && current.status == CallStatus.CONNECTED) {
                    _activeCall.value = current.copy(durationSeconds = current.durationSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    fun rejectCall() {
        val current = _activeCall.value
        timerJob?.cancel()
        if (current != null) {
            sendCallSignal("REJECT", current.callId, current.callType)
        }
        _activeCall.value = _activeCall.value?.copy(status = CallStatus.REJECTED)
        scope.launch {
            delay(800)
            _activeCall.value = null
        }
    }

    fun endCall() {
        val current = _activeCall.value
        timerJob?.cancel()
        if (current != null) {
            sendCallSignal("END", current.callId, current.callType)
        }
        _activeCall.value = _activeCall.value?.copy(status = CallStatus.ENDED)
        scope.launch {
            delay(1000)
            _activeCall.value = null
        }
    }

    fun toggleMute() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleVideo() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isVideoOff = !current.isVideoOff)
    }

    fun toggleFrontCamera() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    fun toggleSpeaker() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    private fun sendCallSignal(action: String, callId: String, callType: CallType) {
        val myUser = authRepository.currentUser.value
        val prefs = context.getSharedPreferences("lik_pairing_prefs", Context.MODE_PRIVATE)
        val partnerCode = prefs.getString("partner_pair_code", "") ?: ""
        val myCode = myUser?.pairCode ?: ""

        if (myCode.isNotBlank() && partnerCode.isNotBlank()) {
            val cleanA = myCode.trim().uppercase().replace("-", "")
            val cleanB = partnerCode.trim().uppercase().replace("-", "")
            val roomId = "lik_room_" + listOf(cleanA, cleanB).sorted().joinToString("_")

            val json = JSONObject().apply {
                put("action", action)
                put("callId", callId)
                put("callType", callType.name)
                put("senderId", myUser?.id ?: "me")
                put("senderName", myUser?.displayName ?: "Partner")
                put("senderAvatar", myUser?.avatarEmoji ?: "🌸")
                put("timestamp", System.currentTimeMillis())
            }
            syncManager.publishCallSignal(roomId, json)
        }
    }
}
