package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun signInWithGmail(email: String, name: String = "", avatarUrl: String = "", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            userRepository.signInWithGmail(email = email, name = name, avatarUrl = avatarUrl)
            onDone()
        }
    }

    fun signInWithPhone(phone: String, name: String = "", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            userRepository.signInWithPhone(phone = phone, name = name)
            onDone()
        }
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            userRepository.updateProfile(name = name, bio = bio, avatarUrl = avatarUrl)
        }
    }

    fun signOut(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            userRepository.signOut()
            onDone()
        }
    }
}
