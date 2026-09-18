package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class StorageStats(
    val totalMessages: Int,
    val totalVoiceNotes: Int,
    val totalMedia: Int,
    val estimatedDataUsageMb: Double
)

class ChatRepository(private val database: AppDatabase) {
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val contactDao = database.contactDao()

    val conversationsFlow: Flow<List<ConversationEntity>> = conversationDao.getConversationsFlow()
    val contactsFlow: Flow<List<ContactEntity>> = contactDao.getAllContactsFlow()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun getContact(contactId: String): ContactEntity? =
        contactDao.getContactById(contactId)

    suspend fun createContact(name: String, phone: String, email: String = ""): ContactEntity {
        val newContact = ContactEntity(
            id = "c_${UUID.randomUUID().toString().take(8)}",
            name = name,
            email = email,
            phone = phone,
            avatarUrl = "",
            bio = "Hey there! I am using Pulse.",
            isOnline = true,
            lastSeen = "Online",
            customStatus = "Available"
        )
        contactDao.insertContact(newContact)
        return newContact
    }

    suspend fun createOrGetConversation(contact: ContactEntity): ConversationEntity {
        val existingId = "conv_${contact.id}"
        val existing = conversationDao.getConversationById(existingId)
        if (existing != null) return existing

        val newConv = ConversationEntity(
            id = existingId,
            contactId = contact.id,
            lastMessage = "Started a conversation",
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = 0
        )
        conversationDao.insertConversation(newConv)
        return newConv
    }

    suspend fun markConversationRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun sendTextMessage(
        conversationId: String,
        contactId: String,
        contactName: String,
        text: String,
        replyToText: String? = null,
        replyToSender: String? = null
    ) {
        val message = MessageEntity(
            id = "msg_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = "me",
            senderName = "Me",
            messageType = MessageType.TEXT.name,
            textContent = text,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name,
            replyToText = replyToText,
            replyToSender = replyToSender
        )
        messageDao.insertMessage(message)
        updateConversationSnippet(conversationId, text)
    }

    suspend fun sendVoiceMessage(
        conversationId: String,
        durationSeconds: Int,
        waveform: String
    ) {
        val message = MessageEntity(
            id = "msg_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = "me",
            senderName = "Me",
            messageType = MessageType.VOICE.name,
            textContent = "Voice message ($durationSeconds s)",
            voiceDurationSeconds = durationSeconds,
            voiceWaveform = waveform,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name
        )
        messageDao.insertMessage(message)
        updateConversationSnippet(conversationId, "Voice message (${formatSeconds(durationSeconds)})")
    }

    suspend fun sendDataMediaMessage(
        conversationId: String,
        type: MessageType,
        uri: String? = null,
        fileName: String? = null,
        fileSizeBytes: Long = 0L,
        caption: String? = null,
        locationTitle: String? = null,
        lat: Double? = null,
        lng: Double? = null
    ) {
        val snippet = when (type) {
            MessageType.IMAGE -> caption?.ifBlank { "Photo" } ?: "Photo"
            MessageType.DOCUMENT -> fileName ?: "Document"
            MessageType.LOCATION -> locationTitle ?: "Location shared"
            MessageType.CONTACT -> "Contact card"
            else -> "Media file"
        }

        val message = MessageEntity(
            id = "msg_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = "me",
            senderName = "Me",
            messageType = type.name,
            textContent = caption ?: snippet,
            mediaUri = uri,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            locationTitle = locationTitle,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name
        )
        messageDao.insertMessage(message)
        updateConversationSnippet(conversationId, snippet)
    }

    suspend fun toggleReaction(messageId: String, currentReaction: String?, newReaction: String) {
        val finalReaction = if (currentReaction == newReaction) null else newReaction
        messageDao.updateReaction(messageId, finalReaction)
    }

    suspend fun simulatePeerReply(conversationId: String, contact: ContactEntity, triggerType: MessageType) {
        delay(1200) // Realistic typing delay

        val replyContent = when (triggerType) {
            MessageType.VOICE -> "Listened to your voice message! Loud and clear. Let's do that 👍"
            MessageType.IMAGE -> "Awesome picture! Thanks for sharing."
            MessageType.DOCUMENT -> "Got the file! Reviewing it now."
            MessageType.LOCATION -> "Great, I know that spot. See you there soon!"
            else -> listOf(
                "Got your message! Thanks for reaching out.",
                "Sounds perfect, let's sync up on call soon.",
                "Received! Let me check on my end and get back to you.",
                "Awesome! Everything is functioning smoothly."
            ).random()
        }

        val replyMsg = MessageEntity(
            id = "msg_${UUID.randomUUID()}",
            conversationId = conversationId,
            senderId = contact.id,
            senderName = contact.name,
            messageType = MessageType.TEXT.name,
            textContent = replyContent,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.READ.name
        )
        messageDao.insertMessage(replyMsg)
        updateConversationSnippet(conversationId, replyContent)
    }

    private suspend fun updateConversationSnippet(conversationId: String, snippet: String) {
        val conv = conversationDao.getConversationById(conversationId)
        if (conv != null) {
            conversationDao.updateConversation(
                conv.copy(
                    lastMessage = snippet,
                    lastMessageTime = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun getStorageStats(): StorageStats {
        val totalMsgs = messageDao.getMessageCount()
        val totalVoice = messageDao.getVoiceCount()
        val totalMedia = messageDao.getMediaCount()
        val estimatedMb = (totalMsgs * 0.002) + (totalVoice * 0.15) + (totalMedia * 1.8)
        return StorageStats(
            totalMessages = totalMsgs,
            totalVoiceNotes = totalVoice,
            totalMedia = totalMedia,
            estimatedDataUsageMb = String.format("%.2f", estimatedMb).toDoubleOrNull() ?: 1.2
        )
    }

    private fun formatSeconds(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%d:%02d".format(m, s)
    }
}
