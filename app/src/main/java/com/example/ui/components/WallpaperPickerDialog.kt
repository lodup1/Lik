package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class WallpaperOption(val name: String, val color: Color)

val availableWallpapers = listOf(
    WallpaperOption("Default", Color(0xFFFCE4EC)),
    WallpaperOption("Rose Mist", Color(0xFFF8BBD0)),
    WallpaperOption("Sunset Glow", Color(0xFFFFCCBC)),
    WallpaperOption("Lavender Dream", Color(0xFFE1BEE7)),
    WallpaperOption("Midnight Love", Color(0xFF263238)),
    WallpaperOption("Soft Mint", Color(0xFFC8E6C9))
)

@Composable
fun WallpaperPickerDialog(
    currentWallpaper: String,
    onSelectWallpaper: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Chat Wallpaper", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableWallpapers) { wallpaper ->
                    val isSelected = wallpaper.name == currentWallpaper
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(wallpaper.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectWallpaper(wallpaper.name)
                            }
                            .testTag("wallpaper_${wallpaper.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wallpaper.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (wallpaper.name == "Midnight Love") Color.White else Color.Black
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("close_wallpaper_btn")) {
                Text("Done")
            }
        }
    )
}
