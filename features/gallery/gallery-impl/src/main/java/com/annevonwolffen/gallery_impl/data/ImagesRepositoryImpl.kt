package com.annevonwolffen.gallery_impl.data

import com.annevonwolffen.coroutine_utils_api.CoroutineDispatchers
import com.annevonwolffen.gallery_impl.data.converters.ImageDtoConverter.convertDataToDomain
import com.annevonwolffen.gallery_impl.data.converters.ImageDtoConverter.convertDomainToData
import com.annevonwolffen.gallery_impl.data.remote.RemoteDataSource
import com.annevonwolffen.gallery_impl.data.remote.RemoteFileStorage
import com.annevonwolffen.gallery_impl.domain.Image
import com.annevonwolffen.gallery_impl.domain.ImagesRepository
import com.annevonwolffen.gallery_impl.presentation.Result
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

internal class ImagesRepositoryImpl(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val remoteDataSource: RemoteDataSource,
    private val remoteFileStorage: RemoteFileStorage
) : ImagesRepository {
    override fun getImagesFlow(folder: String): Flow<Result<List<Image>>> {
        return remoteDataSource.getImagesFlow(folder)
            .map { images ->
                images?.map {
                    convertDataToDomain(it)
                }?.let { Result.Success(it) } ?: Result.Error("No images")
            }
            .flowOn(coroutineDispatchers.ioDispatcher)
    }

    override suspend fun getImages(folder: String): Result<List<Image>> {
        return withContext(coroutineDispatchers.ioDispatcher) {
            runCatching {
                remoteDataSource.getImages(folder)
                    .map { convertDataToDomain(it) }.let { Result.Success(it) }
            }.getOrElse { Result.Error(it.message) }
        }
    }

    override suspend fun uploadImages(folder: String, images: List<Image>): Result<List<Image>> {
        val updatedImages: List<Image> = supervisorScope {
            val imagesWithDeferredIds: List<Pair<Image, Deferred<String>>> = images.map { image ->
                Pair(image, async(coroutineDispatchers.ioDispatcher) {
                    remoteDataSource.uploadImage(
                        folder,
                        convertDomainToData(image)
                    )
                })
            }
            imagesWithDeferredIds.mapNotNull {
                runCatching { it.first.copy(id = it.second.await()) }.getOrNull()
            }
        }

        return if (updatedImages.isNotEmpty()) {
            Result.Success(updatedImages)
        } else {
            Result.Error("Не удалось загрузить изображения.")
        }
    }

    override suspend fun uploadFilesToStorage(folder: String, images: List<Image>) {
        val imagesWithUpdatedUrls: List<Image> = supervisorScope {
            val imagesWithRemoteUrlsDeferred: List<Pair<Image, Deferred<String>>> = images.map { image ->
                Pair(
                    image,
                    async(coroutineDispatchers.ioDispatcher) {
                        remoteFileStorage.uploadFileToStorage(
                            folder,
                            convertDomainToData(image)
                        )
                    })
            }
            imagesWithRemoteUrlsDeferred.mapNotNull {
                runCatching {
                    it.first.copy(url = it.second.await())
                }.getOrElse {
                    // TODO: сохранить image для повторной загрузки в фоне
                    null
                }
            }
        }
        // обновить ссылки на изображения в базе
        uploadImages(folder, imagesWithUpdatedUrls)
    }

    override suspend fun deleteImage(folder: String, image: Image): Result<Unit> {
        return withContext(coroutineDispatchers.ioDispatcher) {
            runCatching {
                remoteDataSource.deleteImage(folder, convertDomainToData(image))
                Result.Success(Unit)
            }.getOrElse { Result.Error(it.message) }
        }
    }

    override suspend fun deleteFileFromStorage(folder: String, image: Image) {
        withContext(coroutineDispatchers.ioDispatcher) {
            remoteFileStorage.deleteFileFromStorage(folder, convertDomainToData(image))
        }
    }
}