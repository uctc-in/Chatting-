package com.example.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogEntity
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.UserEntity
import com.example.data.repository.StorageStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    conversations: List<ConversationEntity>,
    contacts: List<ContactEntity>,
    callLogs: List<CallLogEntity>,
    storageStats: StorageStats?,
    onSelectConversation: (conversationId: String, contactId: String) -> Unit,
    onSelectContactForChat: (ContactEntity) -> Unit,
    onStartCall: (ContactEntity, CallType) -> Unit,
    onClearCallLogs: () -> Unit,
    onUpdateProfile: (name: String, bio: String, avatarUrl: String) -> Unit,
    onSignOut: () -> Unit,
    onAddNewContact: ((name: String, phone: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val totalUnread = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Pulse"
                            1 -> "Calls"
                            2 -> "Contacts"
                            else -> "My Profile & Data"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Chats
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        if (totalUnread > 0) {
                            BadgedBox(
                                badge = {
                                    Badge { Text(totalUnread.toString()) }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.Chat else Icons.Outlined.Chat,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = { Text("Chats") },
                    modifier = Modifier.testTag("nav_chats")
                )

                // Calls
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Call else Icons.Outlined.Call,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls") },
                    modifier = Modifier.testTag("nav_calls")
                )

                // Contacts
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts") },
                    modifier = Modifier.testTag("nav_contacts")
                )

                // Profile
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { selectedTab = 2 },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "New Chat"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ChatListScreen(
                    conversations = conversations,
                    contacts = contacts,
                    onSelectConversation = onSelectConversation,
                    onSelectContactForChat = onSelectContactForChat
                )
                1 -> CallsTabScreen(
                    callLogs = callLogs,
                    contacts = contacts,
                    onStartCall = onStartCall,
                    onClearLogs = onClearCallLogs
                )
                2 -> ContactsTabScreen(
                    contacts = contacts,
                    onSelectContactForChat = onSelectContactForChat,
                    onStartCall = onStartCall,
                    onAddNewContact = onAddNewContact
                )
                3 -> ProfileTabScreen(
                    user = currentUser,
                    storageStats = storageStats,
                    onUpdateProfile = onUpdateProfile,
                    onSignOut = onSignOut
                )
            }
        }
    }
}
