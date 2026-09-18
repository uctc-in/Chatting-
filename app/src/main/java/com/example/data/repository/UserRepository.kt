package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AuthProvider
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()

    val currentUserFlow: Flow<UserEntity?> = userDao.getCurrentUserFlow()

    suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()

    suspend fun signInWithGmail(
        email: String,
        name: String,
        avatarUrl: String = ""
    ): UserEntity {
        val user = UserEntity(
            id = "current_user",
            name = name.ifBlank { email.substringBefore("@").replace(".", " ").capitalizeWords() },
            email = email,
            phone = null,
            avatarUrl = avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150" },
            bio = "Active on Pulse Communication | Available for calls & chats",
            authProvider = AuthProvider.GMAIL.name,
            createdAt = System.currentTimeMillis(),
            isOnline = true
        )
        userDao.saveUser(user)
        return user
    }

    suspend fun signInWithPhone(
        phone: String,
        name: String = ""
    ): UserEntity {
        val user = UserEntity(
            id = "current_user",
            name = name.ifBlank { "Mobile User ${phone.takeLast(4)}" },
            email = null,
            phone = phone,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
            bio = "Active on Pulse Communication | Available for calls & chats",
            authProvider = AuthProvider.PHONE.name,
            createdAt = System.currentTimeMillis(),
            isOnline = true
        )
        userDao.saveUser(user)
        return user
    }

    suspend fun updateProfile(name: String, bio: String, avatarUrl: String) {
        val current = userDao.getCurrentUser() ?: return
        val updated = current.copy(
            name = name,
            bio = bio,
            avatarUrl = avatarUrl
        )
        userDao.saveUser(updated)
    }

    suspend fun signOut() {
        userDao.clearUser()
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
