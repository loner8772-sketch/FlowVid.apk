package com.flowvid.app.ui.common

import android.content.Context
import coil3.ImageLoader
import coil3.video.VideoFrameDecoder

object ThumbnailImageLoader {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader = instance ?: synchronized(this) {
        instance ?: ImageLoader.Builder(context.applicationContext)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
            .also { instance = it }
    }
}
