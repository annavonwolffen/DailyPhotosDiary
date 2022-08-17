package com.annevonwolffen.gallery_impl.domain

import com.annevonwolffen.gallery_impl.presentation.Result
import kotlinx.coroutines.flow.Flow

internal interface ImagesInteractor {
    fun getImagesFlow(folder: String): Flow<Result<List<Image>>>
    suspend fun uploadImages(folder: String, images: List<Image>): Result<List<Image>>
    suspend fun uploadFilesToStorage(folder: String, images: List<Image>)
    suspend fun deleteImage(folder: String, image: Image): Result<Unit>
    suspend fun deleteFileFromStorage(folder: String, image: Image)
}