package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.LikRosePrimary

data class MediaSample(
    val url: String,
    val isVideo: Boolean = false,
    val label: String
)

@Composable
fun MediaAttachmentDialog(
    onDismiss: () -> Unit,
    onSendPhoto: (String) -> Unit,
    onSendVideo: (String) -> Unit
) {
    val samplePhotos = listOf(
        MediaSample("https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=600", label = "Love Memory 💖"),
        MediaSample("https://images.unsplash.com/photo-1522673607200-164d1b6ce486?w=600", label = "Sunset Walk 🌅"),
        MediaSample("https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=600", label = "Together Forever ❤️"),
        MediaSample("https://images.unsplash.com/photo-1511285560929-80b456fea0bc?w=600", label = "Date Night 🍷")
    )

    val sampleVideos = listOf(
        MediaSample("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", isVideo = true, label = "Our Beach Trip 🌊"),
        MediaSample("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4", isVideo = true, label = "Funny Moment 🎬")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Send Media Attachment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Photos", style = MaterialTheme.typography.labelMedium.copy(color = LikRosePrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(samplePhotos) { photo ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1.3f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSendPhoto(photo.url)
                                    onDismiss()
                                }
                        ) {
                            AsyncImage(
                                model = photo.url,
                                contentDescription = photo.label,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    photo.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Videos", style = MaterialTheme.typography.labelMedium.copy(color = LikRosePrimary, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleVideos.forEach { video ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .clickable {
                                    onSendVideo(video.url)
                                    onDismiss()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(LikRosePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Video",
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(video.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
