package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AuthProvider {
    GMAIL, PHONE
}

enum class MessageType {
    TEXT, VOICE, IMAGE, DOCUMENT, LOCATION, CONTACT
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

enum class CallType {
    AUDIO, VIDEO
}

enum class CallDirection {
    INCOMING, OUTGOING, MISSED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val avatarUrl: String = "",
    val bio: String = "Hey there! I am using Pulse.",
    val authProvider: String = AuthProvider.GMAIL.name,
    val createdAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val avatarUrl: String,
    val bio: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val customStatus: String = ""
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String, // "me" or contactId
    val senderName: String,
    val messageType: String = MessageType.TEXT.name,
    val textContent: String? = null,
    val mediaUri: String? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long = 0L,
    val voiceDurationSeconds: Int = 0,
    val voiceWaveform: String = "", // comma-separated amplitudes
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationTitle: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = MessageStatus.SENT.name,
    val reaction: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val contactAvatar: String,
    val callType: String = CallType.AUDIO.name,
    val direction: String = CallDirection.OUTGOING.name,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
