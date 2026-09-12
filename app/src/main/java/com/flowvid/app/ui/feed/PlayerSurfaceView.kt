package com.flowvid.app.ui.feed

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * Renders one pooled [Player] full-bleed, letterboxed (never stretched/cropped)
 * unless [zoomToFill] is requested. Controller chrome is always off: FlowVid
 * draws its own minimal, gesture-first UI on top in Compose.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurfaceView(
    player: Player?,
    zoomToFill: Boolean,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                resizeMode = if (zoomToFill) {
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
                // Media3's official workaround for PlayerView-inside-AndroidView
                // leaking/misplacing its Surface in scrolling Compose containers
                // (documented at developer.android.com/media/media3/ui/surface).
                setEnableComposeSurfaceSyncWorkaround(true)
            }
        },
        update = { view ->
            view.player = player
            view.resizeMode = if (zoomToFill) {
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        onRelease = { view -> view.player = null },
    )
}
