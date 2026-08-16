package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.RegisteredAccountEntity
import com.example.data.local.UserSessionEntity
import com.example.data.model.UserAccount
import com.example.data.remote.LikRealtimeSyncManager
import com.example.data.util.PasswordSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val context: Context,
    val syncManager: LikRealtimeSyncManager
) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lik_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    val activeSessionFlow: Flow<UserAccount?> = userDao.getActiveSession().map { entity ->
        entity?.let {
            UserAccount(
                id = it.id,
                username = it.username,
                dateOfBirth = it.dateOfBirth,
                displayName = it.displayName,
                avatarEmoji = it.avatarEmoji,
                customAvatarUrl = it.customAvatarUrl,
                statusMood = it.statusMood,
                pairCode = it.pairCode,
                pairedUserId = it.pairedUserId
            )
        }
    }

    init {
        loadStoredSession()
    }

    private fun loadStoredSession() {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val id = prefs.getString("user_id", null) ?: UUID.randomUUID().toString()
            val username = prefs.getString("user_username", "") ?: ""
            val dob = prefs.getString("user_dob", "") ?: ""
            val displayName = prefs.getString("user_display_name", "") ?: ""
            val avatarEmoji = prefs.getString("user_avatar_emoji", "❤️") ?: "❤️"
            val customAvatarUrl = prefs.getString("user_avatar_url", null)
            val statusMood = prefs.getString("user_status_mood", null)
            val pairCode = prefs.getString("user_pair_code", generatePairCode()) ?: generatePairCode()
            val pairedUserId = prefs.getString("user_paired_id", null)

            val account = UserAccount(
                id = id,
                username = username,
                dateOfBirth = dob,
                displayName = if (displayName.isBlank()) username else displayName,
                avatarEmoji = avatarEmoji,
                customAvatarUrl = customAvatarUrl,
                statusMood = statusMood,
                pairCode = pairCode,
                pairedUserId = pairedUserId
            )
            _currentUser.value = account
            syncManager.initializeUser(account.id, account.pairCode)
        }
    }

    /**
     * Checks if a username is already registered in the local database.
     */
    suspend fun isUsernameAvailable(username: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = username.trim()
        if (trimmed.isBlank()) return@withContext false
        val count = userDao.countUsername(trimmed)
        return@withContext count == 0
    }

    /**
     * Registers a new user account with secure password hashing and local persistence.
     */
    suspend fun register(
        username: String,
        passwordPlain: String,
        dateOfBirth: String
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = username.trim()
            val cleanDob = dateOfBirth.trim()

            if (cleanUsername.length < 2) {
                return@withContext Result.failure(IllegalArgumentException("Username must be at least 2 characters."))
            }
            if (passwordPlain.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
            }
            if (cleanDob.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Please select your Date of Birth."))
            }

            // Check availability
            val existing = userDao.getAccountByUsername(cleanUsername)
            if (existing != null) {
                return@withContext Result.failure(IllegalStateException("Username is already taken."))
            }

            // Generate cryptographic salt & hash password
            val salt = PasswordSecurity.generateSalt()
            val passwordHash = PasswordSecurity.hashPassword(passwordPlain, salt)
            val userId = "user_${UUID.randomUUID().toString().take(8)}"
            val pairCode = generatePairCode()

            val accountEntity = RegisteredAccountEntity(
                id = userId,
                username = cleanUsername,
                passwordHash = passwordHash,
                passwordSalt = salt,
                dateOfBirth = cleanDob,
                displayName = cleanUsername,
                avatarEmoji = "❤️",
                customAvatarUrl = null,
                statusMood = null,
                pairCode = pairCode,
                pairedUserId = null,
                createdAt = System.currentTimeMillis()
            )

            // Save registered account permanently in Room
            userDao.insertAccount(accountEntity)

            // Create logged-in session
            val sessionUser = UserAccount(
                id = userId,
                username = cleanUsername,
                dateOfBirth = cleanDob,
                displayName = cleanUsername,
                avatarEmoji = "❤️",
                pairCode = pairCode
            )

            saveSessionLocally(sessionUser)
            return@withContext Result.success(sessionUser)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    /**
     * Logs in an existing registered user by verifying their password against the stored hash.
     */
    suspend fun login(
        username: String,
        passwordPlain: String
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val cleanUsername = username.trim()
            if (cleanUsername.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Please enter your username."))
            }
            if (passwordPlain.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Please enter your password."))
            }

            val accountEntity = userDao.getAccountByUsername(cleanUsername)
                ?: return@withContext Result.failure(IllegalArgumentException("Account not found. Please register first."))

            val isPasswordValid = PasswordSecurity.verifyPassword(
                password = passwordPlain,
                salt = accountEntity.passwordSalt,
                expectedHash = accountEntity.passwordHash
            )

            if (!isPasswordValid) {
                return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
            }

            val loggedInUser = UserAccount(
                id = accountEntity.id,
                username = accountEntity.username,
                dateOfBirth = accountEntity.dateOfBirth,
                displayName = accountEntity.displayName.ifBlank { accountEntity.username },
                avatarEmoji = accountEntity.avatarEmoji,
                customAvatarUrl = accountEntity.customAvatarUrl,
                statusMood = accountEntity.statusMood,
                pairCode = accountEntity.pairCode,
                pairedUserId = accountEntity.pairedUserId
            )

            saveSessionLocally(loggedInUser)
            return@withContext Result.success(loggedInUser)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private suspend fun saveSessionLocally(user: UserAccount) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", user.id)
            .putString("user_username", user.username)
            .putString("user_dob", user.dateOfBirth)
            .putString("user_display_name", user.displayName)
            .putString("user_avatar_emoji", user.avatarEmoji)
            .putString("user_avatar_url", user.customAvatarUrl)
            .putString("user_status_mood", user.statusMood)
            .putString("user_pair_code", user.pairCode)
            .putString("user_paired_id", user.pairedUserId)
            .apply()

        userDao.saveSession(
            UserSessionEntity(
                id = user.id,
                username = user.username,
                dateOfBirth = user.dateOfBirth,
                displayName = user.displayName,
                avatarEmoji = user.avatarEmoji,
                customAvatarUrl = user.customAvatarUrl,
                statusMood = user.statusMood,
                pairCode = user.pairCode,
                pairedUserId = user.pairedUserId,
                isLoggedIn = true
            )
        )

        _currentUser.value = user
        syncManager.initializeUser(user.id, user.pairCode)
    }

    suspend fun updateProfile(
        displayName: String,
        avatarEmoji: String,
        customAvatarUrl: String? = null
    ) = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext
        val updated = current.copy(
            displayName = displayName,
            avatarEmoji = avatarEmoji,
            customAvatarUrl = customAvatarUrl
        )
        _currentUser.value = updated

        prefs.edit()
            .putString("user_display_name", displayName)
            .putString("user_avatar_emoji", avatarEmoji)
            .putString("user_avatar_url", customAvatarUrl)
            .apply()

        userDao.saveSession(
            UserSessionEntity(
                id = updated.id,
                username = updated.username,
                dateOfBirth = updated.dateOfBirth,
                displayName = updated.displayName,
                avatarEmoji = updated.avatarEmoji,
                customAvatarUrl = updated.customAvatarUrl,
                statusMood = updated.statusMood,
                pairCode = updated.pairCode,
                pairedUserId = updated.pairedUserId,
                isLoggedIn = true
            )
        )

        // Also update registered accounts table
        val regAccount = userDao.getAccountById(updated.id)
        if (regAccount != null) {
            userDao.updateAccount(
                regAccount.copy(
                    displayName = updated.displayName,
                    avatarEmoji = updated.avatarEmoji,
                    customAvatarUrl = updated.customAvatarUrl
                )
            )
        }
    }

    fun updateStatusMood(status: String?) {
        val current = _currentUser.value ?: return
        val updated = current.copy(statusMood = status)
        _currentUser.value = updated
        prefs.edit().putString("user_status_mood", status).apply()
        scope.launch {
            userDao.saveSession(
                UserSessionEntity(
                    id = updated.id,
                    username = updated.username,
                    dateOfBirth = updated.dateOfBirth,
                    displayName = updated.displayName,
                    avatarEmoji = updated.avatarEmoji,
                    customAvatarUrl = updated.customAvatarUrl,
                    statusMood = updated.statusMood,
                    pairCode = updated.pairCode,
                    pairedUserId = updated.pairedUserId,
                    isLoggedIn = true
                )
            )
            val regAccount = userDao.getAccountById(updated.id)
            if (regAccount != null) {
                userDao.updateAccount(regAccount.copy(statusMood = updated.statusMood))
            }
        }
    }

    suspend fun setPairedUser(partnerId: String) = withContext(Dispatchers.IO) {
        val current = _currentUser.value ?: return@withContext
        val updated = current.copy(pairedUserId = partnerId)
        _currentUser.value = updated

        prefs.edit().putString("user_paired_id", partnerId).apply()

        userDao.saveSession(
            UserSessionEntity(
                id = updated.id,
                username = updated.username,
                dateOfBirth = updated.dateOfBirth,
                displayName = updated.displayName,
                avatarEmoji = updated.avatarEmoji,
                customAvatarUrl = updated.customAvatarUrl,
                statusMood = updated.statusMood,
                pairCode = updated.pairCode,
                pairedUserId = partnerId,
                isLoggedIn = true
            )
        )

        val regAccount = userDao.getAccountById(updated.id)
        if (regAccount != null) {
            userDao.updateAccount(regAccount.copy(pairedUserId = partnerId))
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        userDao.clearSession()
        _currentUser.value = null
    }

    private fun generatePairCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        return "LIK-$randomPart"
    }
}
