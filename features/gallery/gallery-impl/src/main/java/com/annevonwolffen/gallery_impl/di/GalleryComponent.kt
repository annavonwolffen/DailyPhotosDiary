package com.annevonwolffen.gallery_impl.di

import com.annevonwolffen.coroutine_utils_api.CoroutineUtilsApi
import com.annevonwolffen.di.PerFeature
import com.annevonwolffen.gallery_impl.di.external.GalleryExternalModule
import com.annevonwolffen.preferences_api.PreferencesApi
import dagger.Component

@PerFeature
@Component(
    modules = [GalleryInternalModule::class, GalleryExternalModule::class, GallerySettingsModule::class],
    dependencies = [CoroutineUtilsApi::class, PreferencesApi::class]
)
internal interface GalleryComponent : GalleryInternalApi {

    @Component.Factory
    interface Factory {
        fun create(coroutineUtilsApi: CoroutineUtilsApi, preferencesApi: PreferencesApi): GalleryComponent
    }
}