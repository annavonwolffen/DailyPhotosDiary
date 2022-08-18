package com.annevonwolffen.gallery_impl.presentation

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.coroutine_utils_api.extension.launchFlowCollection
import com.annevonwolffen.design_system.extensions.doOnApplyWindowInsets
import com.annevonwolffen.design_system.extensions.hideKeyboard
import com.annevonwolffen.di.FeatureProvider.getFeature
import com.annevonwolffen.di.FeatureProvider.getInnerFeature
import com.annevonwolffen.gallery_api.di.GalleryExternalApi
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.FragmentAddImageBinding
import com.annevonwolffen.gallery_impl.di.GalleryInternalApi
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.models.toDomain
import com.annevonwolffen.gallery_impl.presentation.models.toPresentation
import com.annevonwolffen.gallery_impl.presentation.utils.createFileFromUri
import com.annevonwolffen.gallery_impl.presentation.utils.createImageFile
import com.annevonwolffen.gallery_impl.presentation.utils.getUriForFile
import com.annevonwolffen.gallery_impl.presentation.utils.isEqualByDate
import com.annevonwolffen.gallery_impl.presentation.utils.toCalendar
import com.annevonwolffen.gallery_impl.presentation.utils.toDateString
import com.annevonwolffen.gallery_impl.presentation.viewmodels.AddImageViewModel
import com.annevonwolffen.mainscreen_api.ToolbarFragment
import com.annevonwolffen.ui_utils_api.UiUtilsApi
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding
import com.annevonwolffen.ui_utils_api.extensions.setVisibility
import com.annevonwolffen.ui_utils_api.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import com.annevonwolffen.navigation.R as NavR

class AddImageFragment : Fragment(R.layout.fragment_add_image) {

    private val binding: FragmentAddImageBinding by fragmentViewBinding(FragmentAddImageBinding::bind)

    private val viewModel: AddImageViewModel by navGraphViewModels(NavR.id.gallery_graph) {
        ViewModelProviderFactory {
            AddImageViewModel(
                getInnerFeature(
                    GalleryExternalApi::class,
                    GalleryInternalApi::class
                ).imagesInteractor
            )
        }
    }

    private val args: AddImageFragmentArgs by navArgs()
    private val imageToEdit: Image? by lazy { args.image }

    private lateinit var dateTextView: TextView
    private lateinit var progressLoader: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var addedImagesAdapter: AddedImagesAdapter

    private var selectedCalendar: Calendar = Calendar.getInstance()

    private var currentCreatedPhotoFile: File? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val createdImageFile: File? = result.data?.data?.let {
                createFileFromUri(it, requireContext())
            }
                ?: currentCreatedPhotoFile

