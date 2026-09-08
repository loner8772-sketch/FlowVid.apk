package com.flowvid.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flowvid.app.R

@Composable
fun FeedTopBar(
    showBackButton: Boolean,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                ),
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChromeIconButton(
            icon = if (showBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Menu,
            contentDescription = if (showBackButton) stringResource(R.string.cd_back) else stringResource(R.string.cd_menu),
            onClick = if (showBackButton) onBack else onMenu,
        )
        Row {
            ChromeIconButton(
                icon = Icons.Filled.Search,
                contentDescription = stringResource(R.string.cd_search),
                onClick = onSearch,
            )
            ChromeIconButton(
                icon = Icons.Filled.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun ChromeIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = Color.White)
    }
}
