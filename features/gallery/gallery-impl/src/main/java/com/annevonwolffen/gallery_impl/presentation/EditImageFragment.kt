package com.annevonwolffen.gallery_impl.presentation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.annevonwolffen.coroutine_utils_api.extension.launchFlowCollection
import com.annevonwolffen.design_system.extensions.doOnApplyWindowInsets
import com.annevonwolffen.di.FeatureProvider
import com.annevonwolffen.di.FeatureProvider.getInnerFeature
import com.annevonwolffen.gallery_api.di.GalleryExternalApi
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.FragmentEditImageBinding
import com.annevonwolffen.gallery_impl.di.GalleryInternalApi
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.utils.toCalendar
import com.annevonwolffen.gallery_impl.presentation.viewmodels.EditImageViewModel
import com.annevonwolffen.ui_utils_api.UiUtilsApi
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding
import com.annevonwolffen.ui_utils_api.image.ImageLoader
import com.annevonwolffen.ui_utils_api.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.launch

internal class EditImageFragment : Fragment(R.layout.fragment_edit_image) {

    private val binding: FragmentEditImageBinding by fragmentViewBinding(FragmentEditImageBinding::bind)

    private val viewModel: EditImageViewModel by viewModels {
        ViewModelProviderFactory {
            EditImageViewModel(getInnerFeature(GalleryExternalApi::class, GalleryInternalApi::class).imagesInteractor)
        }
    }

    private val args: EditImageFragmentArgs by navArgs()
    private val imageToEdit: Image by lazy { args.image }

    private lateinit var saveImagesDelegate: SaveImagesViewDelegate

    private val imageLoader: ImageLoader by lazy { FeatureProvider.getFeature(UiUtilsApi::class).imageLoader }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveImagesDelegate = SaveImagesViewDelegate(this, viewModel)
        saveImagesDelegate.onViewCreated(
            savedInstanceState,
            imageToEdit.date?.toCalendar(),
            binding.scrollContainer,
            binding.progressLayout,
            binding.tvDate
        )
        imageToEdit.takeIf { savedInstanceState == null }?.let {
            viewModel.initialDate = it.date ?: 0L
            viewModel.addImage(it)
        }
        initViews()
        collectFlows()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveImagesDelegate.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initViews() {
        imageLoader.loadImage(binding.editImage.ivAddedImage, imageToEdit.url)
        binding.editImage.etAddedImageDescription.setText(imageToEdit.description)
        binding.editImage.etAddedImageDescription.doAfterTextChanged { text ->
            viewModel.updateImageDescription(imageToEdit, text.toString())
        }
        setupDeleteButton()
    }

    private fun setupDeleteButton() {
        binding.btnDelete.apply {
            setOnClickListener { viewModel.deleteImage(imageToEdit) }
            doOnApplyWindowInsets { _, bottomInset, _ ->
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin =
                        resources.getDimensionPixelOffset(com.annevonwolffen.design_system.R.dimen.margin_medium) + bottomInset
                }
            }
        }
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchFlowCollection(viewModel.imageDeletedEvent) {
                    processImageEvent(it)
                }
            }
        }
    }

    private fun processImageEvent(state: State<Unit>) {
        when (state) {
            is State.Success -> {
                findNavController().popBackStack()
            }
            is State.Error -> {
                Toast.makeText(
                    requireContext(), "Ошибка при удалении: ${state.errorMessage}", Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(), "Ошибка при удалении", Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}