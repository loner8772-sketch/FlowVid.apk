package com.flowvid.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowvid.app.domain.model.AppSettings
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.ThemeMode
import com.flowvid.app.domain.repository.FavoritesRepository
import com.flowvid.app.domain.repository.HistoryRepository
import com.flowvid.app.domain.repository.SettingsRepository
import com.flowvid.app.domain.repository.VideoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setDefaultSpeed(speed: Float) = launch { settingsRepository.setDefaultPlaybackSpeed(speed) }
    fun setAutoLoop(enabled: Boolean) = launch { settingsRepository.setAutoLoop(enabled) }
    fun setAutoAdvance(enabled: Boolean) = launch { settingsRepository.setAutoAdvance(enabled) }
    fun setResume(enabled: Boolean) = launch { settingsRepository.setResumePlayback(enabled) }
    fun setGestureBoost(enabled: Boolean) = launch { settingsRepository.setGestureSpeedBoostEnabled(enabled) }
    fun setDefaultMuted(enabled: Boolean) = launch { settingsRepository.setDefaultMuted(enabled) }
    fun setTheme(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }
    fun setShowFilename(enabled: Boolean) = launch { settingsRepository.setShowFilename(enabled) }
    fun setAutoShowControls(enabled: Boolean) = launch { settingsRepository.setAutoShowControls(enabled) }
    fun setAllowLandscape(enabled: Boolean) = launch { settingsRepository.setAllowLandscapeRotation(enabled) }
    fun setSortOrder(order: SortOrder) = launch { settingsRepository.setSortOrder(order) }

    fun rescanLibrary() = launch { videoRepository.rescan() }
    fun clearHistory() = launch { historyRepository.clearAll() }
    fun clearFavorites() = launch { favoritesRepository.clearAll() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
