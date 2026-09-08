package com.flowvid.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.flowvid.app.domain.model.AppSettings
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.ThemeMode
import com.flowvid.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            defaultPlaybackSpeed = prefs[SettingsKeys.DEFAULT_SPEED] ?: 1f,
            autoLoop = prefs[SettingsKeys.AUTO_LOOP] ?: true,
            autoAdvance = prefs[SettingsKeys.AUTO_ADVANCE] ?: false,
            resumePlayback = prefs[SettingsKeys.RESUME_PLAYBACK] ?: true,
            gestureSpeedBoostEnabled = prefs[SettingsKeys.GESTURE_SPEED_BOOST] ?: true,
            defaultMuted = prefs[SettingsKeys.DEFAULT_MUTED] ?: false,
            themeMode = prefs[SettingsKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            showFilename = prefs[SettingsKeys.SHOW_FILENAME] ?: true,
            autoShowControls = prefs[SettingsKeys.AUTO_SHOW_CONTROLS] ?: true,
            allowLandscapeRotation = prefs[SettingsKeys.ALLOW_LANDSCAPE_ROTATION] ?: false,
            sortOrder = prefs[SettingsKeys.SORT_ORDER]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
                ?: SortOrder.RECENT,
            includedBucketIds = prefs[SettingsKeys.INCLUDED_BUCKET_IDS],
        )
    }

    override suspend fun setDefaultPlaybackSpeed(speed: Float) = update { it[SettingsKeys.DEFAULT_SPEED] = speed }
    override suspend fun setAutoLoop(enabled: Boolean) = update { it[SettingsKeys.AUTO_LOOP] = enabled }
    override suspend fun setAutoAdvance(enabled: Boolean) = update { it[SettingsKeys.AUTO_ADVANCE] = enabled }
    override suspend fun setResumePlayback(enabled: Boolean) = update { it[SettingsKeys.RESUME_PLAYBACK] = enabled }
    override suspend fun setGestureSpeedBoostEnabled(enabled: Boolean) =
        update { it[SettingsKeys.GESTURE_SPEED_BOOST] = enabled }
    override suspend fun setDefaultMuted(enabled: Boolean) = update { it[SettingsKeys.DEFAULT_MUTED] = enabled }
    override suspend fun setThemeMode(mode: ThemeMode) = update { it[SettingsKeys.THEME_MODE] = mode.name }
    override suspend fun setShowFilename(enabled: Boolean) = update { it[SettingsKeys.SHOW_FILENAME] = enabled }
    override suspend fun setAutoShowControls(enabled: Boolean) =
        update { it[SettingsKeys.AUTO_SHOW_CONTROLS] = enabled }
    override suspend fun setAllowLandscapeRotation(enabled: Boolean) =
        update { it[SettingsKeys.ALLOW_LANDSCAPE_ROTATION] = enabled }
    override suspend fun setSortOrder(order: SortOrder) = update { it[SettingsKeys.SORT_ORDER] = order.name }

    override suspend fun setIncludedBucketIds(ids: Set<String>?) = update { prefs ->
        if (ids == null) {
            prefs.remove(SettingsKeys.INCLUDED_BUCKET_IDS)
        } else {
            prefs[SettingsKeys.INCLUDED_BUCKET_IDS] = ids
        }
    }

    private suspend fun update(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }
}
