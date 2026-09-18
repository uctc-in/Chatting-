package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.ReceivedBubbleColorDark
import com.example.ui.theme.ReceivedBubbleColorLight
import com.example.ui.theme.SentBubbleColorDark
import com.example.ui.theme.SentBubbleColorLight
import com.example.ui.theme.SkyPrimary
import com.example.ui.theme.WhatsAppBlueTicks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    isMe: Boolean,
    playbackManager: AudioPlaybackManager,
    onReactionSelect: (String) -> Unit,
    onReplySelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReactionMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleColor = if (isMe) {
        if (isDark) SentBubbleColorDark else SentBubbleColorLight
    } else {
        if (isDark) ReceivedBubbleColorDark else ReceivedBubbleColorLight
    }

    val textColor = if (isDark) {
        Color(0xFFE9EDEF)
    } else {
        Color(0xFF111B21)
    }

    val metaTextColor = if (isDark) {
        Color(0xFF8696A0)
    } else {
        Color(0xFF667781)
    }

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Quick reaction popup
        AnimatedVisibility(
            visible = showReactionMenu,
            enter = fadeIn() + scaleIn(animationSpec = tween(200, easing = FastOutSlowInEasing))
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("❤️", "👍", "🔥", "😂", "😮", "🙏").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onReactionSelect(emoji)
                                    showReactionMenu = false
                                }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }

        // The main bubble
        Surface(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .combinedClickable(
                    onClick = { /* normal tap */ },
                    onLongClick = { showReactionMenu = !showReactionMenu }
                )
                .animateContentSize(),
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = if (isMe) 2.dp else 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Quoted reply if any
                if (!message.replyToText.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isMe) Color.Black.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
                            .padding(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .background(if (isMe) Color.White else MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = message.replyToSender ?: "Reply",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.replyToText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = textColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Render based on message type
                when (message.messageType) {
                    MessageType.TEXT.name -> {
                        Text(
                            text = message.textContent ?: "",
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }

                    MessageType.VOICE.name -> {
                        VoiceMessageBubbleContent(
                            message = message,
                            isMe = isMe,
                            textColor = textColor,
                            playbackManager = playbackManager,
                            onPlayToggle = {
                                playbackManager.togglePlay(message.id, message.voiceDurationSeconds, scope)
                            }
                        )
                    }

                    MessageType.IMAGE.name -> {
                        Column {
                            if (!message.mediaUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(message.mediaUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image message",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                            if (!message.textContent.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.textContent,
                                    color = textColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    MessageType.DOCUMENT.name -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isMe) Color.White.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (isMe) Color.White.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Document",
                                    tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName ?: "Document.pdf",
                                    color = textColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = formatFileSize(message.fileSizeBytes),
                                    color = metaTextColor,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    MessageType.LOCATION.name -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFF4338CA))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = "Live Location",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.locationTitle ?: "Shared Location",
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            if (message.latitude != null && message.longitude != null) {
                                Text(
                                    text = "Lat: ${"%.4f".format(message.latitude)}, Lng: ${"%.4f".format(message.longitude)}",
                                    color = metaTextColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    MessageType.CONTACT.name -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isMe) Color.White.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(EmeraldTertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Contact",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = message.textContent ?: "Shared Contact",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Tap to view contact",
                                    color = metaTextColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Bubble Footer: Timestamp & status ticks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeString,
                        color = metaTextColor,
                        fontSize = 10.sp
                    )

                    if (isMe) {
                        when (message.status) {
                            MessageStatus.READ.name -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = WhatsAppBlueTicks,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            MessageStatus.DELIVERED.name -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = metaTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = metaTextColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reaction badge attached to corner
        if (!message.reaction.isNullOrBlank()) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMe) 0.dp else 8.dp, end = if (isMe) 8.dp else 0.dp)
                    .clip(CircleShape)
                    .combinedClickable { showReactionMenu = true }
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
            ) {
                Text(
                    text = message.reaction,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceMessageBubbleContent(
    message: MessageEntity,
    isMe: Boolean,
    textColor: Color,
    playbackManager: AudioPlaybackManager,
    onPlayToggle: () -> Unit
) {
    val isPlaying = playbackManager.playingMessageId == message.id
    val progress = if (isPlaying) playbackManager.playbackProgress else 0f

    val playedColor = if (isMe) Color.White else MaterialTheme.colorScheme.primary
    val unplayedColor = if (isMe) Color.White.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Play / Pause Circle
        IconButton(
            onClick = onPlayToggle,
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (isMe) Color.White.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Waveform bar
            AudioWaveform(
                waveformString = message.voiceWaveform,
                progress = progress,
                playedColor = playedColor,
                unplayedColor = unplayedColor,
                barHeight = 28.dp,
                onSeek = { seekFrac ->
                    if (isPlaying) {
                        playbackManager.seekTo(seekFrac)
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentSec = if (isPlaying) {
                    ((message.voiceDurationSeconds * progress).toInt())
                } else {
                    message.voiceDurationSeconds
                }
                Text(
                    text = "%d:%02d".format(currentSec / 60, currentSec % 60),
                    fontSize = 11.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                // Speed button if playing
                if (isPlaying) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { playbackManager.cycleSpeed() }
                    ) {
                        Text(
                            text = "${playbackManager.playbackSpeed}x",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "1.2 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        "%.1f MB".format(mb)
    } else {
        "%.0f KB".format(kb)
    }
}
