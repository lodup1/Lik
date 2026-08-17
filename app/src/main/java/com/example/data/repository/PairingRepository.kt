package com.example.data.repository

import com.example.data.local.UserDao

class PairingRepository(private val userDao: UserDao) {

    suspend fun pairWithCode(enteredCode: String): Result<String> {
        val trimmed = enteredCode.trim().uppercase()
        if (!trimmed.startsWith("LIK-") || trimmed.length != 8) {
            return Result.failure(IllegalArgumentException("Invalid Lik code format. Use LIK-XXXX"))
        }

        val currentUser = userDao.getUserSync("current_user")
            ?: return Result.failure(IllegalStateException("No current user logged in"))

        if (currentUser.pairCode.equals(trimmed, ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("You cannot pair with your own code"))
        }

        val updatedUser = currentUser.copy(pairedPartnerId = trimmed)
        userDao.updateUser(updatedUser)
        return Result.success("Successfully connected with partner!")
    }

    suspend fun unpair(): Result<Unit> {
        val currentUser = userDao.getUserSync("current_user") ?: return Result.success(Unit)
        userDao.updateUser(currentUser.copy(pairedPartnerId = null))
        return Result.success(Unit)
    }
}
