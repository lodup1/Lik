package com.example.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

enum class WallpaperType {
    DEFAULT, SOLID, BUILTIN, GALLERY
}

data class WallpaperConfig(
    val type: WallpaperType = WallpaperType.DEFAULT,
    val value: String = "" // Color hex (e.g. "#1A1B2F"), builtin name, or file path
)

class ThemeViewModel(private val context: Context? = null) : ViewModel() {
    private val prefs: SharedPreferences? =
        context?.getSharedPreferences("lik_theme_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs?.getBoolean("is_dark_mode", true) ?: true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _wallpaperConfig = MutableStateFlow(loadWallpaperConfig())
    val wallpaperConfig: StateFlow<WallpaperConfig> = _wallpaperConfig.asStateFlow()

    private fun loadWallpaperConfig(): WallpaperConfig {
        val typeStr = prefs?.getString("wallpaper_type", WallpaperType.DEFAULT.name) ?: WallpaperType.DEFAULT.name
        val value = prefs?.getString("wallpaper_value", "") ?: ""
        val type = try { WallpaperType.valueOf(typeStr) } catch (e: Exception) { WallpaperType.DEFAULT }
        return WallpaperConfig(type, value)
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs?.edit()?.putBoolean("is_dark_mode", newValue)?.apply()
    }

    fun setSolidWallpaper(colorHex: String) {
        val config = WallpaperConfig(WallpaperType.SOLID, colorHex)
        _wallpaperConfig.value = config
        saveWallpaperConfig(config)
    }

    fun setBuiltinWallpaper(builtinId: String) {
        val config = WallpaperConfig(WallpaperType.BUILTIN, builtinId)
        _wallpaperConfig.value = config
        saveWallpaperConfig(config)
    }

    fun setGalleryWallpaper(uri: Uri) {
        context?.let { ctx ->
            try {
                // Delete previous wallpaper files
                ctx.filesDir.listFiles()?.filter { it.name.startsWith("chat_wallpaper") }?.forEach { it.delete() }
                
                val fileName = "chat_wallpaper_${System.currentTimeMillis()}.jpg"
                val file = File(ctx.filesDir, fileName)
                val inputStream = ctx.contentResolver.openInputStream(uri) ?: return
                val outputStream = FileOutputStream(file)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val config = WallpaperConfig(WallpaperType.GALLERY, file.absolutePath)
                _wallpaperConfig.value = config
                saveWallpaperConfig(config)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetWallpaperToDefault() {
        context?.let { ctx ->
            try {
                ctx.filesDir.listFiles()?.filter { it.name.startsWith("chat_wallpaper") }?.forEach { it.delete() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val config = WallpaperConfig(WallpaperType.DEFAULT, "")
        _wallpaperConfig.value = config
        saveWallpaperConfig(config)
    }

    private fun saveWallpaperConfig(config: WallpaperConfig) {
        prefs?.edit()
            ?.putString("wallpaper_type", config.type.name)
            ?.putString("wallpaper_value", config.value)
            ?.apply()
    }
}
