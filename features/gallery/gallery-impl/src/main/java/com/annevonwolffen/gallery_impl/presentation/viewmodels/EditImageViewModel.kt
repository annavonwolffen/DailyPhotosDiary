package com.annevonwolffen.gallery_impl.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.annevonwolffen.gallery_impl.domain.ImagesInteractor
import com.annevonwolffen.gallery_impl.presentation.Result
import com.annevonwolffen.gallery_impl.presentation.State
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.models.toDomain
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class EditImageViewModel(private val imagesInteractor: ImagesInteractor) :
    SaveImagesViewModel(imagesInteractor) {

    val imageDeletedEvent get() = _imageDeletedEvent.receiveAsFlow()
    private val _imageDeletedEvent = Channel<State<Unit>>(Channel.CONFLATED)

    private val deleteExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, "Ошибка при удалении изображения: $throwable")
        _progressLoaderState.value = false
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch(deleteExceptionHandler) {
            _progressLoaderState.value = true
            imagesInteractor.deleteImage(TEST_FOLDER, image.toDomain()).also {
                _progressLoaderState.value = false
                when (it) {
                    is Result.Success -> {
                        _imageDeletedEvent.send(State.Success(Unit))
                        imagesInteractor.deleteFileFromStorage(TEST_FOLDER, image.toDomain())
                    }
                    is Result.Error -> {
                        _imageDeletedEvent.send(State.Error(it.errorMessage))
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "EditImageViewModel"
    }
}