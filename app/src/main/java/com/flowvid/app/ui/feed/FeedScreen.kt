@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowvid.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.flowvid.app.R
import com.flowvid.app.domain.model.FeedSource
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.ui.common.appContainer
import com.flowvid.app.ui.feed.components.FeedBottomBar
import com.flowvid.app.ui.feed.components.FeedTopBar
import com.flowvid.app.ui.feed.components.PlaybackErrorOverlay
import com.flowvid.app.ui.feed.components.SpeedSelectorSheet
import com.flowvid.app.ui.feed.components.VideoInfoSheet
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    source: FeedSource,
    startVideoId: String?,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenFolders: () -> Unit,
) {
    val container = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val viewModel: FeedViewModel = viewModel(
        factory = remember(source, startVideoId) {
            viewModelFactory {
                initializer {
                    FeedViewModel(
                        application = context.applicationContext as android.app.Application,
                        videoRepository = container.videoRepository,
                        favoritesRepository = container.favoritesRepository,
                        historyRepository = container.historyRepository,
                        settingsRepository = container.settingsRepository,
                        source = source,
                        startVideoId = startVideoId,
                    )
                }
            }
        },
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.savePositionForActiveVideo()
                    viewModel.pauseAllPlayback()
                }
                Lifecycle.Event.ON_RESUME -> viewModel.resumeActivePlayback()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    if (!uiState.hasLoadedOnce) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    if (uiState.videos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(
                text = if (source == FeedSource.Favorites) {
                    stringResource(R.string.favorites_empty_body)
                } else {
                    stringResource(R.string.empty_body)
                },
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(32.dp),
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = uiState.initialPageIndex,
        pageCount = { uiState.videos.size },
    )
    var hasJumpedToInitialIndex by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.initialPageIndex, uiState.hasLoadedOnce) {
        if (!hasJumpedToInitialIndex && uiState.hasLoadedOnce) {
            hasJumpedToInitialIndex = true
            if (uiState.initialPageIndex != pagerState.currentPage) {
                pagerState.scrollToPage(uiState.initialPageIndex)
            }
            viewModel.onPageSettled(pagerState.currentPage)
        }
    }

    LaunchedEffect(pagerState) {
        androidx.compose.runtime.snapshotFlow { pagerState.settledPage }
            .collect { settled -> viewModel.onPageSettled(settled) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedEvent.AdvanceToNext -> {
                    val next = event.fromIndex + 1
                    if (next < uiState.videos.size) {
                        pagerState.animateScrollToPage(next)
                    }
                }
            }
        }
    }

    var showSpeedSheet by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showMenuSheet by remember { mutableStateOf(false) }
    var progressFraction by remember { mutableStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { index -> uiState.videos.getOrNull(index)?.stableKey ?: index },
        ) { page ->
            val pageVideo = uiState.videos.getOrNull(page) ?: return@VerticalPager
            val isActivePage = page == pagerState.settledPage
            val player = remember(pageVideo.stableKey) {
                viewModel.playerPool.playerFor(pageVideo.stableKey, pageVideo.uri)
            }

            VideoPageContent(
                video = pageVideo,
                player = player,
                isActivePage = isActivePage,
                zoomToFill = false,
                gestureSpeedBoostEnabled = uiState.settings.gestureSpeedBoostEnabled,
                onTogglePlayPause = { player.playWhenReady = !player.playWhenReady },
                onSpeedBoostChanged = { active -> if (isActivePage) viewModel.setBoost(active) },
                onProgressChanged = { fraction, _, _ -> if (isActivePage) progressFraction = fraction },
            )
        }

        val activeVideo = uiState.videos.getOrNull(uiState.activeIndex)
        val activeError = activeVideo?.let { uiState.errors[it.stableKey] }

        FeedTopBar(
            showBackButton = showBackButton,
            onBack = onBack,
            onMenu = { showMenuSheet = true },
            onSearch = onOpenSearch,
            onSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        if (activeVideo != null) {
            FeedBottomBar(
                filename = activeVideo.displayName,
                showFilename = uiState.settings.showFilename,
                progressFraction = progressFraction,
                onScrubStart = { viewModel.playerPool.peek(activeVideo.stableKey)?.playWhenReady = false },
                onScrub = { fraction -> progressFraction = fraction },
                onScrubFinished = { fraction ->
                    val player = viewModel.playerPool.peek(activeVideo.stableKey)
                    val duration = player?.duration?.coerceAtLeast(0) ?: 0
                    if (player != null && duration > 0) {
                        player.seekTo((fraction * duration).toLong())
                    }
                    player?.playWhenReady = true
                },
                isMuted = uiState.isMuted,
                onToggleMute = viewModel::toggleMute,
                speed = uiState.speed,
                onOpenSpeedSelector = { showSpeedSheet = true },
                isFavorite = uiState.favoriteUris.contains(activeVideo.stableKey),
                onToggleFavorite = { viewModel.toggleFavorite(activeVideo) },
                onShare = { shareVideo(context, activeVideo.uri) },
                onOpenInfo = { showInfoSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (activeError != null && activeVideo != null) {
            com.flowvid.app.ui.theme.FlowVidFeedTheme {
                PlaybackErrorOverlay(
                    error = activeError,
                    onSkip = {
                        scope.launch {
                            val next = (uiState.activeIndex + 1).coerceAtMost(uiState.videos.lastIndex)
                            if (next != uiState.activeIndex) pagerState.animateScrollToPage(next)
                        }
                    },
                    onRetry = { viewModel.retry(activeVideo) },
                )
            }
        }
    }

    if (showSpeedSheet) {
        com.flowvid.app.ui.theme.FlowVidFeedTheme {
            SpeedSelectorSheet(
                currentSpeed = uiState.speed,
                onSelect = { speed ->
                    viewModel.setSpeed(speed)
                    showSpeedSheet = false
                },
                onDismiss = { showSpeedSheet = false },
            )
        }
    }

    if (showInfoSheet) {
        uiState.videos.getOrNull(uiState.activeIndex)?.let { video ->
            com.flowvid.app.ui.theme.FlowVidFeedTheme {
                VideoInfoSheet(video = video, onDismiss = { showInfoSheet = false })
            }
        }
    }

    if (showMenuSheet) {
        com.flowvid.app.ui.theme.FlowVidFeedTheme {
            FeedQuickMenuSheet(
                currentSort = uiState.settings.sortOrder,
                onSortSelected = { order -> scope.launch { container.settingsRepository.setSortOrder(order) } },
                onOpenFavorites = { showMenuSheet = false; onOpenFavorites() },
                onOpenFolders = { showMenuSheet = false; onOpenFolders() },
                onDismiss = { showMenuSheet = false },
            )
        }
    }
}

@Composable
private fun FeedQuickMenuSheet(
    currentSort: SortOrder,
    onSortSelected: (SortOrder) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenFolders: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFavorites)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null)
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFolders)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Folder, contentDescription = null)
                Text(
                    text = stringResource(R.string.settings_included_folders),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Text(
                text = stringResource(R.string.sort_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            sortOptions().forEach { (order, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortSelected(order) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    if (order == currentSort) Icon(Icons.Filled.Check, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun sortOptions(): List<Pair<SortOrder, String>> = listOf(
    SortOrder.RECENT to stringResource(R.string.sort_recent),
    SortOrder.OLDEST to stringResource(R.string.sort_oldest),
    SortOrder.NAME to stringResource(R.string.sort_name),
    SortOrder.DURATION to stringResource(R.string.sort_duration),
    SortOrder.RANDOM to stringResource(R.string.sort_random),
)

private fun shareVideo(context: android.content.Context, uri: android.net.Uri) {
    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "video/*"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
}
