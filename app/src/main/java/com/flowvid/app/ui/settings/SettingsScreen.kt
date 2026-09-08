package com.flowvid.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.flowvid.app.BuildConfig
import com.flowvid.app.R
import com.flowvid.app.domain.model.ThemeMode
import com.flowvid.app.ui.common.appContainer
import com.flowvid.app.ui.feed.components.SpeedSelectorSheet
import com.flowvid.app.domain.model.asSpeedLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenFolders: () -> Unit,
) {
    val container = appContainer()
    val viewModel: SettingsViewModel = viewModel(
        factory = remember {
            viewModelFactory {
                initializer {
                    SettingsViewModel(
                        container.settingsRepository,
                        container.videoRepository,
                        container.favoritesRepository,
                        container.historyRepository,
                    )
                }
            }
        },
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearFavoritesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader(stringResource(R.string.settings_section_playback))
            SettingsClickableRow(
                title = stringResource(R.string.settings_default_speed),
                subtitle = settings.defaultPlaybackSpeed.asSpeedLabel(),
                onClick = { showSpeedSheet = true },
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_auto_loop),
                subtitle = stringResource(R.string.settings_auto_loop_sub),
                checked = settings.autoLoop,
                onCheckedChange = viewModel::setAutoLoop,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_auto_advance),
                subtitle = stringResource(R.string.settings_auto_advance_sub),
                checked = settings.autoAdvance,
                onCheckedChange = viewModel::setAutoAdvance,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_resume),
                subtitle = stringResource(R.string.settings_resume_sub),
                checked = settings.resumePlayback,
                onCheckedChange = viewModel::setResume,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_gesture_boost),
                subtitle = stringResource(R.string.settings_gesture_boost_sub),
                checked = settings.gestureSpeedBoostEnabled,
                onCheckedChange = viewModel::setGestureBoost,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_default_mute),
                checked = settings.defaultMuted,
                onCheckedChange = viewModel::setDefaultMuted,
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
            SettingsClickableRow(
                title = stringResource(R.string.settings_theme),
                subtitle = themeLabel(settings.themeMode),
                onClick = { showThemeDialog = true },
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_show_filename),
                checked = settings.showFilename,
                onCheckedChange = viewModel::setShowFilename,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_auto_show_controls),
                checked = settings.autoShowControls,
                onCheckedChange = viewModel::setAutoShowControls,
            )
            SettingsSwitchRow(
                title = stringResource(R.string.settings_allow_landscape),
                subtitle = stringResource(R.string.settings_allow_landscape_sub),
                checked = settings.allowLandscapeRotation,
                onCheckedChange = viewModel::setAllowLandscape,
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            SettingsSectionHeader(stringResource(R.string.settings_section_library))
            SettingsClickableRow(
                title = stringResource(R.string.settings_included_folders),
                onClick = onOpenFolders,
            )
            SettingsClickableRow(
                title = stringResource(R.string.settings_sort_order),
                subtitle = sortLabel(settings.sortOrder),
                onClick = { showSortDialog = true },
            )
            SettingsClickableRow(
                title = stringResource(R.string.settings_rescan),
                onClick = viewModel::rescanLibrary,
            )
            SettingsClickableRow(
                title = stringResource(R.string.settings_clear_history),
                onClick = { showClearHistoryDialog = true },
            )
            SettingsClickableRow(
                title = stringResource(R.string.settings_clear_favorites),
                onClick = { showClearFavoritesDialog = true },
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            SettingsSectionHeader(stringResource(R.string.settings_section_about))
            SettingsClickableRow(
                title = stringResource(R.string.app_name),
                subtitle = "${stringResource(R.string.settings_version)} ${BuildConfig.VERSION_NAME}",
                onClick = {},
            )
            Text(
                text = stringResource(R.string.settings_privacy_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }

    if (showSpeedSheet) {
        SpeedSelectorSheet(
            currentSpeed = settings.defaultPlaybackSpeed,
            onSelect = { speed -> viewModel.setDefaultSpeed(speed); showSpeedSheet = false },
            onDismiss = { showSpeedSheet = false },
        )
    }

    if (showSortDialog) {
        SortPickerDialog(
            current = settings.sortOrder,
            onSelect = { viewModel.setSortOrder(it); showSortDialog = false },
            onDismiss = { showSortDialog = false },
        )
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            current = settings.themeMode,
            onSelect = { viewModel.setTheme(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.confirm_clear_history_title)) },
            text = { Text(stringResource(R.string.confirm_clear_history_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearHistory(); showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }

    if (showClearFavoritesDialog) {
        AlertDialog(
            onDismissRequest = { showClearFavoritesDialog = false },
            title = { Text(stringResource(R.string.confirm_clear_favorites_title)) },
            text = { Text(stringResource(R.string.confirm_clear_favorites_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFavorites(); showClearFavoritesDialog = false }) {
                    Text(stringResource(R.string.action_clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearFavoritesDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun sortLabel(order: com.flowvid.app.domain.model.SortOrder): String = when (order) {
    com.flowvid.app.domain.model.SortOrder.RECENT -> stringResource(R.string.sort_recent)
    com.flowvid.app.domain.model.SortOrder.OLDEST -> stringResource(R.string.sort_oldest)
    com.flowvid.app.domain.model.SortOrder.NAME -> stringResource(R.string.sort_name)
    com.flowvid.app.domain.model.SortOrder.DURATION -> stringResource(R.string.sort_duration)
    com.flowvid.app.domain.model.SortOrder.RANDOM -> stringResource(R.string.sort_random)
}
