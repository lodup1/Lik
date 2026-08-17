package com.example.data.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val pairCode: String,
    val pairedPartnerId: String? = null,
    val avatarEmoji: String = "❤️",
    val statusMessage: String = "Connected on Lik",
    val currentMood: String = "Happy",
    val anniversaryDate: Long? = null
)

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,
    val reaction: String? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    LOCATION,
    STICKER
}

data class GoalItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val targetDate: String? = null,
    val category: String = "General"
)

data class CallSession(
    val callId: String,
    val callerName: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val durationSeconds: Int = 0,
    val status: CallStatus = CallStatus.RINGING
)

enum class CallStatus {
    RINGING,
    CONNECTED,
    ENDED
}
