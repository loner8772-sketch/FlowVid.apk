package com.flowvid.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val uri: String,
    val positionMs: Long,
    val lastPlayedAtMillis: Long,
)

@Dao
interface HistoryDao {
    @Query("SELECT uri FROM history ORDER BY lastPlayedAtMillis DESC LIMIT :limit")
    fun observeRecentUris(limit: Int): Flow<List<String>>

    @Query("SELECT * FROM history WHERE uri = :uri LIMIT 1")
    suspend fun find(uri: String): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}
