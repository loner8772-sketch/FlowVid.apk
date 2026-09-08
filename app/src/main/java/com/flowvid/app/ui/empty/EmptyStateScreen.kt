package com.flowvid.app.ui.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowvid.app.R
import com.flowvid.app.ui.theme.FlowVidBackgroundDark
import com.flowvid.app.ui.theme.FlowVidOnDark

@Composable
fun EmptyStateScreen(
    onScanAgain: () -> Unit,
    onChooseFolders: () -> Unit,
) {
    Surface(color = FlowVidBackgroundDark, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.headlineMedium,
                color = FlowVidOnDark,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = FlowVidOnDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onScanAgain) {
                    Text(stringResource(R.string.empty_scan_again))
                }
                Button(onClick = onChooseFolders) {
                    Text(stringResource(R.string.empty_choose_folders))
                }
            }
        }
    }
}
