package com.example.data.repository

import com.example.data.local.GoalDao
import com.example.data.local.GoalEntity
import com.example.data.local.MessageDao
import com.example.data.local.MessageEntity
import com.example.data.model.GoalItem
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.notification.LikNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChatRepository(
    private val messageDao: MessageDao,
    private val goalDao: GoalDao,
    private val notificationManager: LikNotificationManager
) {

    val messages: Flow<List<Message>> = messageDao.getAllMessages().map { entities ->
        entities.map { entity ->
            Message(
                id = entity.id,
                senderId = entity.senderId,
                receiverId = entity.receiverId,
                text = entity.text,
                timestamp = entity.timestamp,
                isRead = entity.isRead,
                messageType = try {
                    MessageType.valueOf(entity.messageType)
                } catch (e: Exception) {
                    MessageType.TEXT
                },
                mediaUri = entity.mediaUri,
                reaction = entity.reaction
            )
        }
    }

    val goals: Flow<List<GoalItem>> = goalDao.getAllGoals().map { entities ->
        entities.map { entity ->
            GoalItem(
                id = entity.id,
                title = entity.title,
                isCompleted = entity.isCompleted,
                targetDate = entity.targetDate,
                category = entity.category
            )
        }
    }

    suspend fun sendMessage(
        text: String,
        receiverId: String,
        type: MessageType = MessageType.TEXT,
        mediaUri: String? = null
    ) {
        val entity = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = "current_user",
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            messageType = type.name,
            mediaUri = mediaUri,
            reaction = null
        )
        messageDao.insertMessage(entity)
    }

    suspend fun receivePartnerMessage(text: String, senderName: String = "Partner") {
        val entity = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = "partner",
            receiverId = "current_user",
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            messageType = MessageType.TEXT.name,
            mediaUri = null,
            reaction = null
        )
        messageDao.insertMessage(entity)
        notificationManager.showMessageNotification(senderName, text)
    }

    suspend fun addReaction(messageId: String, reaction: String) {
        messageDao.updateReaction(messageId, reaction)
    }

    suspend fun markAllAsRead() {
        messageDao.markAllAsRead()
    }

    suspend fun clearHistory() {
        messageDao.clearMessages()
    }

    suspend fun addGoal(title: String, category: String, targetDate: String?) {
        val entity = GoalEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            isCompleted = false,
            targetDate = targetDate,
            category = category
        )
        goalDao.insertGoal(entity)
    }

    suspend fun toggleGoal(goal: GoalItem) {
        val entity = GoalEntity(
            id = goal.id,
            title = goal.title,
            isCompleted = !goal.isCompleted,
            targetDate = goal.targetDate,
            category = goal.category
        )
        goalDao.updateGoal(entity)
    }

    suspend fun deleteGoal(goal: GoalItem) {
        val entity = GoalEntity(
            id = goal.id,
            title = goal.title,
            isCompleted = goal.isCompleted,
            targetDate = goal.targetDate,
            category = goal.category
        )
        goalDao.deleteGoal(entity)
    }
}
