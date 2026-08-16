package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import com.example.data.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.UUID

enum class NetworkConnectionState {
    CONNECTED, CONNECTING, DISCONNECTED
}

class LikRealtimeSyncManager(private val context: Context) {

    private val tag = "LikRealtimeSync"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectMutex = Mutex()

    // Global Public MQTT Brokers for resilient peer-to-peer relay over the internet
    private val brokers = listOf(
        "tcp://broker.hivemq.com:1883",
        "tcp://broker.emqx.io:1883",
        "tcp://test.mosquitto.org:1883"
    )
    private var currentBrokerIndex = 0

    // Stable client ID generated once per application run / device
    private val appClientId: String by lazy {
        val randomPart = UUID.randomUUID().toString().replace("-", "").take(8)
        "lik_app_${randomPart}"
    }

    private var mqttClient: MqttAsyncClient? = null
    private var myUserId: String = ""
    private var myPairCode: String = ""
    private var currentRoomId: String? = null

    private val _connectionState = MutableStateFlow(NetworkConnectionState.DISCONNECTED)
    val connectionState: StateFlow<NetworkConnectionState> = _connectionState.asStateFlow()

    private var reconnectJob: Job? = null
    private var retryDelayMs = 2000L
    private val maxRetryDelayMs = 30000L

    // Callbacks for repository integration
    var onMessageReceived: ((ChatMessage) -> Unit)? = null
    var onMessageStatusUpdated: ((messageId: String, status: MessageStatus) -> Unit)? = null
    var onPartnerTyping: ((Boolean) -> Unit)? = null
    var onPartnerProfileUpdated: ((displayName: String?, emoji: String?, avatarUrl: String?, mood: String?) -> Unit)? = null
    var onPairRequestReceived: ((partnerUser: UserAccount) -> Unit)? = null
    var onPairAcceptedReceived: ((partnerUser: UserAccount) -> Unit)? = null
    var onCallSignalReceived: ((JSONObject) -> Unit)? = null

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(
                networkRequest,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(tag, "Internet network available, verifying MQTT connection...")
                        if (myUserId.isNotBlank()) {
                            connect()
                        }
                    }

