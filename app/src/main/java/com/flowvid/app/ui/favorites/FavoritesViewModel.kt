package com.flowvid.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.repository.FavoritesRepository
import com.flowvid.app.domain.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _allVideos = MutableStateFlow<List<Video>>(emptyList())

    val favoriteVideos: StateFlow<List<Video>> = combine(
        favoritesRepository.observeFavoriteUris(),
        _allVideos,
    ) { favoriteUris, videos ->
        val byUri = videos.associateBy { it.stableKey }
        favoriteUris.mapNotNull { byUri[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _allVideos.value = videoRepository.queryAllVideos(SortOrder.RECENT)
        }
    }

    fun clearAll() {
        viewModelScope.launch { favoritesRepository.clearAll() }
    }
}