            createdImageFile?.let { file ->
                viewModel.addImage(
                    constructImageFromFile(file).toDomain()
                )
            }
            viewModel.dismissBottomSheet()
        } else {
            currentCreatedPhotoFile = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedCalendar = savedInstanceState?.getSerializable(SELECTED_CALENDAR) as? Calendar
            ?: imageToEdit?.date?.toCalendar()
                ?: Calendar.getInstance()
        if (savedInstanceState == null) {
            viewModel.clearImages()
        }
        imageToEdit?.takeIf { savedInstanceState == null }?.let {
            viewModel.addImage(it.toDomain())
        }
        initViews()
        collectFlows()
        inflateToolbarMenu()
    }

    private fun inflateToolbarMenu() {
        (parentFragment?.parentFragment as? ToolbarFragment)
            ?.inflateToolbarMenu(R.menu.menu_add_image, { prepareOptionsMenu(it) }, { onMenuItemSelected(it) })
    }

    private fun initViews() {
        progressLoader = binding.progressLayout
        setupDateField()
        setupAddImageButton()
        setupRecyclerView()
        setupDeleteButton()
    }

    private fun setupAddImageButton() {
        binding.btnImage.apply {
            setOnClickListener {
                findNavController().navigate(AddImageFragmentDirections.actionToAddImageBottomSheet())
            }
            setVisibility(imageToEdit == null)
        }
    }

    private fun setupDateField() {
        val todayCalendar = Calendar.getInstance()
        dateTextView = binding.tvDate
        if (dateTextView.text.isEmpty()) {
            dateTextView.text =
                selectedCalendar.takeIf { !it.isEqualByDate(todayCalendar) }?.toDateString(resources)
                    ?: getString(R.string.today)
        }
        dateTextView.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedCalendar = Calendar.getInstance().also { it.set(year, month, dayOfMonth) }
                    dateTextView.text =
                        if (selectedCalendar.isEqualByDate(todayCalendar)) {
                            getString(R.string.today)
                        } else {
                            selectedCalendar.toDateString(resources)
                        }
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rvAddedImages
        addedImagesAdapter = AddedImagesAdapter(getFeature(UiUtilsApi::class).imageLoader)
        recyclerView.adapter = addedImagesAdapter
    }

    private fun setupDeleteButton() {
        binding.btnDelete.apply {
            setVisibility(imageToEdit?.id != null)
            imageToEdit?.let { im -> setOnClickListener { viewModel.deleteImage(im.toDomain()) } }
            doOnApplyWindowInsets { _, bottomInset ->
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin =
                        resources.getDimensionPixelOffset(com.annevonwolffen.design_system.R.dimen.margin_medium) + bottomInset
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(SELECTED_CALENDAR, selectedCalendar)
        outState.putSerializable(CURRENT_PHOTO_FILE, currentCreatedPhotoFile)
        super.onSaveInstanceState(outState)
    }

    private fun prepareOptionsMenu(menu: Menu) {
        val saveButton = menu.findItem(R.id.save)
        saveButton.isVisible = viewModel.imagesFlow.value.isNotEmpty() && progressLoader.isVisible.not()
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            viewModel.updateImagesDate(selectedCalendar.timeInMillis)
            viewModel.saveImages()
            requireActivity().hideKeyboard()
        }
        return true
    }

    private fun collectFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchFlowCollection(viewModel.imagesFlow) { images ->
                    addedImagesAdapter.updateAdapterList(images.map { it.toPresentation() })
                    inflateToolbarMenu()
                }

                launchFlowCollection(viewModel.progressLoaderState) { isLoading ->
                    progressLoader.setVisibility(isLoading)
                    inflateToolbarMenu()
                }

                launchFlowCollection(viewModel.imageUploadedEvent) {
                    processImageEvent(it, "Ошибка при сохранении изображения")
                }

                launchFlowCollection(viewModel.imageDeletedEvent) {
                    processImageEvent(it, "Ошибка при удалении изображения")
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

    private fun processImageEvent(state: State<Unit>, errorMessage: String) {
        when (state) {
            is State.Success -> {
                findNavController().popBackStack()
            }
            is State.Error -> {
                Toast.makeText(
                    activity,
                    "$errorMessage: ${state.errorMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    activity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun selectImageFromGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(pickImageIntent)
    }

    private fun takePhotoFromCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePhotoIntent ->
            takePhotoIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? =
                    kotlin.runCatching { createImageFile(requireContext()) }
                        .onFailure { Log.d(TAG, "Ошибка при создании файла.") }
                        .getOrNull()
                photoFile?.also {
                    currentCreatedPhotoFile = it
                    val photoURI: Uri = it.getUriForFile(requireContext())
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultLauncher.launch(takePhotoIntent)
                }
            }
        }
    }

    private fun constructImageFromFile(file: File): Image =
        Image(
            name = file.name,
            date = selectedCalendar.timeInMillis,
            url = file.getUriForFile(requireContext()).toString()
        )

    private companion object {
        const val TAG = "AddImageFragment"

        private const val SELECTED_CALENDAR = "SELECTED_CALENDAR"
        private const val CURRENT_PHOTO_FILE = "CURRENT_PHOTO_FILE"
    }
}