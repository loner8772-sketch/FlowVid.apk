package com.flowvid.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flowvid.app.R
import com.flowvid.app.domain.model.asSpeedLabel

@Composable
fun FeedBottomBar(
    filename: String?,
    showFilename: Boolean,
    progressFraction: Float,
    onScrubStart: () -> Unit,
    onScrub: (Float) -> Unit,
    onScrubFinished: (Float) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    speed: Float,
    onOpenSpeedSelector: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onOpenInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                ),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (showFilename && !filename.isNullOrBlank()) {
            Text(
                text = filename,
                color = Color.White.copy(alpha = 0.75f),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        ScrubProgressBar(
            progressFraction = progressFraction,
            onScrubStart = onScrubStart,
            onScrub = onScrub,
            onScrubFinished = onScrubFinished,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomIconButton(
                icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                contentDescription = stringResource(if (isMuted) R.string.cd_unmute else R.string.cd_mute),
                onClick = onToggleMute,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                SpeedBadge(speed = speed, onClick = onOpenSpeedSelector)
                BottomIconButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(if (isFavorite) R.string.cd_unfavorite else R.string.cd_favorite),
                    onClick = onToggleFavorite,
                    tint = if (isFavorite) Color(0xFFE3A83B) else Color.White,
                )
                BottomIconButton(
                    icon = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.cd_share),
                    onClick = onShare,
                )
                BottomIconButton(
                    icon = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.cd_info),
                    onClick = onOpenInfo,
                )
            }
        }
    }
}

@Composable
private fun SpeedBadge(speed: Float, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.35f),
        contentColor = Color.White,
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Text(
            text = speed.asSpeedLabel(),
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BottomIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = Color.White,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}
