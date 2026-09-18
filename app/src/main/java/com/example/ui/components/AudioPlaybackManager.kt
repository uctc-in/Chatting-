package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlaybackManager {
    var playingMessageId by mutableStateOf<String?>(null)
        private set
    var playbackProgress by mutableFloatStateOf(0f)
        private set
    var playbackSpeed by mutableFloatStateOf(1.0f)
        private set

    private var playbackJob: Job? = null
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {}
    }

    fun togglePlay(messageId: String, durationSeconds: Int, scope: CoroutineScope) {
        if (playingMessageId == messageId) {
            pause()
        } else {
            play(messageId, durationSeconds, scope)
        }
    }

    fun play(messageId: String, durationSeconds: Int, scope: CoroutineScope) {
        playbackJob?.cancel()
        playingMessageId = messageId
        playbackProgress = 0f

        toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)

        playbackJob = scope.launch(Dispatchers.Main) {
            val totalMs = (durationSeconds.coerceAtLeast(1) * 1000).toLong()
            val stepMs = 50L
            var elapsed = 0L

            while (isActive && elapsed < totalMs) {
                delay((stepMs / playbackSpeed).toLong())
                elapsed += stepMs
                playbackProgress = (elapsed.toFloat() / totalMs).coerceIn(0f, 1f)
            }
            playbackProgress = 1f
            delay(150)
            playingMessageId = null
            playbackProgress = 0f
        }
    }

    fun pause() {
        playbackJob?.cancel()
        playingMessageId = null
        playbackProgress = 0f
    }

    fun cycleSpeed() {
        playbackSpeed = when (playbackSpeed) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
    }

    fun seekTo(progress: Float) {
        playbackProgress = progress.coerceIn(0f, 1f)
    }
}
