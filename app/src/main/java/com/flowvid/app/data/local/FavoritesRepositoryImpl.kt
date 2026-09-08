package com.flowvid.app.data.local

import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteDao,
) : FavoritesRepository {

    override fun observeFavoriteUris(): Flow<List<String>> =
        dao.observeAll().map { list -> list.map { it.uri } }

    override suspend fun isFavorite(uri: String): Boolean = dao.isFavorite(uri)

    override suspend fun toggleFavorite(video: Video) {
        val uri = video.stableKey
        if (dao.isFavorite(uri)) {
            dao.deleteByUri(uri)
        } else {
            dao.insert(
                FavoriteEntity(
                    uri = uri,
                    displayName = video.displayName,
                    addedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    override suspend fun clearAll() = dao.clearAll()
}
