package com.flowvid.app.ui.feed.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A pulse that fades in, holds briefly, then fades out. [pulseKey] should
 * change (e.g. increment a counter) each time a new pulse should play, even
 * if [isPlaying] didn't change value, so repeated taps always re-trigger it.
 */
@Composable
fun PlayPausePulse(
    pulseKey: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(pulseKey) {
        if (pulseKey == 0) return@LaunchedEffect
        visible = true
        delay(180)
        visible = false
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (visible) 120 else 220),
        label = "playPausePulseAlpha",
    )

    if (alpha > 0f) {
        Box(
            modifier = modifier
                .size(84.dp)
                .alpha(alpha)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp).align(androidx.compose.ui.Alignment.Center),
            )
        }
    }
}
