package com.example.ui.screens.call

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.model.CallType
import com.example.ui.components.AvatarView
import com.example.ui.theme.EmeraldTertiary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.ActiveCallState
import com.example.ui.viewmodel.CallStatus

@Composable
fun CallScreen(
    callState: ActiveCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVideo = callState.callType == CallType.VIDEO && callState.isVideoEnabled
    val isConnected = callState.status == CallStatus.CONNECTED

    // Soundwave pulsing animation for audio call
    val infiniteTransition = rememberInfiniteTransition(label = "ring_waves")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "wave1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "waveAlpha1"
    )

    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.95f,
        animationSpec = infiniteRepeatable(tween(1400, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "wave2"
    )
    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "waveAlpha2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B141A),
                        Color(0xFF103429),
                        Color(0xFF0B141A)
                    )
                )
            )
    ) {
        // If Video mode: Render remote video background + PIP local camera preview
        if (isVideo) {
            // Simulated Remote video background
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = callState.contact.avatarUrl.ifBlank {
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800"
                    },
                    contentDescription = "Remote video stream",
                    modifier = Modifier.fillMaxSize()
                )
                // Dark subtle overlay for controls readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }

            // Local Camera PIP View in upper-right corner
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .align(Alignment.TopEnd)
                    .size(width = 110.dp, height = 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                CameraPreviewView(isFrontCamera = callState.isFrontCamera)
            }
        }

        // Main Caller Info & Ripples (for Audio or non-video mode)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = callState.contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    callState.status == CallStatus.RINGING -> "Ringing..."
                    isConnected -> {
                        val min = callState.durationSeconds / 60
                        val sec = callState.durationSeconds % 60
                        "%02d:%02d".format(min, sec)
                    }
                    else -> "Call ended"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isConnected) EmeraldTertiary else Color.White.copy(alpha = 0.75f)
            )

            if (!isVideo) {
                Spacer(modifier = Modifier.height(64.dp))

                // Pulsing Audio Avatar Ripples
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (callState.status == CallStatus.RINGING) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(waveScale2)
                                .background(WhatsAppGreen.copy(alpha = waveAlpha2), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .scale(waveScale1)
                                .background(WhatsAppGreen.copy(alpha = waveAlpha1), CircleShape)
                        )
                    }

                    AvatarView(
                        name = callState.contact.name,
                        avatarUrl = callState.contact.avatarUrl,
                        size = 140.dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callState.contact.phone.ifBlank { callState.contact.email },
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // In-Call Action Controls Dock at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.Black.copy(alpha = 0.65f),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    CallActionButton(
                        icon = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (callState.isMuted) "Unmute" else "Mute",
                        isActive = callState.isMuted,
                        onClick = onMuteToggle
                    )

                    // Speaker Button
                    CallActionButton(
                        icon = if (callState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        label = "Speaker",
                        isActive = callState.isSpeakerOn,
                        onClick = onSpeakerToggle
                    )

                    // Video Toggle Button
                    CallActionButton(
                        icon = if (callState.isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        label = "Video",
                        isActive = callState.isVideoEnabled,
                        onClick = onVideoToggle
                    )

                    // Flip Camera (only active if video enabled)
                    if (callState.isVideoEnabled) {
                        CallActionButton(
                            icon = Icons.Default.Cameraswitch,
                            label = "Flip",
                            isActive = false,
                            onClick = onSwitchCamera
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // End Call Button (Large Red)
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(68.dp)
                    .background(ErrorRed, CircleShape)
                    .testTag("end_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isActive) Color.White else Color.White.copy(alpha = 0.2f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun CameraPreviewView(isFrontCamera: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (_: Exception) {
                    // Fallback gracefully in headless emulator / camera permission pending
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
