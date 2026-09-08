package com.flowvid.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flowvid.app.domain.model.FeedSource
import com.flowvid.app.ui.common.appContainer
import com.flowvid.app.ui.empty.EmptyStateScreen
import com.flowvid.app.ui.feed.FeedScreen
import com.flowvid.app.ui.onboarding.OnboardingScreen
import com.flowvid.app.util.PermissionUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenFolders: () -> Unit,
) {
    val context = LocalContext.current
    val container = appContainer()
    val scope = rememberCoroutineScope()
    var hasAccess by remember { mutableStateOf(PermissionUtils.hasAnyAccess(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccess = PermissionUtils.hasAnyAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    if (!hasAccess) {
        OnboardingScreen(onAccessGranted = { hasAccess = true })
        return
    }

    val hasVideosFlow = remember {
        container.settingsRepository.settings.flatMapLatest { settings ->
            container.videoRepository.observeVideos(settings.sortOrder, settings.includedBucketIds)
        }.map { it.isNotEmpty() }
    }
    val hasVideos by hasVideosFlow.collectAsStateWithLifecycle(initialValue = null)

    when (hasVideos) {
        null -> Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        false -> EmptyStateScreen(
            onScanAgain = { scope.launch { container.videoRepository.rescan() } },
            onChooseFolders = onOpenFolders,
        )
        true -> FeedScreen(
            source = FeedSource.Library,
            startVideoId = null,
            showBackButton = false,
            onBack = {},
            onOpenSearch = onOpenSearch,
            onOpenSettings = onOpenSettings,
            onOpenFavorites = onOpenFavorites,
            onOpenFolders = onOpenFolders,
        )
    }
}
