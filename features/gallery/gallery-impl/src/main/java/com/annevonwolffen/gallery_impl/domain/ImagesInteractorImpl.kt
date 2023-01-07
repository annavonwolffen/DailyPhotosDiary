package com.annevonwolffen.gallery_impl.domain

import com.annevonwolffen.gallery_impl.presentation.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImagesInteractorImpl @Inject constructor(private val imagesRepository: ImagesRepository) : ImagesInteractor {
    override fun getImagesFlow(folder: String): Flow<Result<List<Image>>> {
        return imagesRepository.getImagesFlow(folder)
    }

    override suspend fun uploadImages(folder: String, images: List<Image>): Result<List<Image>> {
        return imagesRepository.uploadImages(folder, images)
    }

    override suspend fun uploadFilesToStorage(folder: String, images: List<Image>) {
        return imagesRepository.uploadFilesToStorage(folder, images)
    }

    override suspend fun deleteImage(folder: String, image: Image): Result<Unit> {
        return imagesRepository.deleteImage(folder, image)
    }

    override suspend fun deleteFileFromStorage(folder: String, image: Image) {
        return imagesRepository.deleteFileFromStorage(folder, image)
    }
}