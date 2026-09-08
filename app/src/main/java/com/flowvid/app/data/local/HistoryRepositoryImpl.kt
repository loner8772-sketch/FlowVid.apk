package com.flowvid.app.data.local

import com.flowvid.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class HistoryRepositoryImpl(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observeRecentUris(limit: Int): Flow<List<String>> = dao.observeRecentUris(limit)

    override suspend fun savedPositionMs(uri: String): Long? = dao.find(uri)?.positionMs

    override suspend fun recordProgress(uri: String, positionMs: Long) {
        dao.upsert(
            HistoryEntity(
                uri = uri,
                positionMs = positionMs,
                lastPlayedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun clearAll() = dao.clearAll()
}
