package com.flowvid.app.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object MediaFormatters {

    fun duration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    fun fileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "${value.toInt()} ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
        }
    }

    fun resolution(width: Int, height: Int): String =
        if (width > 0 && height > 0) "$width \u00d7 $height" else "Unknown"

    fun dateFromEpochSeconds(epochSeconds: Long): String {
        if (epochSeconds <= 0) return "Unknown"
        val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return formatter.format(Date(epochSeconds * 1000))
    }

    /** A short, human "3m ago" / "yesterday" style label, used nowhere critical so kept simple. */
    fun relativeTime(epochMillis: Long): String {
        val diffMs = abs(System.currentTimeMillis() - epochMillis)
        val minutes = diffMs / 60_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            else -> "${minutes / (60 * 24)}d ago"
        }
    }
}
