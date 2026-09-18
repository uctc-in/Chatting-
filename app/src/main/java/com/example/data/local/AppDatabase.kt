package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallDirection
import com.example.data.model.CallLogEntity
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        CallLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun callLogDao(): CallLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulse_comm_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getDatabase(context))
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val contactDao = database.contactDao()
    val conversationDao = database.conversationDao()
    val messageDao = database.messageDao()
    val callLogDao = database.callLogDao()

    val contacts = listOf(
        ContactEntity(
            id = "c1",
            name = "Sarah Jenkins",
            email = "sarah.jenkins@designlab.io",
            phone = "+1 (555) 234-8901",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            bio = "Product Designer @ Figma Community ✨",
            isOnline = true,
            lastSeen = "Online",
            customStatus = "Designing the new design system 🎨"
        ),
        ContactEntity(
            id = "c2",
            name = "Alex Rivera",
            email = "alex.rivera@techcorp.dev",
            phone = "+1 (555) 432-1098",
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            bio = "Mobile Architect & Kotlin Enthusiast 🚀",
            isOnline = true,
            lastSeen = "Online",
            customStatus = "Coding in Kotlin Multiplatform"
        ),
        ContactEntity(
            id = "c3",
            name = "Elena Rostova",
            email = "elena.r@quantum.ai",
            phone = "+44 7911 123456",
            avatarUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150",
            bio = "AI Research Scientist | Audio DSP 🎧",
            isOnline = false,
            lastSeen = "12m ago",
            customStatus = "Reviewing speech synthesis papers"
        ),
        ContactEntity(
            id = "c4",
            name = "Marcus Vance",
            email = "m.vance@soundwave.fm",
            phone = "+1 (555) 876-5432",
            avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
            bio = "Podcast Producer & Sound Engineer 🎙️",
            isOnline = false,
            lastSeen = "2h ago",
            customStatus = "In recording studio"
        ),
        ContactEntity(
            id = "c5",
            name = "Maya Lin",
            email = "maya.lin@tokyo.net",
            phone = "+81 90 1234 5678",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            bio = "Photographer & Digital Nomad 🌸",
            isOnline = true,
            lastSeen = "Online",
            customStatus = "Exploring Shibuya 📸"
        )
    )
    contactDao.insertContacts(contacts)

    val now = System.currentTimeMillis()

    // Conversations
    val conv1 = ConversationEntity(
        id = "conv_c1",
        contactId = "c1",
        lastMessage = "Sent you the audio sample and the project brief!",
        lastMessageTime = now - 1000 * 60 * 3,
        unreadCount = 2,
        isPinned = true
    )
    val conv2 = ConversationEntity(
        id = "conv_c2",
        contactId = "c2",
        lastMessage = "Voice message (0:14)",
        lastMessageTime = now - 1000 * 60 * 25,
        unreadCount = 0,
        isPinned = true
    )
    val conv3 = ConversationEntity(
        id = "conv_c3",
        contactId = "c3",
        lastMessage = "Shared location: Quantum AI Lab",
        lastMessageTime = now - 1000 * 60 * 120,
        unreadCount = 0,
        isPinned = false
    )
    val conv4 = ConversationEntity(
        id = "conv_c5",
        contactId = "c5",
        lastMessage = "Check out the photo from Shibuya crossing!",
        lastMessageTime = now - 1000 * 60 * 60 * 5,
        unreadCount = 0,
        isPinned = false
    )
    conversationDao.insertConversation(conv1)
    conversationDao.insertConversation(conv2)
    conversationDao.insertConversation(conv3)
    conversationDao.insertConversation(conv4)

    // Messages for conv_c1
    val messagesC1 = listOf(
        MessageEntity(
            id = "m1_1",
            conversationId = "conv_c1",
            senderId = "c1",
            senderName = "Sarah Jenkins",
            messageType = MessageType.TEXT.name,
            textContent = "Hey! Have you seen the latest mobile UI mockup we worked on?",
            timestamp = now - 1000 * 60 * 15,
            status = MessageStatus.READ.name
        ),
        MessageEntity(
            id = "m1_2",
            conversationId = "conv_c1",
            senderId = "me",
            senderName = "Me",
            messageType = MessageType.TEXT.name,
            textContent = "Yes, absolutely! The smooth animations and transitions look incredible.",
            timestamp = now - 1000 * 60 * 12,
            status = MessageStatus.READ.name,
            reaction = "❤️"
        ),
        MessageEntity(
            id = "m1_3",
            conversationId = "conv_c1",
            senderId = "c1",
            senderName = "Sarah Jenkins",
            messageType = MessageType.VOICE.name,
            textContent = "Voice note",
            voiceDurationSeconds = 18,
            voiceWaveform = "30,55,80,45,70,95,60,40,75,90,50,30,85,60,40,65,90,45",
            timestamp = now - 1000 * 60 * 8,
            status = MessageStatus.DELIVERED.name
        ),
        MessageEntity(
            id = "m1_4",
            conversationId = "conv_c1",
            senderId = "c1",
            senderName = "Sarah Jenkins",
            messageType = MessageType.DOCUMENT.name,
            textContent = "Pulse_Communication_Design_System_v2.pdf",
            fileName = "Pulse_Communication_Design_System_v2.pdf",
            fileSizeBytes = 4_820_000L,
            timestamp = now - 1000 * 60 * 3,
            status = MessageStatus.DELIVERED.name
        )
    )

    // Messages for conv_c2
    val messagesC2 = listOf(
        MessageEntity(
            id = "m2_1",
            conversationId = "conv_c2",
            senderId = "me",
            senderName = "Me",
            messageType = MessageType.TEXT.name,
            textContent = "Hey Alex, are we having our voice & video call sync today?",
            timestamp = now - 1000 * 60 * 45,
            status = MessageStatus.READ.name
        ),
        MessageEntity(
            id = "m2_2",
            conversationId = "conv_c2",
            senderId = "c2",
            senderName = "Alex Rivera",
            messageType = MessageType.VOICE.name,
            textContent = "Voice message",
            voiceDurationSeconds = 14,
            voiceWaveform = "40,65,70,85,95,75,60,45,80,90,55,40,70,85",
            timestamp = now - 1000 * 60 * 25,
            status = MessageStatus.READ.name,
            reaction = "👍"
        )
    )

    // Messages for conv_c3
    val messagesC3 = listOf(
        MessageEntity(
            id = "m3_1",
            conversationId = "conv_c3",
            senderId = "c3",
            senderName = "Elena Rostova",
            messageType = MessageType.LOCATION.name,
            textContent = "Quantum AI Lab Campus",
            locationTitle = "Quantum AI Lab, Silicon Avenue 42",
            latitude = 37.7749,
            longitude = -122.4194,
            timestamp = now - 1000 * 60 * 120,
            status = MessageStatus.READ.name
        )
    )

    // Messages for conv_c5
    val messagesC5 = listOf(
        MessageEntity(
            id = "m5_1",
            conversationId = "conv_c5",
            senderId = "c5",
            senderName = "Maya Lin",
            messageType = MessageType.IMAGE.name,
            textContent = "Shibuya Crossing lights tonight!",
            mediaUri = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=600",
            fileName = "shibuya_night.jpg",
            fileSizeBytes = 2_150_000L,
            timestamp = now - 1000 * 60 * 60 * 5,
            status = MessageStatus.READ.name,
            reaction = "🔥"
        )
    )

    for (m in messagesC1) messageDao.insertMessage(m)
    for (m in messagesC2) messageDao.insertMessage(m)
    for (m in messagesC3) messageDao.insertMessage(m)
    for (m in messagesC5) messageDao.insertMessage(m)

    // Call logs
    val sampleCallLogs = listOf(
        CallLogEntity(
            id = "cl_1",
            contactId = "c1",
            contactName = "Sarah Jenkins",
            contactAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            callType = CallType.VIDEO.name,
            direction = CallDirection.INCOMING.name,
            timestamp = now - 1000 * 60 * 95,
            durationSeconds = 482
        ),
        CallLogEntity(
            id = "cl_2",
            contactId = "c2",
            contactName = "Alex Rivera",
            contactAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            callType = CallType.AUDIO.name,
            direction = CallDirection.OUTGOING.name,
            timestamp = now - 1000 * 60 * 60 * 3,
            durationSeconds = 195
        ),
        CallLogEntity(
            id = "cl_3",
            contactId = "c4",
            contactName = "Marcus Vance",
            contactAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
            callType = CallType.AUDIO.name,
            direction = CallDirection.MISSED.name,
            timestamp = now - 1000 * 60 * 60 * 18,
            durationSeconds = 0
        )
    )
    for (cl in sampleCallLogs) callLogDao.insertCallLog(cl)
}
