package com.flowvid.app.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.flowvid.app.R
import com.flowvid.app.ui.common.appContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderFilterScreen(onBack: () -> Unit) {
    val container = appContainer()
    val viewModel: FolderFilterViewModel = viewModel(
        factory = remember {
            viewModelFactory {
                initializer { FolderFilterViewModel(container.videoRepository, container.settingsRepository) }
            }
        },
    )
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val includedIds by viewModel.includedBucketIds.collectAsStateWithLifecycle()
    val isAllSelected = includedIds == null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.folders_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    TextButton(onClick = { if (isAllSelected) viewModel.selectNone() else viewModel.selectAll() }) {
                        Text(if (isAllSelected) "None" else "All")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = stringResource(R.string.folders_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(folders, key = { it.bucketId }) { folder ->
                    val checked = isAllSelected || includedIds?.contains(folder.bucketId) == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggle(folder.bucketId) }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { viewModel.toggle(folder.bucketId) })
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(text = folder.displayName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${folder.category.displayLabel} \u00b7 ${folder.videoCount} videos",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
