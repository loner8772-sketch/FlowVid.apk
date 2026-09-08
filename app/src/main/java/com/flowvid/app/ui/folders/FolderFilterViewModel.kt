package com.flowvid.app.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowvid.app.domain.model.VideoFolder
import com.flowvid.app.domain.repository.SettingsRepository
import com.flowvid.app.domain.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FolderFilterViewModel(
    private val videoRepository: VideoRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _folders = MutableStateFlow<List<VideoFolder>>(emptyList())
    val folders: StateFlow<List<VideoFolder>> = _folders.asStateFlow()

    /** null means "all accessible videos" (the default). */
    val includedBucketIds: StateFlow<Set<String>?> = settingsRepository.settings
        .map { it.includedBucketIds }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { _folders.value = videoRepository.queryFolders() }
    }

    fun toggle(bucketId: String) {
        viewModelScope.launch {
            val allIds = _folders.value.map { it.bucketId }.toSet()
            val current = includedBucketIds.value ?: allIds
            val updated = if (bucketId in current) current - bucketId else current + bucketId
            // If everything ends up included again, store null so newly-discovered
            // folders are included automatically rather than needing re-opt-in.
            settingsRepository.setIncludedBucketIds(if (updated.containsAll(allIds) && updated.size == allIds.size) null else updated)
        }
    }

    fun selectAll() {
        viewModelScope.launch { settingsRepository.setIncludedBucketIds(null) }
    }

    fun selectNone() {
        viewModelScope.launch { settingsRepository.setIncludedBucketIds(emptySet()) }
    }
}
