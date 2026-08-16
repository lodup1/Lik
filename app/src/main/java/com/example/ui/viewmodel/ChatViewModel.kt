package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            chatRepository.allMessages
        } else {
            chatRepository.searchMessages(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isPartnerTyping: StateFlow<Boolean> = chatRepository.isPartnerTyping
    val partnerOnlineStatus: StateFlow<Boolean> = chatRepository.partnerOnlineStatus

    private val _selectedReplyMessage = MutableStateFlow<ChatMessage?>(null)
    val selectedReplyMessage: StateFlow<ChatMessage?> = _selectedReplyMessage.asStateFlow()

    private var typingJob: Job? = null

    fun onUserTyping(text: String) {
        typingJob?.cancel()
        if (text.isNotBlank()) {
            typingJob = viewModelScope.launch {
                chatRepository.setPartnerTyping(true)
                delay(3000)
                chatRepository.setPartnerTyping(false)
            }
        } else {
            chatRepository.setPartnerTyping(false)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    fun setReplyMessage(msg: ChatMessage?) {
        _selectedReplyMessage.value = msg
    }

    fun sendMessage(
        text: String,
        mediaType: MediaType = MediaType.NONE,
        mediaUrl: String? = null,
        mediaSizeFormatted: String? = null
    ) {
        if (text.isBlank() && mediaType == MediaType.NONE) return

        val reply = _selectedReplyMessage.value
        _selectedReplyMessage.value = null
        typingJob?.cancel()
        chatRepository.setPartnerTyping(false)

        viewModelScope.launch {
            chatRepository.sendMessage(
                text = text.trim(),
                mediaType = mediaType,
                mediaUrl = mediaUrl,
                mediaSizeFormatted = mediaSizeFormatted,
                replyToMsg = reply
            )
        }
    }

    fun sendPhotoAttachment(photoUrl: String) {
        sendMessage(
            text = "📷 Photo",
            mediaType = MediaType.IMAGE,
            mediaUrl = photoUrl,
            mediaSizeFormatted = "2.4 MB"
        )
    }

    fun sendVideoAttachment(videoUrl: String) {
        sendMessage(
            text = "🎥 Video",
            mediaType = MediaType.VIDEO,
            mediaUrl = videoUrl,
            mediaSizeFormatted = "8.1 MB"
        )
    }

    fun sendVoiceNoteAttachment() {
        sendMessage(
            text = "🎙️ Voice note (0:12)",
            mediaType = MediaType.VOICE,
            mediaUrl = "https://example.com/audio/voicenote.mp3",
            mediaSizeFormatted = "320 KB"
        )
    }

    fun deleteMessage(messageId: String) {
        deleteMessageForEveryone(messageId)
    }

    fun deleteMessageForMe(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessageForMe(messageId)
        }
    }

    fun deleteMessageForEveryone(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessageForEveryone(messageId)
        }
    }

    fun forwardMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                text = message.text,
                mediaType = message.mediaType,
                mediaUrl = message.mediaUrl,
                mediaSizeFormatted = message.mediaSizeFormatted
            )
        }
    }

    fun retryUpload(messageId: String) {
        viewModelScope.launch {
            chatRepository.retryFailedUpload(messageId)
        }
    }
}
