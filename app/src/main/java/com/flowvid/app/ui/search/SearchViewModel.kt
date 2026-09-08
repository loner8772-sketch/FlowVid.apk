package com.flowvid.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.Video
import com.flowvid.app.domain.repository.HistoryRepository
import com.flowvid.app.domain.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val videoRepository: VideoRepository,
    historyRepository: HistoryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _allVideos = MutableStateFlow<List<Video>>(emptyList())

    /** All accessible videos, regardless of the feed's folder filter — search is deliberately unscoped. */
    val results: StateFlow<List<Video>> = combine(_query, _allVideos) { queryText, videos ->
        if (queryText.isBlank()) {
            emptyList()
        } else {
            videos.filter { it.displayName.contains(queryText, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentVideos: StateFlow<List<Video>> = combine(
        historyRepository.observeRecentUris(limit = 12),
        _allVideos,
    ) { recentUris, videos ->
        val byUri = videos.associateBy { it.stableKey }
        recentUris.mapNotNull { byUri[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _allVideos.value = videoRepository.queryAllVideos(SortOrder.NAME)
        }
    }

    fun setQuery(text: String) {
        _query.value = text
    }
}
