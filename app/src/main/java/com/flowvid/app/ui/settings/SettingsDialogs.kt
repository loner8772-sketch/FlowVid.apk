package com.flowvid.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flowvid.app.R
import com.flowvid.app.domain.model.SortOrder
import com.flowvid.app.domain.model.ThemeMode

@Composable
fun SortPickerDialog(current: SortOrder, onSelect: (SortOrder) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        SortOrder.RECENT to stringResource(R.string.sort_recent),
        SortOrder.OLDEST to stringResource(R.string.sort_oldest),
        SortOrder.NAME to stringResource(R.string.sort_name),
        SortOrder.DURATION to stringResource(R.string.sort_duration),
        SortOrder.RANDOM to stringResource(R.string.sort_random),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        title = { Text(stringResource(R.string.sort_title)) },
        text = {
            Column {
                options.forEach { (order, label) ->
                    RadioRow(label = label, selected = order == current, onClick = { onSelect(order) })
                }
            }
        },
    )
}

@Composable
fun ThemePickerDialog(current: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    RadioRow(label = label, selected = mode == current, onClick = { onSelect(mode) })
                }
            }
        },
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
    }
}
