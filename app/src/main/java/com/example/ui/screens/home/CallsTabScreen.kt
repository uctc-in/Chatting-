package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallDirection
import com.example.data.model.CallLogEntity
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.ErrorRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsTabScreen(
    callLogs: List<CallLogEntity>,
    contacts: List<ContactEntity>,
    onStartCall: (contact: ContactEntity, callType: CallType) -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contactMap = remember(contacts) { contacts.associateBy { it.id } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quick Action: Start Call Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HD Voice & Video Calls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Connect with contacts with real audio & video preview",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call first online contact
                    val targetContact = contacts.firstOrNull() ?: ContactEntity(
                        id = "c1",
                        name = "Sarah Jenkins",
                        email = "sarah.jenkins@designlab.io",
                        phone = "+1 555-234-8901",
                        avatarUrl = "",
                        bio = "",
                        isOnline = true,
                        lastSeen = "Online"
                    )

                    IconButton(
                        onClick = { onStartCall(targetContact, CallType.AUDIO) },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .size(40.dp)
                            .testTag("quick_audio_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onStartCall(targetContact, CallType.VIDEO) },
                        modifier = Modifier
                            .background(EmeraldTertiary, CircleShape)
                            .size(40.dp)
                            .testTag("quick_video_call")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Header for Recent Calls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Calls",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (callLogs.isNotEmpty()) {
                IconButton(onClick = onClearLogs, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No call history yet",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap on any contact to make an audio or video call",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(callLogs, key = { it.id }) { log ->
                    val contact = contactMap[log.contactId] ?: ContactEntity(
                        id = log.contactId,
                        name = log.contactName,
                        email = "",
                        phone = "",
                        avatarUrl = log.contactAvatar,
                        bio = "",
                        isOnline = false,
                        lastSeen = ""
                    )

                    CallLogRowItem(
                        callLog = log,
                        onCallAgain = { type ->
                            onStartCall(contact, type)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallLogRowItem(
    callLog: CallLogEntity,
    onCallAgain: (CallType) -> Unit
) {
    val timeFormatted = remember(callLog.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(callLog.timestamp))
    }

    val isMissed = callLog.direction == CallDirection.MISSED.name
    val isIncoming = callLog.direction == CallDirection.INCOMING.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCallAgain(if (callLog.callType == CallType.VIDEO.name) CallType.VIDEO else CallType.AUDIO)
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarView(
            name = callLog.contactName,
            avatarUrl = callLog.contactAvatar,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callLog.contactName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isMissed) ErrorRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val dirIcon = when {
                    isMissed -> Icons.Default.CallMissed
                    isIncoming -> Icons.Default.CallReceived
                    else -> Icons.Default.CallMade
                }
                val dirColor = when {
                    isMissed -> ErrorRed
                    isIncoming -> EmeraldTertiary
                    else -> MaterialTheme.colorScheme.primary
                }

                Icon(
                    imageVector = dirIcon,
                    contentDescription = null,
                    tint = dirColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = timeFormatted,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (callLog.durationSeconds > 0) {
                    Text(
                        text = " • ${callLog.durationSeconds / 60}m ${callLog.durationSeconds % 60}s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Call back icon
        IconButton(
            onClick = {
                onCallAgain(if (callLog.callType == CallType.VIDEO.name) CallType.VIDEO else CallType.AUDIO)
            }
        ) {
            Icon(
                imageVector = if (callLog.callType == CallType.VIDEO.name) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Call back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
