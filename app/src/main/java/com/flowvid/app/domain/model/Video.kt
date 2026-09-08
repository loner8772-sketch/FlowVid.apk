package com.flowvid.app.domain.model

import android.net.Uri

/**
 * A single locally-stored video, as discovered through [android.provider.MediaStore].
 * FlowVid never copies the underlying file: [uri] always points back at the
 * video's real location in shared storage.
 */
data class Video(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val dateModifiedSeconds: Long,
    val dateAddedSeconds: Long,
    val bucketId: String?,
    val bucketDisplayName: String?,
    val relativePath: String?,
) {
    /** A stable string key, safe to use as a Compose pager/list key or a Room foreign reference. */
    val stableKey: String get() = uri.toString()

    val isLandscape: Boolean get() = width > 0 && height > 0 && width > height
}

/** One entry in the folder-selection screen: a MediaStore bucket plus a friendly category guess. */
data class VideoFolder(
    val bucketId: String,
    val displayName: String,
    val category: FolderCategory,
    val videoCount: Int,
)

/** Coarse, best-effort categorization of a folder, purely to make the filter UI legible. */
enum class FolderCategory(val displayLabel: String) {
    CAMERA("Camera"),
    SCREEN_RECORDINGS("Screen recordings"),
    DOWNLOADS("Downloads"),
    MOVIES("Movies"),
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram"),
    OTHER("Other folders");

    companion object {
        fun fromPath(bucketDisplayName: String?, relativePath: String?): FolderCategory {
            val name = (bucketDisplayName ?: "").lowercase()
            val path = (relativePath ?: "").lowercase()
            val haystack = "$name $path"
            return when {
                "whatsapp" in haystack -> WHATSAPP
                "telegram" in haystack -> TELEGRAM
                "screenshot" in haystack || "screen recording" in haystack || "screenrecord" in haystack -> SCREEN_RECORDINGS
                "download" in haystack -> DOWNLOADS
                "movies" in haystack -> MOVIES
                "camera" in haystack || "dcim" in haystack -> CAMERA
                else -> OTHER
            }
        }
    }
}

enum class SortOrder {
    RECENT,
    OLDEST,
    NAME,
    DURATION,
    RANDOM,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/** Which collection a Feed screen instance is paging through. */
sealed class FeedSource {
    data object Library : FeedSource()
    data object Favorites : FeedSource()
}

/** The full set of user-adjustable preferences, persisted via DataStore. */
data class AppSettings(
    val defaultPlaybackSpeed: Float = 1f,
    val autoLoop: Boolean = true,
    val autoAdvance: Boolean = false,
    val resumePlayback: Boolean = true,
    val gestureSpeedBoostEnabled: Boolean = true,
    val defaultMuted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showFilename: Boolean = true,
    val autoShowControls: Boolean = true,
    val allowLandscapeRotation: Boolean = false,
    val sortOrder: SortOrder = SortOrder.RECENT,
    val includedBucketIds: Set<String>? = null, // null == "all accessible videos"
)

/** The supported speed steps, in the exact order the speed selector cycles through. */
val PLAYBACK_SPEEDS: List<Float> = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f)

fun Float.asSpeedLabel(): String {
    val rounded = (this * 100).toInt()
    return if (rounded % 100 == 0) "${rounded / 100}\u00d7" else "${rounded / 100f}\u00d7"
}
