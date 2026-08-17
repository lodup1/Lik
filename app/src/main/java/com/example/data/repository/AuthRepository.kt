package com.example.data.repository

import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepository(private val userDao: UserDao) {

    val currentUser: Flow<User?> = userDao.getUser("current_user").map { entity ->
        entity?.let {
            User(
                id = it.id,
                username = it.username,
                displayName = it.displayName,
                pairCode = it.pairCode,
                pairedPartnerId = it.pairedPartnerId,
                avatarEmoji = it.avatarEmoji,
                statusMessage = it.statusMessage,
                currentMood = it.currentMood,
                anniversaryDate = it.anniversaryDate
            )
        }
    }

    suspend fun loginOrRegister(displayName: String, username: String) {
        val existing = userDao.getUserSync("current_user")
        val pairCode = existing?.pairCode ?: "LIK-${(1000..9999).random()}"
        val user = UserEntity(
            id = "current_user",
            username = username.ifBlank { "User" },
            displayName = displayName.ifBlank { "You" },
            pairCode = pairCode,
            pairedPartnerId = existing?.pairedPartnerId,
            avatarEmoji = existing?.avatarEmoji ?: "❤️",
            statusMessage = existing?.statusMessage ?: "Connected on Lik",
            currentMood = existing?.currentMood ?: "Happy",
            anniversaryDate = existing?.anniversaryDate
        )
        userDao.insertUser(user)
    }

    suspend fun updateProfile(displayName: String, avatarEmoji: String, status: String, mood: String) {
        val existing = userDao.getUserSync("current_user") ?: return
        val updated = existing.copy(
            displayName = displayName,
            avatarEmoji = avatarEmoji,
            statusMessage = status,
            currentMood = mood
        )
        userDao.updateUser(updated)
    }

    suspend fun setAnniversary(timestamp: Long) {
        val existing = userDao.getUserSync("current_user") ?: return
        userDao.updateUser(existing.copy(anniversaryDate = timestamp))
    }

    suspend fun logout() {
        userDao.clearUsers()
    }
}
