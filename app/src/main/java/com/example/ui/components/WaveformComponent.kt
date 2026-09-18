package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioWaveform(
    waveformString: String,
    progress: Float, // 0f to 1f
    playedColor: Color,
    unplayedColor: Color,
    modifier: Modifier = Modifier,
    barHeight: Dp = 32.dp,
    onSeek: ((Float) -> Unit)? = null
) {
    val amplitudes = remember(waveformString) {
        if (waveformString.isNotBlank()) {
            waveformString.split(",").mapNotNull { it.trim().toFloatOrNull() }
        } else {
            listOf(30f, 60f, 85f, 40f, 75f, 95f, 50f, 35f, 70f, 90f, 65f, 40f, 80f, 55f, 30f)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .pointerInput(onSeek) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek?.invoke(frac)
                }
            }
    ) {
        val totalBars = amplitudes.size.coerceAtLeast(1)
        val barWidth = (size.width / (totalBars * 1.6f)).coerceIn(3f, 12f)
        val spacing = (size.width - (barWidth * totalBars)) / (totalBars - 1).coerceAtLeast(1)

        val progressX = size.width * progress

        for (i in 0 until totalBars) {
            val amp = (amplitudes.getOrElse(i) { 40f } / 100f).coerceIn(0.15f, 1f)
            val currentBarHeight = size.height * amp
            val x = i * (barWidth + spacing)
            val y = (size.height - currentBarHeight) / 2f

            val color = if (x <= progressX) playedColor else unplayedColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
