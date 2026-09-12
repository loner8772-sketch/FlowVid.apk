@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowvid.app.ui.feed.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flowvid.app.R
import com.flowvid.app.domain.model.Video
import com.flowvid.app.util.MediaFormatters

@Composable
fun VideoInfoSheet(video: Video, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(text = video.displayName, style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 12.dp))
            InfoRow(stringResource(R.string.info_duration), MediaFormatters.duration(video.durationMs))
            InfoRow(stringResource(R.string.info_resolution), MediaFormatters.resolution(video.width, video.height))
            InfoRow(stringResource(R.string.info_size), MediaFormatters.fileSize(video.sizeBytes))
            InfoRow(stringResource(R.string.info_folder), video.bucketDisplayName ?: video.relativePath ?: "Unknown")
            InfoRow(stringResource(R.string.info_modified), MediaFormatters.dateFromEpochSeconds(video.dateModifiedSeconds))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
