package kz.aruzhan.care_steps

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import kz.aruzhan.care_steps.data.remote.ApiClient

class DiplomaApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(ApiClient.imageOkHttp)
            .crossfade(true)
            .build()
}
