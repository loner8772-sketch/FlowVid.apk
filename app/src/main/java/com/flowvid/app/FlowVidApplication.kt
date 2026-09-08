package com.flowvid.app

import android.app.Application
import com.flowvid.app.data.local.AppDatabase
import com.flowvid.app.data.local.FavoritesRepositoryImpl
import com.flowvid.app.data.local.HistoryRepositoryImpl
import com.flowvid.app.data.media.MediaStoreVideoRepository
import com.flowvid.app.data.settings.SettingsRepositoryImpl
import com.flowvid.app.data.settings.settingsDataStore
import com.flowvid.app.domain.repository.FavoritesRepository
import com.flowvid.app.domain.repository.HistoryRepository
import com.flowvid.app.domain.repository.SettingsRepository
import com.flowvid.app.domain.repository.VideoRepository

/**
 * FlowVid intentionally uses a small hand-written container instead of a DI
 * framework: every dependency here is a plain, cheap-to-construct singleton,
 * and avoiding an annotation-processing framework keeps the whole project
 * buildable without any moving pieces beyond Gradle + KSP for Room.
 */
class AppContainer(app: Application) {
    private val database by lazy { AppDatabase.getInstance(app) }

    val videoRepository: VideoRepository by lazy { MediaStoreVideoRepository(app) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepositoryImpl(database.favoriteDao()) }
    val historyRepository: HistoryRepository by lazy { HistoryRepositoryImpl(database.historyDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(app.settingsDataStore) }
}

class FlowVidApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
