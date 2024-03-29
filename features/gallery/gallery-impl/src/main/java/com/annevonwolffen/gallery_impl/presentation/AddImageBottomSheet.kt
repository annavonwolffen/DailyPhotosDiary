package com.annevonwolffen.gallery_impl.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.annevonwolffen.di.FeatureProvider.getInnerFeature
import com.annevonwolffen.gallery_api.di.GalleryExternalApi
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.BottomsheetAddImageBinding
import com.annevonwolffen.gallery_impl.di.GalleryInternalApi
import com.annevonwolffen.gallery_impl.presentation.viewmodels.AddImagesViewModel
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding
import com.annevonwolffen.ui_utils_api.viewmodel.ViewModelProviderFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.annevonwolffen.navigation.R as NavR

class AddImageBottomSheet : BottomSheetDialogFragment() {

    private val binding: BottomsheetAddImageBinding by fragmentViewBinding(BottomsheetAddImageBinding::bind)

    private val viewModel: AddImagesViewModel by navGraphViewModels(NavR.id.gallery_graph) {
        ViewModelProviderFactory {
            AddImagesViewModel(
                getInnerFeature(
                    GalleryExternalApi::class,
                    GalleryInternalApi::class
                ).imagesInteractor
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottomsheet_add_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpItemsClick()
        observerDismissEvents()
    }

    private fun setUpItemsClick() {
        binding.fromCamera.setOnClickListener {
            viewModel.addImage(AddImage.FromCamera)
        }
        binding.fromGallery.setOnClickListener {
            viewModel.addImage(AddImage.FromGallery)
        }
    }

    private fun observerDismissEvents() {
        viewModel.dismissBottomSheetEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { dismiss() }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun getTheme(): Int {
        return R.style.ThemeOverlay_BottomSheetDialog
    }

    sealed class AddImage {
        object FromCamera : AddImage()
        object FromGallery : AddImage()
    }
}