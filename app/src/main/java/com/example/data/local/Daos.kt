package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Query("UPDATE chat_messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("UPDATE chat_messages SET isDeleted = 1, text = 'This message was deleted' WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Dao
interface UserDao {
    // Registered Accounts Management
    @Query("SELECT * FROM registered_accounts WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getAccountByUsername(username: String): RegisteredAccountEntity?

    @Query("SELECT * FROM registered_accounts WHERE id = :userId LIMIT 1")
    suspend fun getAccountById(userId: String): RegisteredAccountEntity?

    @Query("SELECT COUNT(*) FROM registered_accounts WHERE LOWER(username) = LOWER(:username)")
    suspend fun countUsername(username: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: RegisteredAccountEntity)

    @Update
    suspend fun updateAccount(account: RegisteredAccountEntity)

    // Active User Session Management
    @Query("SELECT * FROM user_session WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserSessionEntity?

    @Query("SELECT * FROM user_session LIMIT 1")
    fun getActiveSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getActiveSessionOnce(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}

@Dao
interface PartnerDao {
    @Query("SELECT * FROM partner_profile LIMIT 1")
    fun getPartnerProfile(): Flow<PartnerProfileEntity?>

    @Query("SELECT * FROM partner_profile LIMIT 1")
    suspend fun getPartnerProfileOnce(): PartnerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePartnerProfile(partner: PartnerProfileEntity)

    @Query("UPDATE partner_profile SET partnerAvatarUrl = :avatarUrl WHERE partnerId = :partnerId")
    suspend fun updatePartnerAvatarUrl(partnerId: String, avatarUrl: String?)

    @Query("DELETE FROM partner_profile")
    suspend fun clearPartnerProfile()
}
