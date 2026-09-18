package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSendImage: (uri: String, caption: String) -> Unit,
    onSendDocument: (name: String, sizeBytes: Long) -> Unit,
    onSendLocation: (title: String, lat: Double, lng: Double) -> Unit,
    onSendContact: (name: String, phone: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Share & Send Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    icon = Icons.Default.Image,
                    label = "Gallery",
                    color = Color(0xFF3B82F6),
                    onClick = {
                        onDismiss()
                        onSendImage(
                            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
                            "Abstract design art"
                        )
                    }
                )

                AttachmentItem(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    color = Color(0xFFEC4899),
                    onClick = {
                        onDismiss()
                        onSendImage(
                            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=600",
                            "Captured moment 📷"
                        )
                    }
                )

                AttachmentItem(
                    icon = Icons.Default.Description,
                    label = "Document",
                    color = Color(0xFF8B5CF6),
                    onClick = {
                        onDismiss()
                        onSendDocument("Project_Specification_2026.pdf", 3_450_000L)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = Color(0xFF10B981),
                    onClick = {
                        onDismiss()
                        onSendLocation("Pulse Innovation Hub, Downtown", 37.7858, -122.4065)
                    }
                )

                AttachmentItem(
                    icon = Icons.Default.ContactPhone,
                    label = "Contact",
                    color = Color(0xFFF59E0B),
                    onClick = {
                        onDismiss()
                        onSendContact("Jordan Taylor", "+1 (555) 789-0123")
                    }
                )

                AttachmentItem(
                    icon = Icons.Default.Description,
                    label = "Archive ZIP",
                    color = Color(0xFF64748B),
                    onClick = {
                        onDismiss()
                        onSendDocument("Application_Assets_Bundle.zip", 14_800_000L)
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
