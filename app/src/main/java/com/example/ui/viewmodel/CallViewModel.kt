package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallDirection
import com.example.data.model.CallLogEntity
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.data.repository.CallRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class CallStatus {
    RINGING, CONNECTED, ENDED
}

data class ActiveCallState(
    val contact: ContactEntity,
    val callType: CallType,
    val status: CallStatus = CallStatus.RINGING,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = true,
    val isFrontCamera: Boolean = true
)

class CallViewModel(private val callRepository: CallRepository) : ViewModel() {

    val callLogs: StateFlow<List<CallLogEntity>> = callRepository.callLogsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private var durationJob: Job? = null

    fun startCall(contact: ContactEntity, type: CallType) {
        durationJob?.cancel()
        _activeCall.value = ActiveCallState(
            contact = contact,
            callType = type,
            status = CallStatus.RINGING,
            isVideoEnabled = (type == CallType.VIDEO)
        )

        // Connect after 2.5 seconds ringing
        viewModelScope.launch {
            delay(2500)
            if (_activeCall.value?.status == CallStatus.RINGING) {
                _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
                startTimer()
            }
        }
    }

    private fun startTimer() {
        durationJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { current ->
                    current.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleVideo() {
        _activeCall.value = _activeCall.value?.let { it.copy(isVideoEnabled = !it.isVideoEnabled) }
    }

    fun switchCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun endCall() {
        durationJob?.cancel()
        val current = _activeCall.value
        if (current != null) {
            viewModelScope.launch {
                callRepository.recordCall(
                    contactId = current.contact.id,
                    contactName = current.contact.name,
                    contactAvatar = current.contact.avatarUrl,
                    callType = current.callType,
                    direction = CallDirection.OUTGOING,
                    durationSeconds = current.durationSeconds
                )
            }
        }
        _activeCall.value = null
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            callRepository.clearHistory()
        }
    }
}
