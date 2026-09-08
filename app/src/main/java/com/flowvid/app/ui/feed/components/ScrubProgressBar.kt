package com.flowvid.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * A very thin (2dp) scrubbable line, deliberately far from a Material Slider
 * in appearance — the video should stay the visual focus.
 *
 * [progressFraction] and [onScrub]/[onScrubFinished] operate in 0f..1f so the
 * caller doesn't need to know pixel widths.
 */
@Composable
fun ScrubProgressBar(
    progressFraction: Float,
    onScrubStart: () -> Unit,
    onScrub: (fraction: Float) -> Unit,
    onScrubFinished: (fraction: Float) -> Unit,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
    fillColor: Color = Color(0xFFE3A83B),
) {
    // -1f means "not currently being dragged"; any 0f..1f value is a live drag position.
    var dragFraction by remember { mutableFloatStateOf(-1f) }
    val shownFraction = (if (dragFraction >= 0f) dragFraction else progressFraction).coerceIn(0f, 1f)
    val barHeight = if (dragFraction >= 0f) 3.dp else 2.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp) // generous touch target; the visible bar itself is thin
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onScrubStart()
                    onScrub(fraction)
                    onScrubFinished(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                        onScrubStart()
                    },
                    onDragEnd = {
                        val finalFraction = dragFraction.coerceIn(0f, 1f)
                        dragFraction = -1f
                        onScrubFinished(finalFraction)
                    },
                    onDragCancel = { dragFraction = -1f },
                ) { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    dragFraction = fraction
                    onScrub(fraction)
                },
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // Track (full width, always visible)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(trackColor),
        )
        // Fill (proportional to progress, drawn on top of the track)
        Box(
            modifier = Modifier
                .fillMaxWidth(shownFraction.coerceAtLeast(0.001f))
                .height(barHeight)
                .clip(RoundedCornerShape(50))
                .background(fillColor),
        )
    }
}
