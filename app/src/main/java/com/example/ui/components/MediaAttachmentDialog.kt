package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaAttachmentDialog(
    onDismiss: () -> Unit,
    onAttachImage: () -> Unit,
    onAttachVoice: () -> Unit,
    onAttachLocation: () -> Unit,
    onAttachSticker: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Share with Partner",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Photo",
                    tag = "attach_image_btn",
                    onClick = {
                        onAttachImage()
                        onDismiss()
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Mic,
                    label = "Voice",
                    tag = "attach_voice_btn",
                    onClick = {
                        onAttachVoice()
                        onDismiss()
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Place,
                    label = "Location",
                    tag = "attach_location_btn",
                    onClick = {
                        onAttachLocation()
                        onDismiss()
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Favorite,
                    label = "Sticker",
                    tag = "attach_sticker_btn",
                    onClick = {
                        onAttachSticker()
                        onDismiss()
                    }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(tag)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
