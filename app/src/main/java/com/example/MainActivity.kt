package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.UserRepository
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.call.CallScreen
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.theme.PulseTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CallViewModel
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val userRepository = UserRepository(database)
        val chatRepository = ChatRepository(database)
        val callRepository = CallRepository(database)

        setContent {
            PulseTheme {
                // Request Audio & Camera permissions gracefully on start
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions handled */ }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CAMERA
                        )
                    )
                }

                PulseApp(
                    userRepository = userRepository,
                    chatRepository = chatRepository,
                    callRepository = callRepository
                )
            }
        }
    }
}

@Composable
fun PulseApp(
    userRepository: UserRepository,
    chatRepository: ChatRepository,
    callRepository: CallRepository
) {
    val authViewModel: AuthViewModel = viewModel { AuthViewModel(userRepository) }
    val chatViewModel: ChatViewModel = viewModel { ChatViewModel(chatRepository) }
    val callViewModel: CallViewModel = viewModel { CallViewModel(callRepository) }

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val activeCall by callViewModel.activeCall.collectAsStateWithLifecycle()
    val conversations by chatViewModel.conversations.collectAsStateWithLifecycle()
    val contacts by chatViewModel.contacts.collectAsStateWithLifecycle()
    val callLogs by callViewModel.callLogs.collectAsStateWithLifecycle()
    val storageStats by chatViewModel.storageStats.collectAsStateWithLifecycle()

    val activeConversationId by chatViewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeContact by chatViewModel.activeContact.collectAsStateWithLifecycle()
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isPeerTyping by chatViewModel.isPeerTyping.collectAsStateWithLifecycle()

    var isViewingChat by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Fullscreen Active Call Overlay takes highest priority
        if (activeCall != null) {
            CallScreen(
                callState = activeCall!!,
                onMuteToggle = { callViewModel.toggleMute() },
                onSpeakerToggle = { callViewModel.toggleSpeaker() },
                onVideoToggle = { callViewModel.toggleVideo() },
                onSwitchCamera = { callViewModel.switchCamera() },
                onEndCall = { callViewModel.endCall() }
            )
        } else if (currentUser == null) {
            // Auth Screen
            AuthScreen(
                onSignInSuccess = {},
                onSignInWithGmail = { email, name, avatar ->
                    authViewModel.signInWithGmail(email, name, avatar)
                },
                onSignInWithPhone = { phone, name ->
                    authViewModel.signInWithPhone(phone, name)
                }
            )
        } else {
            // Main App Navigation
            AnimatedContent(
                targetState = isViewingChat && activeContact != null,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "navigation"
            ) { viewingChat ->
                if (viewingChat && activeContact != null) {
                    ChatDetailScreen(
                        contact = activeContact,
                        messages = messages,
                        isPeerTyping = isPeerTyping,
                        onBack = { isViewingChat = false },
                        onStartCall = { contact, type ->
                            callViewModel.startCall(contact, type)
                        },
                        onSendText = { text, replyText, replySender ->
                            chatViewModel.sendText(text, replyText, replySender)
                        },
                        onSendVoice = { durationSec, waveform ->
                            chatViewModel.sendVoice(durationSec, waveform)
                        },
                        onSendImage = { uri, caption ->
                            chatViewModel.sendImage(uri, caption)
                        },
                        onSendDocument = { name, size ->
                            chatViewModel.sendDocument(name, size)
                        },
                        onSendLocation = { title, lat, lng ->
                            chatViewModel.sendLocation(title, lat, lng)
                        },
                        onSendContact = { name, phone ->
                            chatViewModel.sendContact(name, phone)
                        },
                        onReactionSelect = { msgId, currentReaction, reaction ->
                            chatViewModel.toggleReaction(msgId, currentReaction, reaction)
                        }
                    )
                } else {
                    HomeScreen(
                        currentUser = currentUser,
                        conversations = conversations,
                        contacts = contacts,
                        callLogs = callLogs,
                        storageStats = storageStats,
                        onSelectConversation = { convId, contactId ->
                            chatViewModel.openConversation(convId, contactId)
                            isViewingChat = true
                        },
                        onSelectContactForChat = { contact ->
                            chatViewModel.openChatWithContact(contact)
                            isViewingChat = true
                        },
                        onStartCall = { contact, type ->
                            callViewModel.startCall(contact, type)
                        },
                        onClearCallLogs = {
                            callViewModel.clearCallLogs()
                        },
                        onUpdateProfile = { name, bio, avatar ->
                            authViewModel.updateProfile(name, bio, avatar)
                        },
                        onSignOut = {
                            isViewingChat = false
                            authViewModel.signOut()
                        },
                        onAddNewContact = { name, phone ->
                            chatViewModel.addNewContact(name, phone)
                            isViewingChat = true
                        }
                    )
                }
            }
        }
    }
}
