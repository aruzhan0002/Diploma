package com.example.diploma

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.diploma.data.remote.ApiClient

class DiplomaApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(ApiClient.imageOkHttp)
            .crossfade(true)
            .build()
}
