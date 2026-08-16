package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.PartnerProfileEntity
import com.example.data.model.PairingInfo
import com.example.data.model.UserAccount
import com.example.data.remote.LikRealtimeSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PairingRepository(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val syncManager: LikRealtimeSyncManager
) {
    private val tag = "PairingRepository"
    private val db = AppDatabase.getDatabase(context)
    private val partnerDao = db.partnerDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs = context.getSharedPreferences("lik_pairing_prefs", Context.MODE_PRIVATE)

    private val _pairingInfo = MutableStateFlow<PairingInfo>(loadPairingInfo())
    val pairingInfo: StateFlow<PairingInfo> = _pairingInfo.asStateFlow()

    init {
        setupSyncCallbacks()
        // If already paired from stored session, initialize room
        val stored = _pairingInfo.value
        if (stored.isPaired) {
            val myCode = authRepository.currentUser.value?.pairCode ?: ""
            val partnerCode = prefs.getString("partner_pair_code", stored.partnerId ?: "") ?: ""
            if (myCode.isNotBlank() && partnerCode.isNotBlank()) {
                val roomId = computeRoomId(myCode, partnerCode)
                syncManager.setRoom(roomId)
            }
        }
    }

    private fun setupSyncCallbacks() {
        syncManager.onPairRequestReceived = { partnerUser ->
            Log.d(tag, "Received pair request from: ${partnerUser.displayName} (${partnerUser.pairCode})")
            handleIncomingPairRequest(partnerUser)
        }

        syncManager.onPairAcceptedReceived = { partnerUser ->
            Log.d(tag, "Received pair accepted from: ${partnerUser.displayName} (${partnerUser.pairCode})")
            handleIncomingPairAccepted(partnerUser)
        }

        syncManager.onPartnerProfileUpdated = { name, emoji, avatarUrl, mood ->
            Log.d(tag, "Partner profile updated: $name, $emoji, $avatarUrl, $mood")
            val current = _pairingInfo.value
            if (current.isPaired) {
                val updated = current.copy(
                    partnerDisplayName = name ?: current.partnerDisplayName,
                    partnerAvatarEmoji = emoji ?: current.partnerAvatarEmoji,
                    partnerAvatarUrl = avatarUrl ?: current.partnerAvatarUrl,
                    partnerStatusMood = mood ?: current.partnerStatusMood
                )
                _pairingInfo.value = updated
                savePairingLocally(updated, prefs.getString("partner_pair_code", "") ?: "")
            }
        }
    }

    private fun handleIncomingPairRequest(partnerUser: UserAccount) {
        val myUser = authRepository.currentUser.value ?: return
        val partnerCode = partnerUser.pairCode.trim().uppercase()
        val roomId = computeRoomId(myUser.pairCode, partnerCode)

        val newPairing = PairingInfo(
            isPaired = true,
            partnerId = partnerUser.id,
            partnerUsername = partnerUser.username.ifBlank { "partner_account" },
            partnerDisplayName = partnerUser.displayName.ifBlank { partnerUser.username.ifBlank { "Partner" } },
            partnerAvatarEmoji = partnerUser.avatarEmoji.ifBlank { "🌸" },
            partnerAvatarUrl = partnerUser.customAvatarUrl,
            partnerStatusMood = partnerUser.statusMood ?: "✨ Connected via Lik",
            partnerPhoneNumber = null,
            partnerIsOnline = true,
            partnerLastSeen = System.currentTimeMillis()
        )

        savePairingLocally(newPairing, partnerCode)
        scope.launch {
            authRepository.setPairedUser(partnerUser.id)
        }
        _pairingInfo.value = newPairing

        // Join the shared room and reply with pair_accept handshake
        syncManager.setRoom(roomId)
        syncManager.publishPairAccept(partnerCode, myUser)
    }

    private fun handleIncomingPairAccepted(partnerUser: UserAccount) {
        val partnerCode = partnerUser.pairCode.trim().uppercase()
        val current = _pairingInfo.value

        val updated = current.copy(
            isPaired = true,
            partnerId = partnerUser.id,
            partnerUsername = partnerUser.username.ifBlank { "partner_account" },
            partnerDisplayName = partnerUser.displayName.ifBlank { current.partnerDisplayName ?: "Partner" },
            partnerAvatarEmoji = partnerUser.avatarEmoji.ifBlank { current.partnerAvatarEmoji ?: "🌸" },
            partnerAvatarUrl = partnerUser.customAvatarUrl ?: current.partnerAvatarUrl,
            partnerStatusMood = partnerUser.statusMood ?: current.partnerStatusMood,
            partnerPhoneNumber = current.partnerPhoneNumber
        )

        savePairingLocally(updated, partnerCode)
        scope.launch {
            authRepository.setPairedUser(partnerUser.id)
        }
        _pairingInfo.value = updated
    }

    private fun loadPairingInfo(): PairingInfo {
        val isPaired = prefs.getBoolean("is_paired", false)
        if (!isPaired) return PairingInfo(isPaired = false)

        val now = System.currentTimeMillis()
        val defaultConnectedSince = now - (14L * 24 * 60 * 60 * 1000L) // Default ~14 days if not previously set
        val connectedSince = prefs.getLong("connected_since", defaultConnectedSince)
        val anniversaryDate = prefs.getLong("anniversary_date", connectedSince)

        return PairingInfo(
            isPaired = true,
            partnerId = prefs.getString("partner_id", "partner_default_id"),
            partnerUsername = prefs.getString("partner_username", "Girlfriend"),
            partnerDisplayName = prefs.getString("partner_display_name", "Partner ❤️"),
            partnerAvatarEmoji = prefs.getString("partner_emoji", "🌸"),
            partnerAvatarUrl = prefs.getString("partner_avatar_url", null),
            partnerStatusMood = prefs.getString("partner_status_mood", "✨ Online"),
            partnerPhoneNumber = prefs.getString("partner_phone", "+123456789"),
            partnerIsOnline = true,
            partnerLastSeen = System.currentTimeMillis(),
            connectedSinceTimestamp = connectedSince,
            anniversaryDateMs = anniversaryDate
        )
    }

    suspend fun pairWithCode(inputCode: String): Result<PairingInfo> {
        val cleanCode = inputCode.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a valid Chat Code."))
        }

        val myUser = authRepository.currentUser.value
        val myCode = myUser?.pairCode ?: "LIK-ME"

        if (cleanCode == myCode.trim().uppercase()) {
            return Result.failure(IllegalArgumentException("You cannot connect to your own Chat Code! Enter your partner's code."))
        }

        val partnerId = "partner_${cleanCode.replace("-", "").takeLast(6)}"
        val roomId = computeRoomId(myCode, cleanCode)

        val now = System.currentTimeMillis()
        val newPairing = PairingInfo(
            isPaired = true,
            partnerId = partnerId,
            partnerUsername = "partner_${cleanCode.replace("-", "").takeLast(4)}",
            partnerDisplayName = "Partner ($cleanCode)",
            partnerAvatarEmoji = "🌸",
            partnerAvatarUrl = null,
            partnerStatusMood = "✨ Connected via Lik",
            partnerPhoneNumber = "",
            partnerIsOnline = true,
            partnerLastSeen = now,
            connectedSinceTimestamp = now,
            anniversaryDateMs = now
        )

        savePairingLocally(newPairing, cleanCode)
        authRepository.setPairedUser(partnerId)
        _pairingInfo.value = newPairing

        // Join room and broadcast pair request over the internet
        syncManager.setRoom(roomId)
        if (myUser != null) {
            syncManager.publishPairRequest(cleanCode, myUser)
        }

        return Result.success(newPairing)
    }

    suspend fun createQuickDemoPairing(partnerName: String = "My Partner ❤️"): PairingInfo {
        val partnerId = "partner_quick_link"
        val cleanCode = "LIK-DEMO"
        val now = System.currentTimeMillis()
        val demoConnectedSince = now - (42L * 24 * 60 * 60 * 1000L) // 42 days ago
        val newPairing = PairingInfo(
            isPaired = true,
            partnerId = partnerId,
            partnerUsername = "partner_user",
            partnerDisplayName = partnerName,
            partnerAvatarEmoji = "🌸",
            partnerAvatarUrl = null,
            partnerPhoneNumber = "+1 555-0100",
            partnerIsOnline = true,
            partnerLastSeen = now,
            connectedSinceTimestamp = demoConnectedSince,
            anniversaryDateMs = demoConnectedSince
        )

        savePairingLocally(newPairing, cleanCode)
        authRepository.setPairedUser(partnerId)
        _pairingInfo.value = newPairing
        return newPairing
    }

    private fun savePairingLocally(info: PairingInfo, partnerCode: String) {
        prefs.edit()
            .putBoolean("is_paired", info.isPaired)
            .putString("partner_id", info.partnerId)
            .putString("partner_pair_code", partnerCode)
            .putString("partner_username", info.partnerUsername)
            .putString("partner_display_name", info.partnerDisplayName)
            .putString("partner_emoji", info.partnerAvatarEmoji)
            .putString("partner_avatar_url", info.partnerAvatarUrl)
            .putString("partner_status_mood", info.partnerStatusMood)
            .putString("partner_phone", info.partnerPhoneNumber)
            .putLong("connected_since", info.connectedSinceTimestamp)
            .putLong("anniversary_date", info.anniversaryDateMs)
            .apply()

        scope.launch {
            if (info.isPaired && info.partnerId != null) {
                partnerDao.savePartnerProfile(
                    PartnerProfileEntity(
                        partnerId = info.partnerId,
                        partnerUsername = info.partnerUsername ?: "",
                        partnerDisplayName = info.partnerDisplayName ?: "",
                        partnerAvatarEmoji = info.partnerAvatarEmoji ?: "🌸",
                        partnerAvatarUrl = info.partnerAvatarUrl,
                        partnerStatusMood = info.partnerStatusMood,
                        partnerPhoneNumber = info.partnerPhoneNumber,
                        isPaired = true,
                        connectedSinceTimestamp = info.connectedSinceTimestamp,
                        anniversaryDateMs = info.anniversaryDateMs
                    )
                )
            }
        }
    }

    fun updatePartnerDp(avatarUrl: String?) {
        val current = _pairingInfo.value
        val updated = current.copy(partnerAvatarUrl = avatarUrl)
        _pairingInfo.value = updated
        prefs.edit().putString("partner_avatar_url", avatarUrl).apply()

        scope.launch {
            if (current.partnerId != null) {
                partnerDao.updatePartnerAvatarUrl(current.partnerId, avatarUrl)
            }
        }
    }

    fun updatePartnerStatusMood(status: String?) {
        val current = _pairingInfo.value
        val updated = current.copy(partnerStatusMood = status)
        _pairingInfo.value = updated
        prefs.edit().putString("partner_status_mood", status).apply()

        scope.launch {
            if (current.partnerId != null) {
                val partnerEntity = partnerDao.getPartnerProfileOnce()
                if (partnerEntity != null) {
                    partnerDao.savePartnerProfile(partnerEntity.copy(partnerStatusMood = status))
                }
            }
            // Also notify partner over MQTT if paired
            val myUser = authRepository.currentUser.value
            val partnerCode = prefs.getString("partner_pair_code", "") ?: ""
            if (myUser != null && partnerCode.isNotBlank()) {
                val roomId = computeRoomId(myUser.pairCode, partnerCode)
                syncManager.publishProfileUpdate(roomId, myUser.copy(statusMood = status))
            }
        }
    }

    fun updateAnniversaryDate(anniversaryMs: Long) {
        val current = _pairingInfo.value
        val updated = current.copy(anniversaryDateMs = anniversaryMs)
        _pairingInfo.value = updated
        prefs.edit().putLong("anniversary_date", anniversaryMs).apply()

        scope.launch {
            if (current.partnerId != null) {
                val partnerEntity = partnerDao.getPartnerProfileOnce()
                if (partnerEntity != null) {
                    partnerDao.savePartnerProfile(partnerEntity.copy(anniversaryDateMs = anniversaryMs))
                }
            }
            // Notify partner over MQTT if connected
            val myUser = authRepository.currentUser.value
            val partnerCode = prefs.getString("partner_pair_code", "") ?: ""
            if (myUser != null && partnerCode.isNotBlank()) {
                val roomId = computeRoomId(myUser.pairCode, partnerCode)
                syncManager.publishProfileUpdate(roomId, myUser)
            }
        }
    }

    fun unpair() {
        prefs.edit().clear().apply()
        _pairingInfo.value = PairingInfo(isPaired = false)
        scope.launch {
            partnerDao.clearPartnerProfile()
        }
    }

    private fun computeRoomId(codeA: String, codeB: String): String {
        val cleanA = codeA.trim().uppercase().replace("-", "")
        val cleanB = codeB.trim().uppercase().replace("-", "")
        return "lik_room_" + listOf(cleanA, cleanB).sorted().joinToString("_")
    }
}