                    override fun onLost(network: Network) {
                        Log.d(tag, "Internet network lost")
                        _connectionState.value = NetworkConnectionState.DISCONNECTED
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(tag, "Error registering network callback", e)
        }
    }

    fun initializeUser(userId: String, pairCode: String) {
        this.myUserId = userId
        this.myPairCode = pairCode.trim().uppercase()
        connect()
    }

    fun setRoom(roomId: String) {
        this.currentRoomId = roomId
        subscribeToRoom(roomId)
    }

    fun connect() {
        if (myUserId.isBlank()) return

        scope.launch {
            connectMutex.withLock {
                if (mqttClient?.isConnected == true) {
                    _connectionState.value = NetworkConnectionState.CONNECTED
                    subscribeToUserTopics()
                    currentRoomId?.let { subscribeToRoom(it) }
                    return@withLock
                }

                _connectionState.value = NetworkConnectionState.CONNECTING

                try {
                    // Safe cleanup of any previous dead/stale client
                    try {
                        mqttClient?.setCallback(null)
                        if (mqttClient?.isConnected == true) {
                            mqttClient?.disconnectForcibly(500, 500)
                        }
                        mqttClient?.close()
                    } catch (ignored: Exception) {
                    }

                    val serverUri = brokers[currentBrokerIndex % brokers.size]
                    val uniqueClientId = "${appClientId}_${myUserId.take(6)}"

                    Log.d(tag, "Connecting to MQTT Broker: $serverUri (clientId=$uniqueClientId)...")

                    val client = MqttAsyncClient(serverUri, uniqueClientId, MemoryPersistence())
                    mqttClient = client

                    val options = MqttConnectOptions().apply {
                        isAutomaticReconnect = true
                        isCleanSession = true // Critical for cloud brokers to avoid session clashes
                        connectionTimeout = 15
                        keepAliveInterval = 30
                        maxInflight = 100
                    }

                    client.setCallback(object : MqttCallbackExtended {
                        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                            Log.d(tag, "MQTT Connected successfully to $serverURI (reconnect=$reconnect)")
                            _connectionState.value = NetworkConnectionState.CONNECTED
                            retryDelayMs = 2000L // Reset backoff on success
                            scope.launch {
                                subscribeToUserTopics()
                                currentRoomId?.let { subscribeToRoom(it) }
                            }
                        }

                        override fun connectionLost(cause: Throwable?) {
                            Log.w(tag, "MQTT Connection lost: ${cause?.message ?: "Unknown cause"}")
                            _connectionState.value = NetworkConnectionState.DISCONNECTED
                            scheduleReconnect()
                        }

                        override fun messageArrived(topic: String?, message: MqttMessage?) {
                            if (topic == null || message == null) return
                            val payload = String(message.payload, Charsets.UTF_8)
                            handleIncomingMqttMessage(topic, payload)
                        }

                        override fun deliveryComplete(token: IMqttDeliveryToken?) {
                            // Delivered to broker
                        }
                    })

                    client.connect(options, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(tag, "MQTT Connection established to $serverUri")
                            _connectionState.value = NetworkConnectionState.CONNECTED
                            retryDelayMs = 2000L
                            scope.launch {
                                subscribeToUserTopics()
                                currentRoomId?.let { subscribeToRoom(it) }
                            }
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.w(tag, "MQTT Connect failed on $serverUri: ${exception?.message}")
                            _connectionState.value = NetworkConnectionState.DISCONNECTED
                            currentBrokerIndex++
                            scheduleReconnect()
                        }
                    })

                } catch (e: Exception) {
                    Log.e(tag, "Exception during MQTT connect setup: ${e.message}", e)
                    _connectionState.value = NetworkConnectionState.DISCONNECTED
                    currentBrokerIndex++
                    scheduleReconnect()
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (myUserId.isBlank()) return
        if (reconnectJob?.isActive == true) return

        reconnectJob = scope.launch {
            delay(retryDelayMs)
            retryDelayMs = (retryDelayMs * 1.5).toLong().coerceAtMost(maxRetryDelayMs)
            Log.d(tag, "Attempting MQTT auto-reconnect (next backoff: ${retryDelayMs}ms)...")
            connect()
        }
    }

    private fun subscribeToUserTopics() {
        val client = mqttClient ?: return
        if (myPairCode.isBlank()) return

        try {
            if (!client.isConnected) {
                Log.d(tag, "Delaying user topic subscription: client is not fully connected yet.")
                return
            }
            val userTopic = "lik/v1/user/$myPairCode/#"
            client.subscribe(userTopic, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag, "Subscribed to personal pair channel: $userTopic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w(tag, "Failed to subscribe to $userTopic: ${exception?.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(tag, "Error or race condition subscribing to user topic: ${e.message}")
        }
    }

    private fun subscribeToRoom(roomId: String) {
        val client = mqttClient ?: return
        if (roomId.isBlank()) return

        try {
            if (!client.isConnected) {
                Log.d(tag, "Delaying room topic subscription: client is not fully connected yet.")
                return
            }
            val roomTopic = "lik/v1/room/$roomId/#"
            client.subscribe(roomTopic, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag, "Subscribed to shared room channel: $roomTopic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w(tag, "Failed to subscribe to room $roomTopic: ${exception?.message}")
                }
            })
        } catch (e: Exception) {
            Log.w(tag, "Error or race condition subscribing to room topic: ${e.message}")
        }
    }

    private fun handleIncomingMqttMessage(topic: String, payload: String) {
        scope.launch {
            try {
                Log.d(tag, "Incoming payload on topic: $topic")
                val json = JSONObject(payload)

                // 1. User Handshake & Pairing Channels
                if (topic.startsWith("lik/v1/user/")) {
                    when {
                        topic.endsWith("/pair_request") -> {
                            val partnerUser = parseUserAccount(json)
                            onPairRequestReceived?.invoke(partnerUser)
                        }
                        topic.endsWith("/pair_accept") -> {
                            val partnerUser = parseUserAccount(json)
                            onPairAcceptedReceived?.invoke(partnerUser)
                        }
                    }
                    return@launch
                }

                // 2. Room Specific Realtime Events
                if (topic.startsWith("lik/v1/room/")) {
                    when {
                        topic.endsWith("/msg") -> {
                            val senderId = json.optString("senderId")
                            if (senderId != myUserId) {
                                val chatMessage = parseChatMessage(json)
                                onMessageReceived?.invoke(chatMessage)
                            }
                        }

                        topic.endsWith("/status") -> {
                            val msgId = json.optString("messageId")
                            val statusStr = json.optString("status")
                            val status = try {
                                MessageStatus.valueOf(statusStr)
                            } catch (e: Exception) {
                                MessageStatus.DELIVERED
                            }
                            if (msgId.isNotBlank()) {
                                onMessageStatusUpdated?.invoke(msgId, status)
                            }
                        }

                        topic.endsWith("/typing") -> {
                            val senderId = json.optString("senderId")
                            val isTyping = json.optBoolean("isTyping", false)
                            if (senderId != myUserId) {
                                onPartnerTyping?.invoke(isTyping)
                            }
                        }

                        topic.endsWith("/profile") -> {
                            val senderId = json.optString("senderId")
                            if (senderId != myUserId) {
                                val displayName = json.optString("displayName").takeIf { it.isNotBlank() }
                                val emoji = json.optString("avatarEmoji").takeIf { it.isNotBlank() }
                                val avatarUrl = json.optString("customAvatarUrl").takeIf { it.isNotBlank() }
                                val mood = json.optString("statusMood").takeIf { it.isNotBlank() }
                                onPartnerProfileUpdated?.invoke(displayName, emoji, avatarUrl, mood)
                            }
                        }

                        topic.endsWith("/call") -> {
                            val senderId = json.optString("senderId")
                            if (senderId != myUserId) {
                                onCallSignalReceived?.invoke(json)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing incoming MQTT payload", e)
            }
        }
    }

    // --- Outgoing Publishing Methods ---

    fun publishChatMessage(roomId: String, message: ChatMessage, onComplete: ((Boolean) -> Unit)? = null) {
        val topic = "lik/v1/room/$roomId/msg"
        val json = JSONObject().apply {
            put("id", message.id)
            put("senderId", message.senderId)
            put("receiverId", message.receiverId)
            put("text", message.text)
            put("mediaType", message.mediaType.name)
            put("mediaUrl", message.mediaUrl ?: "")
            put("mediaSizeFormatted", message.mediaSizeFormatted ?: "")
            put("uploadProgress", message.uploadProgress.toDouble())
            put("timestamp", message.timestamp)
            put("status", MessageStatus.SENT.name)
            put("replyToId", message.replyToId ?: "")
            put("replyToText", message.replyToText ?: "")
            put("replyToSenderName", message.replyToSenderName ?: "")
            put("isDeleted", message.isDeleted)
        }
        publish(topic, json.toString(), qos = 1, onComplete = onComplete)
    }

    fun publishMessageStatus(roomId: String, messageId: String, status: MessageStatus) {
        val topic = "lik/v1/room/$roomId/status"
        val json = JSONObject().apply {
            put("messageId", messageId)
            put("status", status.name)
            put("senderId", myUserId)
            put("timestamp", System.currentTimeMillis())
        }
        publish(topic, json.toString(), qos = 1)
    }

    fun publishTyping(roomId: String, isTyping: Boolean) {
        val topic = "lik/v1/room/$roomId/typing"
        val json = JSONObject().apply {
            put("senderId", myUserId)
            put("isTyping", isTyping)
        }
        publish(topic, json.toString(), qos = 0)
    }

    fun publishProfileUpdate(roomId: String, user: UserAccount) {
        val topic = "lik/v1/room/$roomId/profile"
        val json = JSONObject().apply {
            put("senderId", user.id)
            put("displayName", user.displayName)
            put("avatarEmoji", user.avatarEmoji)
            put("customAvatarUrl", user.customAvatarUrl ?: "")
            put("statusMood", user.statusMood ?: "")
            put("timestamp", System.currentTimeMillis())
        }
        publish(topic, json.toString(), qos = 1)
    }

    fun publishPairRequest(targetPairCode: String, myUser: UserAccount, onComplete: ((Boolean) -> Unit)? = null) {
        val cleanTarget = targetPairCode.trim().uppercase()
        val topic = "lik/v1/user/$cleanTarget/pair_request"
        val json = userAccountToJson(myUser)
        publish(topic, json.toString(), qos = 1, onComplete = onComplete)
    }

    fun publishPairAccept(targetPairCode: String, myUser: UserAccount) {
        val cleanTarget = targetPairCode.trim().uppercase()
        val topic = "lik/v1/user/$cleanTarget/pair_accept"
        val json = userAccountToJson(myUser)
        publish(topic, json.toString(), qos = 1)
    }

    fun publishCallSignal(roomId: String, payload: JSONObject) {
        val topic = "lik/v1/room/$roomId/call"
        publish(topic, payload.toString(), qos = 1)
    }

    private fun publish(topic: String, payload: String, qos: Int = 1, onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            try {
                val client = mqttClient
                if (client != null && client.isConnected) {
                    val message = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
                        this.qos = qos
                    }
                    client.publish(topic, message, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(tag, "Successfully published payload to $topic")
                            onComplete?.invoke(true)
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.w(tag, "Failed publishing to $topic: ${exception?.message}")
                            onComplete?.invoke(false)
                        }
                    })
                } else {
                    Log.w(tag, "MQTT not connected while publishing to $topic, initiating connect...")
                    connect()
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error publishing to $topic", e)
                onComplete?.invoke(false)
            }
        }
    }

    // --- JSON Helpers ---

    private fun userAccountToJson(user: UserAccount): JSONObject {
        return JSONObject().apply {
            put("id", user.id)
            put("username", user.username)
            put("displayName", user.displayName)
            put("avatarEmoji", user.avatarEmoji)
            put("customAvatarUrl", user.customAvatarUrl ?: "")
            put("statusMood", user.statusMood ?: "")
            put("pairCode", user.pairCode)
            put("pairedUserId", user.pairedUserId ?: "")
        }
    }

    private fun parseUserAccount(json: JSONObject): UserAccount {
        return UserAccount(
            id = json.optString("id", UUID.randomUUID().toString()),
            username = json.optString("username"),
            displayName = json.optString("displayName").ifBlank { json.optString("username", "Partner") },
            avatarEmoji = json.optString("avatarEmoji", "🌸"),
            customAvatarUrl = json.optString("customAvatarUrl").takeIf { it.isNotBlank() },
            statusMood = json.optString("statusMood").takeIf { it.isNotBlank() },
            pairCode = json.optString("pairCode"),
            pairedUserId = json.optString("pairedUserId").takeIf { it.isNotBlank() }
        )
    }

    private fun parseChatMessage(json: JSONObject): ChatMessage {
        val mediaTypeStr = json.optString("mediaType", "NONE")
        val mediaType = try {
            MediaType.valueOf(mediaTypeStr)
        } catch (e: Exception) {
            MediaType.NONE
        }

        return ChatMessage(
            id = json.optString("id", UUID.randomUUID().toString()),
            senderId = json.optString("senderId"),
            receiverId = json.optString("receiverId"),
            text = json.optString("text"),
            mediaType = mediaType,
            mediaUrl = json.optString("mediaUrl").takeIf { it.isNotBlank() },
            mediaSizeFormatted = json.optString("mediaSizeFormatted").takeIf { it.isNotBlank() },
            uploadProgress = json.optDouble("uploadProgress", 1.0).toFloat(),
            timestamp = json.optLong("timestamp", System.currentTimeMillis()),
            status = MessageStatus.DELIVERED,
            replyToId = json.optString("replyToId").takeIf { it.isNotBlank() },
            replyToText = json.optString("replyToText").takeIf { it.isNotBlank() },
            replyToSenderName = json.optString("replyToSenderName").takeIf { it.isNotBlank() },
            isDeleted = json.optBoolean("isDeleted", false)
        )
    }
}
