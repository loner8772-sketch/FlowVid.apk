@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowvid.app.ui.feed.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flowvid.app.domain.model.PLAYBACK_SPEEDS
import com.flowvid.app.domain.model.asSpeedLabel

@Composable
fun SpeedSelectorSheet(
    currentSpeed: Float,
    onSelect: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = "Playback speed",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            PLAYBACK_SPEEDS.forEach { speed ->
                val selected = speed == currentSpeed
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(speed) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                ) {
                    Text(text = speed.asSpeedLabel(), style = MaterialTheme.typography.bodyLarge)
                    if (selected) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                    }
                }
            }
        }
    }
}
