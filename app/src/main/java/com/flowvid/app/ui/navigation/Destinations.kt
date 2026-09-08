package com.flowvid.app.ui.navigation

import android.net.Uri

object Destinations {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val FOLDERS = "folders"

    const val FEED_SOURCE_ARG = "source"
    const val FEED_START_ID_ARG = "startId"
    const val FEED_ROUTE = "feed/{$FEED_SOURCE_ARG}?$FEED_START_ID_ARG={$FEED_START_ID_ARG}"

    const val FEED_SOURCE_LIBRARY = "library"
    const val FEED_SOURCE_FAVORITES = "favorites"

    /** [startId] is a MediaStore content Uri string; it must be percent-encoded to survive as a query value. */
    fun feedRoute(source: String, startId: String? = null): String {
        val base = "feed/$source"
        return if (startId != null) "$base?$FEED_START_ID_ARG=${Uri.encode(startId)}" else base
    }
}
