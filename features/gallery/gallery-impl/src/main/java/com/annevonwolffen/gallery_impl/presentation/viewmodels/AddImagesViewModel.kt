package com.annevonwolffen.gallery_impl.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.annevonwolffen.gallery_impl.domain.ImagesInteractor
import com.annevonwolffen.gallery_impl.presentation.AddImageBottomSheet
import com.annevonwolffen.gallery_impl.presentation.models.Image
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class AddImagesViewModel(imagesInteractor: ImagesInteractor) :
    SaveImagesViewModel(imagesInteractor) {

    val addImageEvent get() = _addImageEvent.receiveAsFlow()
    private val _addImageEvent = Channel<AddImageBottomSheet.AddImage>(Channel.CONFLATED)

    val dismissBottomSheetEvent get() = _dismissBottomSheetEvent.receiveAsFlow()
    private val _dismissBottomSheetEvent = Channel<Unit>(Channel.CONFLATED)

    fun addImage(addImageCommand: AddImageBottomSheet.AddImage) {
        viewModelScope.launch { _addImageEvent.send(addImageCommand) }
    }

    fun removeImageFromAdded(image: Image) {
        _imagesFlow.value = _imagesFlow.value.toMutableSet().apply { remove(image) }
    }

    fun dismissBottomSheet() {
        viewModelScope.launch { _dismissBottomSheetEvent.send(Unit) }
    }

}