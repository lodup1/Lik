package com.example.data.model

import java.util.UUID

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

enum class MediaType {
    NONE, IMAGE, VIDEO, VOICE
}

enum class CallType {
    VOICE, VIDEO
}

enum class CallStatus {
    IDLE, DIALING, RINGING, INCOMING, CONNECTED, ENDED, REJECTED
}

data class UserAccount(
    val id: String = UUID.randomUUID().toString(),
    val username: String = "",
    val dateOfBirth: String = "",
    val displayName: String = "",
    val avatarEmoji: String = "❤️",
    val customAvatarUrl: String? = null,
    val statusMood: String? = null,
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val showLastSeen: Boolean = true,
    val pairCode: String = "",
    val pairedUserId: String? = null,
    val deviceName: String = "Android Device"
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiverId: String,
    val text: String = "",
    val mediaType: MediaType = MediaType.NONE,
    val mediaUrl: String? = null,
    val mediaSizeFormatted: String? = null,
    val uploadProgress: Float = 1.0f, // 0.0 to 1.0
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val isDeleted: Boolean = false
)

data class ActiveCall(
    val callId: String = UUID.randomUUID().toString(),
    val callerId: String,
    val callerName: String,
    val callerAvatar: String,
    val receiverId: String,
    val callType: CallType,
    val status: CallStatus = CallStatus.DIALING,
    val isMuted: Boolean = false,
    val isVideoOff: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isSpeakerOn: Boolean = true,
    val startTime: Long = 0L,
    val durationSeconds: Int = 0
)

data class PairingInfo(
    val isPaired: Boolean = false,
    val partnerId: String? = null,
    val partnerUsername: String? = null,
    val partnerDisplayName: String? = null,
    val partnerAvatarEmoji: String? = null,
    val partnerAvatarUrl: String? = null,
    val partnerStatusMood: String? = null,
    val partnerPhoneNumber: String? = null,
    val partnerIsOnline: Boolean = true,
    val partnerLastSeen: Long = System.currentTimeMillis(),
    val connectedSinceTimestamp: Long = System.currentTimeMillis(),
    val anniversaryDateMs: Long = System.currentTimeMillis()
)
