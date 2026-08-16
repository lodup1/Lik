package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.util.LruCache
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

object LikNotificationManager {
    private const val TAG = "LikNotificationManager"
    const val CHANNEL_ID = "lik_chat_messages_channel"
    const val CHANNEL_NAME = "Chat Messages"
    private const val GROUP_KEY = "com.example.lik.CHAT_MESSAGES_GROUP"
    private const val NOTIFICATION_ID_BASE = 1000
    private const val SUMMARY_NOTIFICATION_ID = 999

    const val EXTRA_NAVIGATE_TO = "EXTRA_NAVIGATE_TO"
    const val EXTRA_SENDER_ID = "EXTRA_SENDER_ID"
    const val EXTRA_SENDER_NAME = "EXTRA_SENDER_NAME"
    const val EXTRA_MESSAGE_ID = "EXTRA_MESSAGE_ID"

    // Track active screen state to avoid displaying push notification when already looking at the chat
    @Volatile
    var isChatScreenVisible: Boolean = false

    @Volatile
    var isAppInForeground: Boolean = false

    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory LRU cache for profile picture bitmaps (30 max entries)
    private val avatarBitmapCache = object : LruCache<String, Bitmap>(30) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return (value.byteCount / 1024).coerceAtLeast(1)
        }
    }

    // Thread-safe set of notified message IDs to prevent duplicate alerts
    private val notifiedMessageIds = Collections.synchronizedSet(LinkedHashSet<String>())
    private const val MAX_TRACKED_MESSAGES = 300

    // Stored unread message items per conversation for WhatsApp/Telegram-style thread grouping
    data class NotificationMessageItem(
        val messageId: String,
        val text: String,
        val timestamp: Long,
        val isFromMe: Boolean
    )

    private val activeConversationHistory =
        Collections.synchronizedMap(LinkedHashMap<String, MutableList<NotificationMessageItem>>())

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for incoming chat messages in Lik"
                    enableLights(true)
                    lightColor = 0xFF6366F1.toInt()
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                    setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $CHANNEL_ID")
            }
        }
    }

    fun showMessageNotification(
        context: Context,
        message: ChatMessage,
        senderDisplayName: String,
        senderAvatarUrl: String? = null,
        senderAvatarEmoji: String? = null,
        currentUserId: String? = null
    ) {
        // Prevent duplicate notification for the exact same message
        if (notifiedMessageIds.contains(message.id)) {
            Log.d(TAG, "Skipping notification - message already notified: ${message.id}")
            return
        }

        // If user is actively reading this conversation in foreground, suppress system notification
        if (isChatScreenVisible && isAppInForeground) {
            Log.d(TAG, "Chat is currently active in foreground, suppressing notification banner")
            notifiedMessageIds.add(message.id)
            return
        }

        // Run in IO scope to handle DP image decoding / loading asynchronously
        scope.launch {
            try {
                // Ensure notification channel is configured
                createNotificationChannel(context)

                // Check POST_NOTIFICATIONS permission on Android 13+ (API 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping notification")
                        return@launch
                    }
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        ?: return@launch

                // Format preview text for modern messaging notifications
                val previewText = formatMessagePreview(message)

                // Retrieve or load circular sender avatar DP
                val avatarBitmap = getOrLoadAvatar(
                    context = context,
                    avatarUrl = senderAvatarUrl,
                    emoji = senderAvatarEmoji ?: "❤️",
                    displayName = senderDisplayName
                )

                // Update active unread conversation history
                val senderKey = message.senderId.ifBlank { "partner_chat" }
                val historyList = activeConversationHistory.getOrPut(senderKey) {
                    Collections.synchronizedList(mutableListOf())
                }

                synchronized(historyList) {
                    if (historyList.none { it.messageId == message.id }) {
                        historyList.add(
                            NotificationMessageItem(
                                messageId = message.id,
                                text = previewText,
                                timestamp = message.timestamp,
                                isFromMe = false
                            )
                        )
                        // Keep only recent 15 messages per conversation
                        if (historyList.size > 15) {
                            historyList.removeAt(0)
                        }
                    }
                }

                // 1. Build Person objects for Android MessagingStyle
                val mePerson = Person.Builder()
                    .setName("You")
                    .setKey(currentUserId ?: "me")
                    .build()

                val senderPersonBuilder = Person.Builder()
                    .setName(senderDisplayName)
                    .setKey(senderKey)
                if (avatarBitmap != null) {
                    val senderIconCompat = IconCompat.createWithBitmap(avatarBitmap)
                    senderPersonBuilder.setIcon(senderIconCompat)
                }
                val senderPerson = senderPersonBuilder.build()

                // 2. Build MessagingStyle (WhatsApp / Telegram 1-on-1 style)
                val messagingStyle = NotificationCompat.MessagingStyle(mePerson)
                    .setConversationTitle(null) // Direct 1-to-1 conversation
                    .setGroupConversation(false)

                synchronized(historyList) {
                    for (item in historyList) {
                        val person = if (item.isFromMe) mePerson else senderPerson
                        messagingStyle.addMessage(
                            NotificationCompat.MessagingStyle.Message(
                                item.text,
                                item.timestamp,
                                person
                            )
                        )
                    }
                }

                // 3. PendingIntent to open the exact conversation on tap (works cold start & background)
                val intent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_NAVIGATE_TO, "chat")
                    putExtra(EXTRA_SENDER_ID, message.senderId)
                    putExtra(EXTRA_SENDER_NAME, senderDisplayName)
                    putExtra(EXTRA_MESSAGE_ID, message.id)
                }

                val requestCode = (senderKey.hashCode() and 0x7FFFFFFF)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // 4. Build the modern Notification
                val conversationNotificationId = NOTIFICATION_ID_BASE + (senderKey.hashCode() and 0x7FFF)
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_chat)
                    .apply {
                        if (avatarBitmap != null) {
                            setLargeIcon(avatarBitmap)
                        }
                    }
                    .setStyle(messagingStyle)
                    .setContentTitle(senderDisplayName)
                    .setContentText(previewText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setColor(0xFF6366F1.toInt())
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(false)
                    .setContentIntent(pendingIntent)
                    .setGroup(GROUP_KEY)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

                notificationManager.notify(conversationNotificationId, builder.build())
                Log.d(TAG, "Dispatched WhatsApp/Telegram-style notification for conversation $senderKey")

                // 5. Track notified ID
                synchronized(notifiedMessageIds) {
                    if (notifiedMessageIds.size > MAX_TRACKED_MESSAGES) {
                        val iterator = notifiedMessageIds.iterator()
                        if (iterator.hasNext()) {
                            iterator.next()
                            iterator.remove()
                        }
                    }
                    notifiedMessageIds.add(message.id)
                }

                // 6. Post summary notification if multiple conversations exist
                if (activeConversationHistory.size > 1) {
                    val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_chat)
                        .setStyle(
                            NotificationCompat.InboxStyle()
                                .setSummaryText("${activeConversationHistory.size} chats")
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setColor(0xFF6366F1.toInt())
                        .setGroup(GROUP_KEY)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error displaying message notification", e)
            }
        }
    }

    /**
     * Formats message text appropriately according to media type and status.
     */
    private fun formatMessagePreview(message: ChatMessage): String {
        if (message.isDeleted) {
            return "🚫 This message was deleted"
        }
        return when (message.mediaType) {
            MediaType.IMAGE -> if (message.text.isNotBlank()) "📷 ${message.text}" else "📷 Photo"
            MediaType.VIDEO -> if (message.text.isNotBlank()) "🎥 ${message.text}" else "🎥 Video"
            MediaType.VOICE -> "🎤 Voice message"
            MediaType.NONE -> message.text.ifBlank { "New message" }
        }
    }

    /**
     * Loads the sender's profile avatar from cache, local file, or remote URL,
     * or generates a high-quality circular avatar with initials / emoji.
     */
    private suspend fun getOrLoadAvatar(
        context: Context,
        avatarUrl: String?,
        emoji: String,
        displayName: String
    ): Bitmap = withContext(Dispatchers.IO) {
        val cacheKey = avatarUrl?.ifBlank { null } ?: "avatar_${displayName}_$emoji"

        // Check memory cache first
        avatarBitmapCache.get(cacheKey)?.let {
            return@withContext it
        }

        var loadedBitmap: Bitmap? = null

        if (!avatarUrl.isNullOrBlank()) {
            try {
                if (avatarUrl.startsWith("/") || avatarUrl.startsWith("file://")) {
                    // Local file storage - decode with inSampleSize to save memory and avoid Ashmem warnings
                    val filePath = if (avatarUrl.startsWith("file://")) avatarUrl.removePrefix("file://") else avatarUrl
                    val file = File(filePath)
                    if (file.exists()) {
                        val boundsOptions = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
                        
                        var sampleSize = 1
                        val targetPx = 128
                        if (boundsOptions.outHeight > targetPx || boundsOptions.outWidth > targetPx) {
                            val halfHeight = boundsOptions.outHeight / 2
                            val halfWidth = boundsOptions.outWidth / 2
                            while ((halfHeight / sampleSize) >= targetPx && (halfWidth / sampleSize) >= targetPx) {
                                sampleSize *= 2
                            }
                        }

                        val decodeOptions = BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                        if (rawBitmap != null) {
                            loadedBitmap = getCircularBitmap(rawBitmap, 128)
                        }
                    }
                } else if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                    // Remote image URL via Coil with fixed dimensions
                    val imageLoader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .size(128, 128)
                        .allowHardware(false) // Required for Notification Bitmaps
                        .build()
                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val drawable = result.drawable
                        if (drawable is BitmapDrawable && drawable.bitmap != null) {
                            loadedBitmap = getCircularBitmap(drawable.bitmap, 128)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load avatar from $avatarUrl: ${e.message}")
            }
        }

        // Fallback: Generate initials/emoji circular avatar bitmap (compact 128px)
        val finalBitmap = loadedBitmap ?: createInitialsAvatar(
            nameOrEmoji = if (emoji.isNotBlank()) emoji else displayName,
            sizePx = 128
        )

        avatarBitmapCache.put(cacheKey, finalBitmap)
        return@withContext finalBitmap
    }

    /**
     * Converts any rectangular bitmap into a circular cropped bitmap with target dimensions.
     */
    fun getCircularBitmap(src: Bitmap, targetSize: Int = 128): Bitmap {
        val minEdge = Math.min(src.width, src.height).coerceAtLeast(1)
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val destRect = Rect(0, 0, targetSize, targetSize)

        val xOffset = (src.width - minEdge) / 2
        val yOffset = (src.height - minEdge) / 2
        val srcRect = Rect(xOffset, yOffset, xOffset + minEdge, yOffset + minEdge)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(targetSize / 2f, targetSize / 2f, targetSize / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, srcRect, destRect, paint)
        return output
    }

    /**
     * Generates a circular avatar bitmap with a gradient background and centered initials or emoji.
     */
    fun createInitialsAvatar(
        nameOrEmoji: String,
        sizePx: Int = 128,
        gradientColors: IntArray = intArrayOf(0xFF6366F1.toInt(), 0xFF4338CA.toInt())
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
                gradientColors,
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint)

        // Draw Emoji or Initials
        val isAnEmoji = isEmojiString(nameOrEmoji)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = if (isAnEmoji) sizePx * 0.48f else sizePx * 0.40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val displaySymbol = if (nameOrEmoji.isNotBlank()) {
            if (isAnEmoji) {
                nameOrEmoji.trim()
            } else {
                nameOrEmoji.trim().take(2).uppercase()
            }
        } else {
            "❤️"
        }

        val textBounds = Rect()
        textPaint.getTextBounds(displaySymbol, 0, displaySymbol.length, textBounds)
        val y = (sizePx / 2f) - textBounds.exactCenterY()
        canvas.drawText(displaySymbol, sizePx / 2f, y, textPaint)

        return bitmap
    }

    private fun isEmojiString(str: String): Boolean {
        if (str.isEmpty()) return false
        val codePoint = str.codePointAt(0)
        val type = Character.getType(codePoint)
        return type == Character.SURROGATE.toInt() ||
                type == Character.OTHER_SYMBOL.toInt() ||
                codePoint > 0x1F000 ||
                str.contains("❤") || str.contains("❤️") || str.contains("🌸") || str.contains("✨")
    }

    /**
     * Clears notifications for a specific conversation when the user opens or reads that chat.
     */
    fun clearConversationNotification(context: Context, senderId: String) {
        val senderKey = senderId.ifBlank { "partner_chat" }
        activeConversationHistory.remove(senderKey)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

        val conversationNotificationId = NOTIFICATION_ID_BASE + (senderKey.hashCode() and 0x7FFF)
        try {
            notificationManager.cancel(conversationNotificationId)
            if (activeConversationHistory.isEmpty()) {
                notificationManager.cancel(SUMMARY_NOTIFICATION_ID)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling conversation notification", e)
        }
    }

    /**
     * Clears all active notifications and resets conversation tracking.
     */
    fun cancelChatNotifications(context: Context) {
        activeConversationHistory.clear()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling all notifications", e)
        }
    }
}
