package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.repository.ChatRepository
import com.example.data.repository.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    val conversations: StateFlow<List<ConversationEntity>> = chatRepository.conversationsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val contacts: StateFlow<List<ContactEntity>> = chatRepository.contactsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeContact = MutableStateFlow<ContactEntity?>(null)
    val activeContact: StateFlow<ContactEntity?> = _activeContact.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()

    private val _isPeerTyping = MutableStateFlow(false)
    val isPeerTyping: StateFlow<Boolean> = _isPeerTyping.asStateFlow()

    init {
        refreshStorageStats()
    }

    fun openConversation(conversationId: String, contactId: String) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            _activeContact.value = chatRepository.getContact(contactId)
            chatRepository.markConversationRead(conversationId)
            chatRepository.getMessages(conversationId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun openChatWithContact(contact: ContactEntity) {
        viewModelScope.launch {
            val conv = chatRepository.createOrGetConversation(contact)
            openConversation(conv.id, contact.id)
        }
    }

    fun sendText(text: String, replyToText: String? = null, replyToSender: String? = null) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendTextMessage(
                conversationId = convId,
                contactId = contact.id,
                contactName = contact.name,
                text = text,
                replyToText = replyToText,
                replyToSender = replyToSender
            )
            refreshStorageStats()

            // Peer typing simulation and reply
            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.TEXT)
            _isPeerTyping.value = false
        }
    }

    fun sendVoice(durationSeconds: Int, waveform: String) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendVoiceMessage(
                conversationId = convId,
                durationSeconds = durationSeconds,
                waveform = waveform
            )
            refreshStorageStats()

            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.VOICE)
            _isPeerTyping.value = false
        }
    }

    fun sendImage(uri: String, caption: String) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendDataMediaMessage(
                conversationId = convId,
                type = MessageType.IMAGE,
                uri = uri,
                caption = caption,
                fileSizeBytes = 2_100_000L
            )
            refreshStorageStats()

            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.IMAGE)
            _isPeerTyping.value = false
        }
    }

    fun sendDocument(fileName: String, sizeBytes: Long) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendDataMediaMessage(
                conversationId = convId,
                type = MessageType.DOCUMENT,
                fileName = fileName,
                fileSizeBytes = sizeBytes
            )
            refreshStorageStats()

            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.DOCUMENT)
            _isPeerTyping.value = false
        }
    }

    fun sendLocation(title: String, lat: Double, lng: Double) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendDataMediaMessage(
                conversationId = convId,
                type = MessageType.LOCATION,
                locationTitle = title,
                lat = lat,
                lng = lng
            )
            refreshStorageStats()

            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.LOCATION)
            _isPeerTyping.value = false
        }
    }

    fun sendContact(name: String, phone: String) {
        val convId = _activeConversationId.value ?: return
        val contact = _activeContact.value ?: return
        viewModelScope.launch {
            chatRepository.sendDataMediaMessage(
                conversationId = convId,
                type = MessageType.CONTACT,
                caption = "$name ($phone)"
            )
            refreshStorageStats()

            _isPeerTyping.value = true
            chatRepository.simulatePeerReply(convId, contact, MessageType.CONTACT)
            _isPeerTyping.value = false
        }
    }

    fun toggleReaction(messageId: String, currentReaction: String?, reaction: String) {
        viewModelScope.launch {
            chatRepository.toggleReaction(messageId, currentReaction, reaction)
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            _storageStats.value = chatRepository.getStorageStats()
        }
    }

    fun addNewContact(name: String, phone: String) {
        viewModelScope.launch {
            val contact = chatRepository.createContact(name, phone)
            openChatWithContact(contact)
        }
    }
}
