package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekIndigoLight
import com.example.ui.theme.SleekIndigoPrimary
import com.example.ui.viewmodel.ThemeViewModel
import com.example.ui.viewmodel.WallpaperConfig
import com.example.ui.viewmodel.WallpaperType

data class BuiltinWallpaperOption(
    val id: String,
    val name: String,
    val description: String,
    val colors: List<Color>
)

val BUILTIN_WALLPAPERS = listOf(
    BuiltinWallpaperOption(
        id = "ROMANTIC_SUNSET",
        name = "Romantic Sunset",
        description = "Sunset Violet & Rose",
        colors = listOf(Color(0xFF2E1065), Color(0xFF701A75), Color(0xFF9D174D))
    ),
    BuiltinWallpaperOption(
        id = "STARRY_NIGHT",
        name = "Starry Night",
        description = "Midnight Nebula & Deep Indigo",
        colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E1B4B))
    ),
    BuiltinWallpaperOption(
        id = "PASTEL_LAVENDER",
        name = "Pastel Lavender",
        description = "Soft Lavender Glow",
        colors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF6366F1))
    ),
    BuiltinWallpaperOption(
        id = "COZY_ROSE",
        name = "Cozy Rose",
        description = "Deep Rose & Wine Velvet",
        colors = listOf(Color(0xFF4C0519), Color(0xFF881337), Color(0xFFBE123C))
    ),
    BuiltinWallpaperOption(
        id = "FOREST_EMERALD",
        name = "Forest Emerald",
        description = "Pine & Emerald Jade",
        colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF059669))
    ),
    BuiltinWallpaperOption(
        id = "DEEP_SPACE",
        name = "Deep Space",
        description = "Cosmic Noir & Indigo",
        colors = listOf(Color(0xFF0B0F19), Color(0xFF1E1B4B), Color(0xFF311042))
    )
)

val SOLID_WALLPAPER_COLORS = listOf(
    "#0F172A" to "Midnight Navy",
    "#1E293B" to "Dark Slate",
    "#2D1B2E" to "Deep Violet",
    "#1A2E26" to "Forest Emerald",
    "#3F1D28" to "Soft Crimson",
    "#18181B" to "Warm Charcoal",
    "#172554" to "Royal Navy",
    "#3B0764" to "Muted Plum"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperPickerDialog(
    wallpaperConfig: WallpaperConfig,
    themeViewModel: ThemeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            themeViewModel.setGalleryWallpaper(it)
            Toast.makeText(context, "Gallery Wallpaper applied!", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = SleekIndigoPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Chat Wallpaper", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personalize your chat background. Your partner won't be affected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Gallery Wallpaper Option
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { galleryLauncher.launch("image/*") }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SleekIndigoLight,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = SleekIndigoPrimary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Choose from Gallery", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Select any photo from device", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (wallpaperConfig.type == WallpaperType.GALLERY) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = SleekIndigoPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                HorizontalDivider()

                // 2. Built-in Aesthetic Wallpapers
                Text("Built-In Aesthetic Wallpapers", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BUILTIN_WALLPAPERS.forEach { option ->
                        val isSelected = wallpaperConfig.type == WallpaperType.BUILTIN && wallpaperConfig.value == option.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, SleekIndigoPrimary) else null,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    themeViewModel.setBuiltinWallpaper(option.id)
                                    Toast.makeText(context, "Set to ${option.name}", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Swatch Box with Gradient
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.verticalGradient(option.colors))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(option.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(option.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = SleekIndigoPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. Solid Color Backgrounds
                Text("Solid Color Backgrounds", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 4
                ) {
                    SOLID_WALLPAPER_COLORS.forEach { (hex, name) ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color.DarkGray
                        }
                        val isSelected = wallpaperConfig.type == WallpaperType.SOLID && wallpaperConfig.value == hex

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    themeViewModel.setSolidWallpaper(hex)
                                    Toast.makeText(context, "Set to $name", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 4. Reset to Default
                if (wallpaperConfig.type != WallpaperType.DEFAULT) {
                    OutlinedButton(
                        onClick = {
                            themeViewModel.resetWallpaperToDefault()
                            Toast.makeText(context, "Wallpaper reset to default", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Default Wallpaper", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
