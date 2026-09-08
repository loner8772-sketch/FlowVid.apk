package com.flowvid.app.data.media

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.flowvid.app.domain.model.FolderCategory
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.model.VideoFolder
import com.flowvid.app.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext

/**
 * Reads the device's video library through [MediaStore]. This class never
 * touches raw filesystem paths and never copies a file: every [Video] it
 * returns simply wraps the [Uri] MediaStore already exposes for playback.
 */
class MediaStoreVideoRepository(
    private val appContext: Context,
) : VideoRepository {

    private val resolver: ContentResolver get() = appContext.contentResolver
    private val rescanTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    private val collection: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    private val baseProjection: Array<String> = buildList {
        add(MediaStore.Video.Media._ID)
        add(MediaStore.Video.Media.DISPLAY_NAME)
        add(MediaStore.Video.Media.DURATION)
        add(MediaStore.Video.Media.SIZE)
        add(MediaStore.Video.Media.WIDTH)
        add(MediaStore.Video.Media.HEIGHT)
        add(MediaStore.Video.Media.DATE_MODIFIED)
        add(MediaStore.Video.Media.DATE_ADDED)
        add(MediaStore.Video.Media.BUCKET_ID)
        add(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(MediaStore.Video.Media.RELATIVE_PATH)
        } else {
            @Suppress("DEPRECATION")
            add(MediaStore.Video.Media.DATA)
        }
    }.toTypedArray()

    override fun observeVideos(
        sortOrder: SortOrder,
        includedBucketIds: Set<String>?,
    ): Flow<List<Video>> = mediaStoreChanges()
        .map {
            if (includedBucketIds != null && includedBucketIds.isEmpty()) {
                emptyList()
            } else {
                queryVideos(sortOrder, includedBucketIds)
            }
        }
        .distinctUntilChanged()

    override suspend fun queryAllVideos(sortOrder: SortOrder): List<Video> =
        queryVideos(sortOrder, includedBucketIds = null)

    override suspend fun queryFolders(): List<VideoFolder> = withContext(Dispatchers.IO) {
        val counts = LinkedHashMap<String, Pair<String, Int>>()
        runCatching {
            resolver.query(collection, baseProjection, null, null, null)?.use { cursor ->
                val bucketIdCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = if (bucketIdCol >= 0) cursor.getString(bucketIdCol) else null
                    val name = if (bucketNameCol >= 0) cursor.getString(bucketNameCol) else null
                    if (id != null) {
                        val existing = counts[id]
                        counts[id] = (name ?: existing?.first ?: "Unknown") to ((existing?.second ?: 0) + 1)
                    }
                }
            }
        }
        counts.map { (id, nameAndCount) ->
            VideoFolder(
                bucketId = id,
                displayName = nameAndCount.first,
                category = FolderCategory.fromPath(nameAndCount.first, null),
                videoCount = nameAndCount.second,
            )
        }.sortedByDescending { it.videoCount }
    }

    override suspend fun isStillAccessible(video: Video): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            resolver.openAssetFileDescriptor(video.uri, "r")?.use { true } ?: false
        }.getOrDefault(false)
    }

    override suspend fun rescan() {
        rescanTrigger.emit(Unit)
    }

    /** External ContentObserver notifications, merged with manual "rescan" requests, into one trigger flow. */
    private fun mediaStoreChanges(): Flow<Unit> = merge(contentObserverUpdates(), rescanTrigger)

    private fun contentObserverUpdates(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(collection, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    private suspend fun queryVideos(
        sortOrder: SortOrder,
        includedBucketIds: Set<String>?,
    ): List<Video> = withContext(Dispatchers.IO) {
        val (selection, selectionArgs) = buildSelection(includedBucketIds)
        val sql = sqlSortOrder(sortOrder)

        val results = mutableListOf<Video>()
        runCatching {
            resolver.query(collection, baseProjection, selection, selectionArgs, sql)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationCol = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val widthCol = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val modifiedCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val addedCol = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val bucketIdCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID)
                val bucketNameCol = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val relativePathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else {
                    -1
                }

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    results += Video(
                        id = id,
                        uri = uri,
                        displayName = cursor.getString(nameCol) ?: uri.lastPathSegment.orEmpty(),
                        durationMs = if (durationCol >= 0) cursor.getLong(durationCol) else 0L,
                        sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L,
                        width = if (widthCol >= 0) cursor.getInt(widthCol) else 0,
                        height = if (heightCol >= 0) cursor.getInt(heightCol) else 0,
                        dateModifiedSeconds = if (modifiedCol >= 0) cursor.getLong(modifiedCol) else 0L,
                        dateAddedSeconds = if (addedCol >= 0) cursor.getLong(addedCol) else 0L,
                        bucketId = if (bucketIdCol >= 0) cursor.getString(bucketIdCol) else null,
                        bucketDisplayName = if (bucketNameCol >= 0) cursor.getString(bucketNameCol) else null,
                        relativePath = if (relativePathCol >= 0) cursor.getString(relativePathCol) else null,
                    )
                }
            }
        }

        if (sortOrder == SortOrder.RANDOM) results.shuffled() else results
    }

    private fun buildSelection(includedBucketIds: Set<String>?): Pair<String?, Array<String>?> {
        if (includedBucketIds.isNullOrEmpty()) return null to null
        val placeholders = includedBucketIds.joinToString(",") { "?" }
        val selection = "${MediaStore.Video.Media.BUCKET_ID} IN ($placeholders)"
        return selection to includedBucketIds.toTypedArray()
    }

    /** RANDOM is intentionally handled in-memory above; every other order maps to a real SQL clause. */
    private fun sqlSortOrder(sortOrder: SortOrder): String = when (sortOrder) {
        SortOrder.RECENT -> "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        SortOrder.OLDEST -> "${MediaStore.Video.Media.DATE_MODIFIED} ASC"
        SortOrder.NAME -> "${MediaStore.Video.Media.DISPLAY_NAME} COLLATE NOCASE ASC"
        SortOrder.DURATION -> "${MediaStore.Video.Media.DURATION} ASC"
        SortOrder.RANDOM -> "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
    }
}
