package com.flowvid.app.ui.feed

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.media3.common.Player
import com.flowvid.app.domain.model.Video
import com.flowvid.app.ui.feed.components.PlayPausePulse
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * One page of the feed: the full-bleed player plus its tap (play/pause) and
 * press-and-hold (temporary 2x speed boost) gestures. Vertical swiping is
 * handled entirely by the parent [androidx.compose.foundation.pager.VerticalPager]
 * and is never touched here, so the two gesture systems can't conflict:
 * a real swipe is recognized as a drag by the pager before this composable's
 * simple tap/press detector ever resolves it as anything.
 */
@Composable
fun VideoPageContent(
    video: Video,
    player: Player?,
    isActivePage: Boolean,
    zoomToFill: Boolean,
    gestureSpeedBoostEnabled: Boolean,
    onTogglePlayPause: () -> Unit,
    onSpeedBoostChanged: (Boolean) -> Unit,
    onProgressChanged: (fraction: Float, positionMs: Long, durationMs: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pulseKey by remember { mutableIntStateOf(0) }
    var isPlayingForPulse by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val longPressTimeoutMillis = LocalViewConfiguration.current.longPressTimeoutMillis

    Box(modifier = modifier.fillMaxSize()) {
        PlayerSurfaceView(
            player = player,
            zoomToFill = zoomToFill,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gestureSpeedBoostEnabled, player) {
                    detectTapGestures(
                        onPress = {
                            var boostActivated = false
                            val boostJob = if (gestureSpeedBoostEnabled) {
                                scope.launch {
                                    delay(longPressTimeoutMillis)
                                    boostActivated = true
                                    onSpeedBoostChanged(true)
                                }
                            } else {
                                null
                            }

                            // Suspends until the pointer lifts, or returns false if the
                            // gesture was cancelled elsewhere (e.g. the pager claimed a
                            // vertical drag) — either way we must stop the boost here.
                            tryAwaitRelease()
                            boostJob?.cancel()
                            if (boostActivated) {
                                onSpeedBoostChanged(false)
                            }
                        },
                        onTap = {
                            isPlayingForPulse = player?.playWhenReady?.not() ?: true
                            onTogglePlayPause()
                            pulseKey++
                        },
                    )
                },
        )

        PlayPausePulse(
            pulseKey = pulseKey,
            isPlaying = isPlayingForPulse,
            modifier = Modifier.align(Alignment.Center),
        )
    }

    // Poll playback position while this page is active; avoids needing a
    // dedicated Player.Listener just for progress-bar updates.
    LaunchedEffect(player, isActivePage) {
        if (player == null || !isActivePage) return@LaunchedEffect
        while (isActive) {
            val duration = player.duration.coerceAtLeast(0)
            val position = player.currentPosition.coerceAtLeast(0)
            val fraction = if (duration > 0) (position.toFloat() / duration.toFloat()) else 0f
            onProgressChanged(fraction, position, duration)
            delay(200)
        }
    }
}
