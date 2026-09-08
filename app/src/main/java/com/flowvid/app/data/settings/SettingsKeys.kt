package com.flowvid.app.data.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

internal object SettingsKeys {
    val DEFAULT_SPEED = floatPreferencesKey("default_playback_speed")
    val AUTO_LOOP = booleanPreferencesKey("auto_loop")
    val AUTO_ADVANCE = booleanPreferencesKey("auto_advance")
    val RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
    val GESTURE_SPEED_BOOST = booleanPreferencesKey("gesture_speed_boost")
    val DEFAULT_MUTED = booleanPreferencesKey("default_muted")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SHOW_FILENAME = booleanPreferencesKey("show_filename")
    val AUTO_SHOW_CONTROLS = booleanPreferencesKey("auto_show_controls")
    val ALLOW_LANDSCAPE_ROTATION = booleanPreferencesKey("allow_landscape_rotation")
    val SORT_ORDER = stringPreferencesKey("sort_order")
    val INCLUDED_BUCKET_IDS = stringSetPreferencesKey("included_bucket_ids")
}
