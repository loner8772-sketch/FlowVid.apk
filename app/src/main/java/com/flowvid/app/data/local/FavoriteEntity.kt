package com.flowvid.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

/**
 * A favorited video, identified by its MediaStore content Uri (stored as a
 * string). Only a reference and light metadata are kept here; the actual
 * video file is never copied or moved.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val uri: String,
    val displayName: String,
    val addedAtMillis: Long,
)

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT uri FROM favorites")
    fun observeAllUris(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE uri = :uri)")
    suspend fun isFavorite(uri: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
