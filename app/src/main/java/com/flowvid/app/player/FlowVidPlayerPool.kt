package com.flowvid.app.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

/**
 * Owns a small, fixed number of [ExoPlayer] instances and hands them out by
 * caller-chosen "slot key" (typically a video's stable Uri-derived key).
 *
 * This is the core of FlowVid's performance story: no matter how large the
 * library is (tens or thousands of videos), memory and CPU use is bounded by
 * [poolSize] concrete players. Callers are expected to call [trimTo] whenever
 * the active window changes so far-away players are freed for reuse.
 *
 * Only one player should ever be "active" (audible + playing) at a time;
 * this class does not enforce that itself, callers (FeedViewModel) do, by
 * only ever restoring volume/play-when-ready on the single active slot.
 */
class FlowVidPlayerPool(
    private val context: Context,
    private val poolSize: Int = 3,
    private val onPlayerError: (key: String, error: PlaybackException) -> Unit = { _, _ -> },
    private val onPlaybackEnded: (key: String) -> Unit = {},
) {
    private data class Entry(val player: ExoPlayer, var assignedKey: String? = null)

    private val entries: MutableList<Entry> = MutableList(poolSize) { Entry(buildPlayer()) }.also { list ->
        list.forEach { entry ->
            entry.player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    entry.assignedKey?.let { onPlayerError(it, error) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        entry.assignedKey?.let { onPlaybackEnded(it) }
                    }
                }
            })
        }
    }

    private fun buildPlayer(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 30_000,
                /* bufferForPlaybackMs = */ 300,
                /* bufferForPlaybackAfterRebufferMs = */ 1_000,
            )
            .build()
        return ExoPlayer.Builder(context.applicationContext)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = false
                volume = 0f
            }
    }

    /** Returns the player currently backing [key], preparing a free (or recycled) one if needed. */
    fun playerFor(key: String, uri: Uri): ExoPlayer {
        entries.find { it.assignedKey == key }?.let { return it.player }

        val free = entries.find { it.assignedKey == null } ?: entries.first().also {
            // Pool exhausted (shouldn't normally happen if callers call trimTo before
            // requesting a wider window than poolSize) — recycle the first entry.
            it.player.stop()
            it.player.clearMediaItems()
        }
        free.assignedKey = key
        free.player.setMediaItem(MediaItem.fromUri(uri))
        free.player.volume = 0f
        free.player.playWhenReady = false
        free.player.prepare()
        return free.player
    }

    /** Returns the player already assigned to [key], without creating or preparing anything. */
    fun peek(key: String): ExoPlayer? = entries.find { it.assignedKey == key }?.player

    /** Frees any player whose slot key isn't in [keysToKeep], stopping it so it can be recycled. */
    fun trimTo(keysToKeep: Set<String>) {
        entries.forEach { entry ->
            val key = entry.assignedKey
            if (key != null && key !in keysToKeep) {
                entry.player.stop()
                entry.player.clearMediaItems()
                entry.player.volume = 0f
                entry.assignedKey = null
            }
        }
    }

    /** Mutes and pauses every player except [activeKey]; used to guarantee single-stream audio. */
    fun ensureOnlyActive(activeKey: String?) {
        entries.forEach { entry ->
            if (entry.assignedKey != activeKey) {
                entry.player.volume = 0f
                entry.player.playWhenReady = false
            }
        }
    }

    /** Pauses every player (e.g. on app backgrounding) without releasing them. */
    fun pauseAll() {
        entries.forEach { it.player.playWhenReady = false }
    }

    fun releaseAll() {
        entries.forEach { it.player.release() }
        entries.clear()
    }
}

/** Resolves the on-video-end behavior from the user's two related settings (see AppSettings). */
fun resolveRepeatMode(autoLoop: Boolean, autoAdvance: Boolean): Int = when {
    autoAdvance -> Player.REPEAT_MODE_OFF
    autoLoop -> Player.REPEAT_MODE_ONE
    else -> Player.REPEAT_MODE_OFF
}
