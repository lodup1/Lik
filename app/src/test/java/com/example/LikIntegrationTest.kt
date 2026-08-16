package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.remote.LikRealtimeSyncManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.PairingRepository
import com.example.data.util.PasswordSecurity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LikIntegrationTest {

    private lateinit var context: Context
    private lateinit var authRepository: AuthRepository
    private lateinit var pairingRepository: PairingRepository
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getDatabase(context)
        val syncManager = LikRealtimeSyncManager(context)
        authRepository = AuthRepository(context, syncManager)
        pairingRepository = PairingRepository(context, authRepository, syncManager)
    }

    @Test
    fun testRegistrationAndLoginFlow() = runBlocking {
        val username = "alice_test"
        val password = "secretPassword123"
        val dob = "2000-01-15"

        // 1. Check availability
        val isAvailableInitially = authRepository.isUsernameAvailable(username)
        assertTrue(isAvailableInitially)

        // 2. Register
        val regResult = authRepository.register(username, password, dob)
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)
        assertEquals(username, user?.username)
        assertEquals(dob, user?.dateOfBirth)

        // 3. Username should no longer be available
        val isAvailableAfter = authRepository.isUsernameAvailable(username)
        assertFalse(isAvailableAfter)

        // 4. Duplicate registration must fail
        val duplicateRegResult = authRepository.register(username, "otherPass", "1999-05-20")
        assertTrue(duplicateRegResult.isFailure)

        // 5. Logout
        authRepository.logout()
        val currentUserAfterLogout = authRepository.currentUser.value
        assertEquals(null, currentUserAfterLogout)

        // 6. Login with incorrect password should fail
        val wrongLoginResult = authRepository.login(username, "wrongPassword")
        assertTrue(wrongLoginResult.isFailure)

        // 7. Login with correct password should succeed
        val correctLoginResult = authRepository.login(username, password)
        assertTrue(correctLoginResult.isSuccess)
        val loggedInUser = correctLoginResult.getOrNull()
        assertNotNull(loggedInUser)
        assertEquals(username, loggedInUser?.username)
    }

    @Test
    fun testChatCodePairingFlowAndRoomPersistence() = runBlocking {
        val username = "bob_pair_test"
        val password = "passwordBob123"
        val dob = "1998-11-20"

        val regResult = authRepository.register(username, password, dob)
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)

        val myCode = user?.pairCode ?: ""
        assertTrue("My pair code should start with LIK-", myCode.startsWith("LIK-"))

        // Attempt self-pairing should fail
        val selfPairResult = pairingRepository.pairWithCode(myCode)
        assertTrue(selfPairResult.isFailure)

        // Attempt pairing with another valid code
        val partnerCode = "LIK-9876"
        val pairResult = pairingRepository.pairWithCode(partnerCode)
        assertTrue(pairResult.isSuccess)
        val pairingInfo = pairResult.getOrNull()
        assertNotNull(pairingInfo)
        assertTrue(pairingInfo?.isPaired == true)

        // Verify Room persistence via PartnerDao
        val partnerDao = database.partnerDao()
        val savedEntity = partnerDao.getPartnerProfileOnce()
        assertNotNull("Partner entity should be saved in Room", savedEntity)
        assertEquals(true, savedEntity?.isPaired)
    }

    @Test
    fun testRoomMessagePersistenceAndDeduplication() = runBlocking {
        val chatDao = database.chatDao()
        val msgId = "msg_test_123"

        val msg1 = ChatMessageEntity(
            id = msgId,
            senderId = "user_a",
            receiverId = "user_b",
            text = "Hello Device B",
            mediaType = "NONE",
            mediaUrl = null,
            mediaSizeFormatted = null,
            uploadProgress = 1.0f,
            timestamp = 1000L,
            status = "SENT",
            replyToId = null,
            replyToText = null,
            replyToSenderName = null,
            isDeleted = false
        )

        chatDao.insertMessage(msg1)
        var retrieved = chatDao.getMessageById(msgId)
        assertNotNull(retrieved)
        assertEquals("Hello Device B", retrieved?.text)
        assertEquals("SENT", retrieved?.status)

        // Insert duplicate ID with updated status (delivered) - should update, not duplicate
        val msg2 = msg1.copy(status = "DELIVERED")
        chatDao.insertMessage(msg2)

        retrieved = chatDao.getMessageById(msgId)
        assertNotNull(retrieved)
        assertEquals("DELIVERED", retrieved?.status)
    }

    @Test
    fun testPasswordSecurityHashing() {
        val plain = "mySecretPassword!2026"
        val salt = PasswordSecurity.generateSalt()
        val hash = PasswordSecurity.hashPassword(plain, salt)

        // Verifications
        assertFalse(hash.contains(plain))
        assertTrue(PasswordSecurity.verifyPassword(plain, salt, hash))
        assertFalse(PasswordSecurity.verifyPassword("wrong", salt, hash))
    }
}
