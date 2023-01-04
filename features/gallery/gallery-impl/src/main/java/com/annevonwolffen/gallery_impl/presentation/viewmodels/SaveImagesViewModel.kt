package com.annevonwolffen.gallery_impl.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annevonwolffen.gallery_impl.domain.ImagesInteractor
import com.annevonwolffen.gallery_impl.presentation.Result
import com.annevonwolffen.gallery_impl.presentation.State
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.models.toDomain
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal open class SaveImagesViewModel(private val imagesInteractor: ImagesInteractor) : ViewModel() {
    val imagesFlow: StateFlow<Set<Image>> get() = _imagesFlow
    protected val _imagesFlow: MutableStateFlow<Set<Image>> = MutableStateFlow(emptySet())

    val imageUploadedEvent get() = _imageUploadedEvent.receiveAsFlow()
    private val _imageUploadedEvent = Channel<State<Unit>>(Channel.CONFLATED)

    val progressLoaderState: StateFlow<Boolean> get() = _progressLoaderState
    protected val _progressLoaderState = MutableStateFlow(false)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w(TAG, "Ошибка при сохранении: $throwable")
        _progressLoaderState.value = false
    }

    fun addImages(images: List<Image>) {
        _imagesFlow.value = _imagesFlow.value.toMutableSet().apply { addAll(images) }
    }

    fun addImage(image: Image) {
        addImages(listOf(image))
    }

    fun updateImagesDate(date: Long) {
        _imagesFlow.value = _imagesFlow.value.map { image ->
            image.copy(date = date)
        }.toSet()
    }

    fun updateImageDescription(image: Image, description: String) {
        _imagesFlow.value = _imagesFlow.value.toMutableList().apply {
            replaceAll {
                if (it.url == image.url && it.description.equals(description).not()) {
                    it.copy(description = description)
                } else it
            }
        }.toSet()
    }

    fun saveImages() {
        viewModelScope.launch(exceptionHandler) {
            _progressLoaderState.value = true
            imagesInteractor.uploadImages(TEST_FOLDER, _imagesFlow.value.map { it.toDomain() }).also {
                _progressLoaderState.value = false
                when (it) {
                    is Result.Success -> {
                        _imageUploadedEvent.send(State.Success(Unit))
                        imagesInteractor.uploadFilesToStorage(TEST_FOLDER, it.value)
                    }
                    is Result.Error -> {
                        _imageUploadedEvent.send(State.Error(it.errorMessage))
                    }
                }
            }
        }
    }

    fun clearImages() {
        _imagesFlow.value = emptySet()
    }

    companion object {
        private const val TAG = "SaveImagesViewModel"

        @Suppress("JVM_STATIC_ON_CONST_OR_JVM_FIELD")
        @JvmStatic
        protected const val TEST_FOLDER = "testfolder"
    }
}