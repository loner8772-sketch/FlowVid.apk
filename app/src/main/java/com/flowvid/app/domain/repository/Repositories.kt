package com.flowvid.app.domain.repository

import com.flowvid.app.domain.model.AppSettings
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.ThemeMode
import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.model.VideoFolder
import kotlinx.coroutines.flow.Flow

/**
 * Source of truth for the device's video library, backed by [android.provider.MediaStore].
 * Every method here is local-only: nothing in this interface, or any
 * implementation of it, may perform network I/O.
 */
interface VideoRepository {
    /** Emits the current library every time it changes on disk (insert/delete/rename). */
    fun observeVideos(sortOrder: SortOrder, includedBucketIds: Set<String>?): Flow<List<Video>>

    /** One-shot, unfiltered read of every accessible video, used by search. */
    suspend fun queryAllVideos(sortOrder: SortOrder): List<Video>

    suspend fun queryFolders(): List<VideoFolder>

    /** Confirms whether a single video is still resolvable, used after a playback failure. */
    suspend fun isStillAccessible(video: Video): Boolean

    /** Forces an immediate re-query, used by "Rescan library" and the empty-state retry. */
    suspend fun rescan()
}

interface FavoritesRepository {
    /** Favorite Uris, most-recently-added first, so favorite order is stable and meaningful. */
    fun observeFavoriteUris(): Flow<List<String>>
    suspend fun isFavorite(uri: String): Boolean
    suspend fun toggleFavorite(video: Video)
    suspend fun clearAll()
}

data class HistoryEntry(
    val uri: String,
    val positionMs: Long,
    val lastPlayedAtMillis: Long,
)

interface HistoryRepository {
    fun observeRecentUris(limit: Int = 50): Flow<List<String>>
    suspend fun savedPositionMs(uri: String): Long?
    suspend fun recordProgress(uri: String, positionMs: Long)
    suspend fun clearAll()
}

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setDefaultPlaybackSpeed(speed: Float)
    suspend fun setAutoLoop(enabled: Boolean)
    suspend fun setAutoAdvance(enabled: Boolean)
    suspend fun setResumePlayback(enabled: Boolean)
    suspend fun setGestureSpeedBoostEnabled(enabled: Boolean)
    suspend fun setDefaultMuted(enabled: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setShowFilename(enabled: Boolean)
    suspend fun setAutoShowControls(enabled: Boolean)
    suspend fun setAllowLandscapeRotation(enabled: Boolean)
    suspend fun setSortOrder(order: SortOrder)
    suspend fun setIncludedBucketIds(ids: Set<String>?)
}
