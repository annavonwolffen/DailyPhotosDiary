package com.annevonwolffen.gallery_impl.presentation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.annevonwolffen.coroutine_utils_api.extension.launchFlowCollection
import com.annevonwolffen.design_system.extensions.doOnApplyWindowInsets
import com.annevonwolffen.design_system.extensions.hideKeyboard
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.presentation.utils.getSerializableCompat
import com.annevonwolffen.gallery_impl.presentation.utils.isEqualByDate
import com.annevonwolffen.gallery_impl.presentation.utils.toDateString
import com.annevonwolffen.gallery_impl.presentation.viewmodels.SaveImagesViewModel
import com.annevonwolffen.mainscreen_api.ToolbarFragment
import com.annevonwolffen.ui_utils_api.extensions.setVisibility
import kotlinx.coroutines.launch
import java.util.Calendar

internal class SaveImagesViewDelegate(
    private val fragment: Fragment,
    private val viewModel: SaveImagesViewModel
) {
    private lateinit var dateTextView: TextView
    private lateinit var progressLoader: FrameLayout

    private var selectedCalendar: Calendar = Calendar.getInstance()

    fun onViewCreated(
        savedInstanceState: Bundle?,
        initialCalendarValue: Calendar?,
        scrollContainer: NestedScrollView,
        progressLayout: FrameLayout,
        dateTextView: TextView
    ) {
        selectedCalendar = savedInstanceState?.getSerializableCompat(SELECTED_CALENDAR) ?: initialCalendarValue
            ?: Calendar.getInstance()
        if (savedInstanceState == null) {
            viewModel.clearImages()
        }

        initViews(scrollContainer, progressLayout, dateTextView)
        collectFlows()
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(SELECTED_CALENDAR, selectedCalendar)
    }

    fun inflateToolbarMenu() {
        (fragment.parentFragment?.parentFragment as? ToolbarFragment)
            ?.inflateToolbarMenu(R.menu.menu_add_image, { prepareOptionsMenu(it) }, { onMenuItemSelected(it) })
    }

    private fun initViews(
        scrollContainer: NestedScrollView, progressLayout: FrameLayout, dateTextView: TextView,

        ) {
        scrollContainer.apply {
            doOnApplyWindowInsets { _, _, keyBoardInset ->
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = keyBoardInset
                }
            }
        }

        progressLoader = progressLayout
        setupDateField(dateTextView)
    }

    private fun setupDateField(tvDate: TextView) {
        val todayCalendar = Calendar.getInstance()
        val resources = fragment.resources
        dateTextView = tvDate
        if (dateTextView.text.isEmpty()) {
            dateTextView.text = selectedCalendar.takeIf { !it.isEqualByDate(todayCalendar) }?.toDateString(resources)
                ?: resources.getString(R.string.today)
        }
        dateTextView.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                fragment.requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedCalendar = Calendar.getInstance().also { it.set(year, month, dayOfMonth) }
                    dateTextView.text = if (selectedCalendar.isEqualByDate(todayCalendar)) {
                        resources.getString(R.string.today)
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

    private fun collectFlows() {
        fragment.viewLifecycleOwner.lifecycleScope.launch {
            fragment.viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchFlowCollection(viewModel.progressLoaderState) { isLoading ->
                    progressLoader.setVisibility(isLoading)
                    inflateToolbarMenu()
                }

                launchFlowCollection(viewModel.imageUploadedEvent) {
                    processImageEvent(it)
                }
            }
        }
    }

    private fun processImageEvent(state: State<Unit>) {
        when (state) {
            is State.Success -> {
                fragment.findNavController().popBackStack()
            }
            is State.Error -> {
                Toast.makeText(
                    fragment.requireContext(), "Ошибка при сохранении: ${state.errorMessage}", Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    fragment.requireContext(), "Ошибка при сохранении", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun prepareOptionsMenu(menu: Menu) {
        val saveButton = menu.findItem(R.id.save)
        saveButton.isVisible = viewModel.imagesFlow.value.isNotEmpty() && progressLoader.isVisible.not()
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            viewModel.updateImagesDate(selectedCalendar.timeInMillis)
            viewModel.saveImages()
            fragment.requireActivity().hideKeyboard()
        }
        return true
    }

    private companion object {
        private const val SELECTED_CALENDAR = "SELECTED_CALENDAR"
    }
}