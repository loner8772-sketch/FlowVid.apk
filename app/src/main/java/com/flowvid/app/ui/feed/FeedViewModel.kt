package com.flowvid.app.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import com.flowvid.app.domain.model.AppSettings
import com.flowvid.app.domain.model.FeedSource
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.repository.FavoritesRepository
import com.flowvid.app.domain.repository.HistoryRepository
import com.flowvid.app.domain.repository.SettingsRepository
import com.flowvid.app.domain.repository.VideoRepository
import com.flowvid.app.player.FlowVidPlayerPool
import com.flowvid.app.player.resolveRepeatMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackErrorInfo(val message: String, val errorCodeName: String)

sealed class FeedEvent {
    data class AdvanceToNext(val fromIndex: Int) : FeedEvent()
}

data class FeedUiState(
    val videos: List<Video> = emptyList(),
    val hasLoadedOnce: Boolean = false,
    val initialPageIndex: Int = 0,
    val activeIndex: Int = 0,
    val isMuted: Boolean = false,
    val speed: Float = 1f,
    val boostActive: Boolean = false,
    val favoriteUris: Set<String> = emptySet(),
    val settings: AppSettings = AppSettings(),
    val errors: Map<String, PlaybackErrorInfo> = emptyMap(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    application: Application,
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val source: FeedSource,
    private val startVideoId: String?,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<FeedEvent> = _events

    private var capturedInitialIndex: Int? = null
    private var speedInitialized = false
    private var mutedInitialized = false

    val playerPool = FlowVidPlayerPool(
        context = application,
        poolSize = 3,
        onPlayerError = ::handlePlayerError,
        onPlaybackEnded = ::handlePlaybackEnded,
    )

    private val videosFlow = when (source) {
        FeedSource.Library -> settingsRepository.settings.flatMapLatest { settings ->
            videoRepository.observeVideos(settings.sortOrder, settings.includedBucketIds)
        }
        FeedSource.Favorites -> combine(
            favoritesRepository.observeFavoriteUris(),
            videoRepository.observeVideos(SortOrder.RECENT, null),
        ) { favoriteUris, allVideos ->
            val byUri = allVideos.associateBy { it.stableKey }
            favoriteUris.mapNotNull { byUri[it] }
        }
    }

    init {
        combine(
            videosFlow,
            favoritesRepository.observeFavoriteUris(),
            settingsRepository.settings,
        ) { videos, favoriteUris, settings ->
            Triple(videos, favoriteUris.toSet(), settings)
        }.onEach { (videos, favoriteUris, settings) ->
            if (!speedInitialized) {
                speedInitialized = true
                _uiState.update { it.copy(speed = settings.defaultPlaybackSpeed) }
            }
            if (!mutedInitialized) {
                mutedInitialized = true
                _uiState.update { it.copy(isMuted = settings.defaultMuted) }
            }
            if (capturedInitialIndex == null && (startVideoId == null || videos.isNotEmpty())) {
                capturedInitialIndex = startVideoId
                    ?.let { id -> videos.indexOfFirst { it.stableKey == id } }
                    ?.takeIf { it >= 0 } ?: 0
            }
            _uiState.update {
                it.copy(
                    videos = videos,
                    hasLoadedOnce = true,
                    initialPageIndex = capturedInitialIndex ?: 0,
                    favoriteUris = favoriteUris,
                    settings = settings,
                    activeIndex = it.activeIndex.coerceIn(0, (videos.size - 1).coerceAtLeast(0)),
                )
            }
        }.launchIn(viewModelScope)
    }

    /** Called by the Feed screen whenever the pager settles on a new page. */
    fun onPageSettled(newIndex: Int) {
        val state = _uiState.value
        val videos = state.videos
        if (videos.isEmpty() || newIndex !in videos.indices) return

        val previousIndex = state.activeIndex
        if (previousIndex in videos.indices && previousIndex != newIndex) {
            val previousVideo = videos[previousIndex]
            playerPool.peek(previousVideo.stableKey)?.let { previousPlayer ->
                val position = previousPlayer.currentPosition
                viewModelScope.launch { historyRepository.recordProgress(previousVideo.stableKey, position) }
            }
        }

        _uiState.update { it.copy(activeIndex = newIndex, boostActive = false) }

        val windowIndices = (newIndex - 1..newIndex + 1).filter { it in videos.indices }
        val windowKeys = windowIndices.map { videos[it].stableKey }.toSet()
        playerPool.trimTo(windowKeys)
        windowIndices.forEach { i -> playerPool.playerFor(videos[i].stableKey, videos[i].uri) }

        val activeVideo = videos[newIndex]
        val activePlayer = playerPool.playerFor(activeVideo.stableKey, activeVideo.uri)
        playerPool.ensureOnlyActive(activeVideo.stableKey)

        val current = _uiState.value
        activePlayer.repeatMode = resolveRepeatMode(current.settings.autoLoop, current.settings.autoAdvance)
        activePlayer.setPlaybackSpeed(if (current.boostActive) 2f else current.speed)
        activePlayer.volume = if (current.isMuted) 0f else 1f

        if (current.settings.resumePlayback) {
            viewModelScope.launch {
                val saved = historyRepository.savedPositionMs(activeVideo.stableKey)
                val duration = activeVideo.durationMs
                if (saved != null && saved > 3_000L && (duration <= 0 || saved < duration - 3_000L)) {
                    activePlayer.seekTo(saved)
                }
            }
        }
        activePlayer.playWhenReady = true
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = newMuted) }
        activePlayerOrNull()?.volume = if (newMuted) 0f else 1f
    }

    fun setSpeed(speed: Float) {
        _uiState.update { it.copy(speed = speed) }
        viewModelScope.launch { settingsRepository.setDefaultPlaybackSpeed(speed) }
        if (!_uiState.value.boostActive) {
            activePlayerOrNull()?.setPlaybackSpeed(speed)
        }
    }

    fun setBoost(active: Boolean) {
        if (!_uiState.value.settings.gestureSpeedBoostEnabled) return
        _uiState.update { it.copy(boostActive = active) }
        val player = activePlayerOrNull() ?: return
        player.setPlaybackSpeed(if (active) 2f else _uiState.value.speed)
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(video) }
    }

    fun retry(video: Video) {
        _uiState.update { it.copy(errors = it.errors - video.stableKey) }
        val player = playerPool.peek(video.stableKey) ?: playerPool.playerFor(video.stableKey, video.uri)
        player.prepare()
    }

    fun savePositionForActiveVideo() {
        val state = _uiState.value
        val video = state.videos.getOrNull(state.activeIndex) ?: return
        val player = playerPool.peek(video.stableKey) ?: return
        viewModelScope.launch { historyRepository.recordProgress(video.stableKey, player.currentPosition) }
    }

    fun pauseAllPlayback() = playerPool.pauseAll()

    fun resumeActivePlayback() {
        val state = _uiState.value
        val video = state.videos.getOrNull(state.activeIndex) ?: return
        playerPool.peek(video.stableKey)?.playWhenReady = true
    }

    private fun activePlayerOrNull() = _uiState.value.let { state ->
        state.videos.getOrNull(state.activeIndex)?.let { playerPool.peek(it.stableKey) }
    }

    private fun handlePlayerError(key: String, error: PlaybackException) {
        _uiState.update {
            it.copy(
                errors = it.errors + (
                    key to PlaybackErrorInfo(
                        message = error.message ?: "Unknown error",
                        errorCodeName = PlaybackException.getErrorCodeName(error.errorCode),
                    )
                ),
            )
        }
        viewModelScope.launch {
            val video = _uiState.value.videos.find { it.stableKey == key } ?: return@launch
            videoRepository.isStillAccessible(video) // A follow-up MediaStore refresh will drop it if false.
        }
    }

    private fun handlePlaybackEnded(key: String) {
        val state = _uiState.value
        if (!state.settings.autoAdvance) return
        val index = state.videos.indexOfFirst { it.stableKey == key }
        if (index >= 0) {
            viewModelScope.launch { _events.emit(FeedEvent.AdvanceToNext(index)) }
        }
    }

    override fun onCleared() {
        savePositionForActiveVideo()
        playerPool.releaseAll()
        super.onCleared()
    }
}
