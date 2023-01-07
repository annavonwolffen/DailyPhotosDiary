package com.annevonwolffen.gallery_impl.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearSnapHelper
import com.annevonwolffen.coroutine_utils_api.extension.launchFlowCollection
import com.annevonwolffen.di.FeatureProvider
import com.annevonwolffen.gallery_api.di.GalleryExternalApi
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.FragmentAddImagesBinding
import com.annevonwolffen.gallery_impl.di.GalleryInternalApi
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.utils.createFileFromUri
import com.annevonwolffen.gallery_impl.presentation.utils.createImageFile
import com.annevonwolffen.gallery_impl.presentation.utils.getUriForFile
import com.annevonwolffen.gallery_impl.presentation.viewmodels.AddImagesViewModel
import com.annevonwolffen.ui_utils_api.UiUtilsApi
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding
import com.annevonwolffen.ui_utils_api.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.launch
import java.io.File

internal class AddImagesFragment : Fragment(R.layout.fragment_add_images) {

    private val binding: FragmentAddImagesBinding by fragmentViewBinding(FragmentAddImagesBinding::bind)

    private val viewModel: AddImagesViewModel by navGraphViewModels(com.annevonwolffen.navigation.R.id.gallery_graph) {
        ViewModelProviderFactory {
            AddImagesViewModel(
                FeatureProvider.getInnerFeature(
                    GalleryExternalApi::class, GalleryInternalApi::class
                ).imagesInteractor
            )
        }
    }

    private lateinit var addedImagesAdapter: AddedImagesAdapter

    private var currentCreatedPhotoFile: File? = null

    private val pickMultipleImagesLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                val images = uris.map { uri -> createFileFromUri(uri, requireContext()) }
                    .map { file -> constructImageFromFile(file) }
                viewModel.addImages(images)
            } else {
                Log.d(TAG, "PhotoPicker: No media selected")
            }
            viewModel.dismissBottomSheet()
        }
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            currentCreatedPhotoFile?.let { file ->
                viewModel.addImage(constructImageFromFile(file))
            }
            viewModel.dismissBottomSheet()
        } else {
            currentCreatedPhotoFile = null
        }
    }

    private lateinit var saveImagesDelegate: SaveImagesViewDelegate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveImagesDelegate = SaveImagesViewDelegate(this, viewModel)
        saveImagesDelegate.onViewCreated(
            savedInstanceState, null, binding.scrollContainer, binding.progressLayout, binding.tvDate
        )

        initViews()
        collectFlows()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CURRENT_PHOTO_FILE, currentCreatedPhotoFile)
        saveImagesDelegate.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun constructImageFromFile(file: File): Image = Image(
        name = file.name, date = null, url = file.getUriForFile(requireContext()).toString()
    )

    private fun initViews() {
        setupAddImageButton()
        setupRecyclerView()
    }

    private fun setupAddImageButton() {
        binding.btnImage.setOnClickListener {
            findNavController().navigate(AddImagesFragmentDirections.actionToAddImageBottomSheet())
        }
    }

    private fun setupRecyclerView() {
        addedImagesAdapter = AddedImagesAdapter(FeatureProvider.getFeature(UiUtilsApi::class).imageLoader,
            { image, text -> viewModel.updateImageDescription(image, text) },
            { image -> viewModel.removeImageFromAdded(image) },
            { prevPos, newPos -> viewModel.moveImage(prevPos, newPos) }
        )
        binding.rvAddedImages.apply {
            adapter = addedImagesAdapter
            LinearSnapHelper().attachToRecyclerView(this)
        }
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchFlowCollection(viewModel.imagesFlow) { images ->
                    addedImagesAdapter.updateAdapterList(images.toList())
                    saveImagesDelegate.inflateToolbarMenu()
                }

                launchFlowCollection(viewModel.addImageEvent) { addImageCommand ->
                    when (addImageCommand) {
                        is AddImageBottomSheet.AddImage.FromCamera -> takePhotoFromCamera()
                        is AddImageBottomSheet.AddImage.FromGallery -> selectImageFromGallery()
                    }
                }
            }
        }
    }

    private fun AddedImagesAdapter.updateAdapterList(images: List<Image>) {
        val newImages: MutableList<Image> = mutableListOf()
        newImages.addAll(images)
        submitList(newImages)
    }

    private fun selectImageFromGallery() {
        pickMultipleImagesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun takePhotoFromCamera() {
        runCatching { createImageFile(requireContext()) }.onSuccess { file ->
            currentCreatedPhotoFile = file
            takePhotoLauncher.launch(file.getUriForFile(requireContext()))
        }.onFailure { Log.d(TAG, "Ошибка при создании файла.") }
    }

    private companion object {
        const val TAG = "AddImagesFragment"
        private const val CURRENT_PHOTO_FILE = "CURRENT_PHOTO_FILE"
    }
}