package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GoalItem
import com.example.data.model.Message
import com.example.data.model.MessageType
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    val messages: StateFlow<List<Message>> = chatRepository.messages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalItem>> = chatRepository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(text: String, receiverId: String = "partner", type: MessageType = MessageType.TEXT, mediaUri: String? = null) {
        if (text.isBlank() && mediaUri == null) return
        viewModelScope.launch {
            chatRepository.sendMessage(text, receiverId, type, mediaUri)
        }
    }

    fun simulatePartnerReply(reply: String, partnerName: String = "Partner") {
        viewModelScope.launch {
            chatRepository.receivePartnerMessage(reply, partnerName)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            chatRepository.addReaction(messageId, emoji)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            chatRepository.markAllAsRead()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    fun addGoal(title: String, category: String = "Couple Goal", targetDate: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            chatRepository.addGoal(title, category, targetDate)
        }
    }

    fun toggleGoal(goal: GoalItem) {
        viewModelScope.launch {
            chatRepository.toggleGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalItem) {
        viewModelScope.launch {
            chatRepository.deleteGoal(goal)
        }
    }
}
