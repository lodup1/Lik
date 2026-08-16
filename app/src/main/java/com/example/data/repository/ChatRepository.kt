package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import com.example.data.remote.LikRealtimeSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val pairingRepository: PairingRepository,
    private val syncManager: LikRealtimeSyncManager
) {
    private val tag = "ChatRepository"
    private val db = AppDatabase.getDatabase(context)
    private val chatDao = db.chatDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    private val _partnerOnlineStatus = MutableStateFlow(true)
    val partnerOnlineStatus: StateFlow<Boolean> = _partnerOnlineStatus.asStateFlow()

    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { entity ->
            ChatMessage(
                id = entity.id,
                senderId = entity.senderId,
                receiverId = entity.receiverId,
                text = entity.text,
                mediaType = try {
                    MediaType.valueOf(entity.mediaType)
                } catch (e: Exception) {
                    MediaType.NONE
                },
                mediaUrl = entity.mediaUrl,
                mediaSizeFormatted = entity.mediaSizeFormatted,
                uploadProgress = entity.uploadProgress,
                timestamp = entity.timestamp,
                status = try {
                    MessageStatus.valueOf(entity.status)
                } catch (e: Exception) {
                    MessageStatus.SENT
                },
                replyToId = entity.replyToId,
                replyToText = entity.replyToText,
                replyToSenderName = entity.replyToSenderName,
                isDeleted = entity.isDeleted
            )
        }
    }

    init {
        setupSyncCallbacks()
    }

    private fun setupSyncCallbacks() {
        syncManager.onMessageReceived = { incomingMessage ->
            Log.d(tag, "Incoming realtime message: ${incomingMessage.id} -> ${incomingMessage.text}")
            handleIncomingMessage(incomingMessage)
        }

        syncManager.onMessageStatusUpdated = { messageId, status ->
            Log.d(tag, "Message status update: $messageId -> $status")
            scope.launch {
                chatDao.updateMessageStatus(messageId, status.name)
            }
        }

        syncManager.onPartnerTyping = { isTyping ->
            _isPartnerTyping.value = isTyping
        }
    }

    private fun handleIncomingMessage(incoming: ChatMessage) {
        scope.launch {
            // Save incoming message into Room DB so Flow emits it immediately
            val entity = incoming.toEntity().copy(status = MessageStatus.READ.name)
            chatDao.insertMessage(entity)

            // Trigger system notification if message is from partner
            val currentUserId = authRepository.currentUser.value?.id ?: ""
            if (incoming.senderId != currentUserId) {
                val partnerInfo = pairingRepository.pairingInfo.value
                val rawPartnerName = partnerInfo.partnerDisplayName?.ifBlank { null }
                    ?: partnerInfo.partnerUsername?.ifBlank { null }
                    ?: incoming.replyToSenderName?.ifBlank { null }
                    ?: ""
                val partnerDisplayName = rawPartnerName.removePrefix("@").ifBlank { "User" }

                com.example.data.notification.LikNotificationManager.showMessageNotification(
                    context = context,
                    message = incoming,
                    senderDisplayName = partnerDisplayName,
                    senderAvatarUrl = partnerInfo.partnerAvatarUrl,
                    senderAvatarEmoji = partnerInfo.partnerAvatarEmoji ?: "❤️",
                    currentUserId = currentUserId
                )
            }

            // Send back delivery receipt
            val roomId = getActiveRoomId()
            if (roomId.isNotBlank()) {
                syncManager.publishMessageStatus(roomId, incoming.id, MessageStatus.READ)
            }
        }
    }

    fun setPartnerTyping(isTyping: Boolean) {
        val roomId = getActiveRoomId()
        if (roomId.isNotBlank()) {
            syncManager.publishTyping(roomId, isTyping)
        }
    }

    fun searchMessages(query: String): Flow<List<ChatMessage>> {
        return chatDao.searchMessages(query).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    senderId = entity.senderId,
                    receiverId = entity.receiverId,
                    text = entity.text,
                    mediaType = try {
                        MediaType.valueOf(entity.mediaType)
                    } catch (e: Exception) {
                        MediaType.NONE
                    },
                    mediaUrl = entity.mediaUrl,
                    mediaSizeFormatted = entity.mediaSizeFormatted,
                    uploadProgress = entity.uploadProgress,
                    timestamp = entity.timestamp,
                    status = try {
                        MessageStatus.valueOf(entity.status)
                    } catch (e: Exception) {
                        MessageStatus.SENT
                    },
                    replyToId = entity.replyToId,
                    replyToText = entity.replyToText,
                    replyToSenderName = entity.replyToSenderName,
                    isDeleted = entity.isDeleted
                )
            }
        }
    }

    suspend fun sendMessage(
        text: String,
        mediaType: MediaType = MediaType.NONE,
        mediaUrl: String? = null,
        mediaSizeFormatted: String? = null,
        replyToMsg: ChatMessage? = null
    ): ChatMessage {
        val currentUserId = authRepository.currentUser.value?.id ?: "me"
        val partnerId = pairingRepository.pairingInfo.value.partnerId ?: "partner_account"

        val msg = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(12)}",
            senderId = currentUserId,
            receiverId = partnerId,
            text = text,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            mediaSizeFormatted = mediaSizeFormatted,
            uploadProgress = 1.0f,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENDING,
            replyToId = replyToMsg?.id,
            replyToText = replyToMsg?.text?.take(40),
            replyToSenderName = if (replyToMsg?.senderId == currentUserId) "You" else "Partner"
        )

        // Insert into local Room DB immediately
        chatDao.insertMessage(msg.toEntity())

        // Publish to realtime internet network
        val roomId = getActiveRoomId()
        if (roomId.isNotBlank()) {
            syncManager.publishChatMessage(roomId, msg) { success ->
                scope.launch {
                    val finalStatus = if (success) MessageStatus.SENT else MessageStatus.DELIVERED
                    chatDao.updateMessageStatus(msg.id, finalStatus.name)
                }
            }
        } else {
            // If room not set yet, keep as SENT
            scope.launch {
                chatDao.updateMessageStatus(msg.id, MessageStatus.SENT.name)
            }
        }

        return msg
    }

    suspend fun retryFailedUpload(messageId: String) {
        scope.launch {
            val entity = chatDao.getMessageById(messageId) ?: return@launch
            val roomId = getActiveRoomId()
            if (roomId.isNotBlank()) {
                val msg = ChatMessage(
                    id = entity.id,
                    senderId = entity.senderId,
                    receiverId = entity.receiverId,
                    text = entity.text,
                    mediaType = try { MediaType.valueOf(entity.mediaType) } catch (e: Exception) { MediaType.NONE },
                    mediaUrl = entity.mediaUrl,
                    mediaSizeFormatted = entity.mediaSizeFormatted,
                    uploadProgress = 1.0f,
                    timestamp = entity.timestamp,
                    status = MessageStatus.SENDING,
                    replyToId = entity.replyToId,
                    replyToText = entity.replyToText,
                    replyToSenderName = entity.replyToSenderName,
                    isDeleted = entity.isDeleted
                )
                syncManager.publishChatMessage(roomId, msg) { success ->
                    scope.launch {
                        chatDao.updateMessageStatus(entity.id, if (success) MessageStatus.SENT.name else MessageStatus.FAILED.name)
                    }
                }
            }
        }
    }

    suspend fun deleteMessage(messageId: String) {
        deleteMessageForEveryone(messageId)
    }

    suspend fun deleteMessageForMe(messageId: String) {
        chatDao.deleteMessageById(messageId)
    }

    suspend fun deleteMessageForEveryone(messageId: String) {
        chatDao.markAsDeleted(messageId)
        val roomId = getActiveRoomId()
        if (roomId.isNotBlank()) {
            syncManager.publishMessageStatus(roomId, messageId, MessageStatus.FAILED)
        }
    }

    private fun getActiveRoomId(): String {
        val myCode = authRepository.currentUser.value?.pairCode ?: ""
        val prefs = context.getSharedPreferences("lik_pairing_prefs", Context.MODE_PRIVATE)
        val partnerCode = prefs.getString("partner_pair_code", "") ?: ""
        if (myCode.isNotBlank() && partnerCode.isNotBlank()) {
            val cleanA = myCode.trim().uppercase().replace("-", "")
            val cleanB = partnerCode.trim().uppercase().replace("-", "")
            return "lik_room_" + listOf(cleanA, cleanB).sorted().joinToString("_")
        }
        return ""
    }

    private fun ChatMessage.toEntity(): ChatMessageEntity {
        return ChatMessageEntity(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            mediaType = mediaType.name,
            mediaUrl = mediaUrl,
            mediaSizeFormatted = mediaSizeFormatted,
            uploadProgress = uploadProgress,
            timestamp = timestamp,
            status = status.name,
            replyToId = replyToId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            isDeleted = isDeleted
        )
    }
}
