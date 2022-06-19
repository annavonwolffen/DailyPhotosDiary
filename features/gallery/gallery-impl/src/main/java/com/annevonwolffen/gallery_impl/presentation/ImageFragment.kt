package com.annevonwolffen.gallery_impl.presentation

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.annevonwolffen.design_system.extensions.doOnApplyWindowInsets
import com.annevonwolffen.design_system.extensions.getActionBarHeight
import com.annevonwolffen.design_system.extensions.hideSystemBars
import com.annevonwolffen.design_system.extensions.showSystemBars
import com.annevonwolffen.di.FeatureProvider
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.FragmentImageBinding
import com.annevonwolffen.mainscreen_api.ToolbarFragment
import com.annevonwolffen.ui_utils_api.UiUtilsApi
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding
import com.annevonwolffen.design_system.R as DesignR

internal class ImageFragment : Fragment(R.layout.fragment_image) {

    private val binding: FragmentImageBinding by fragmentViewBinding(FragmentImageBinding::bind)

    private val args: ImageFragmentArgs by navArgs()

    private lateinit var rootLayout: FrameLayout

    private var isFullScreen = false
    private var statusBarSize: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FeatureProvider.getFeature(UiUtilsApi::class).imageLoader.loadImage(binding.image, args.imageUrl)

        (parentFragment?.parentFragment as? ToolbarFragment)?.clearToolbarMenu()

        rootLayout = binding.root

        isFullScreen = savedInstanceState?.getBoolean(IS_FULL_SCREEN) ?: isFullScreen
        rootLayout.setOnClickListener {
            rootLayout.setBackgroundColor(
                resources.getColor(
                    if (isFullScreen) {
                        DesignR.color.color_gray_150
                    } else {
                        DesignR.color.color_black
                    },
                    requireActivity().theme
                )
            )
            if (isFullScreen) {
                requireActivity().showSystemBars()
                (parentFragment?.parentFragment as? ToolbarFragment)?.setToolbarVisibility(true)
                rootLayout.updatePadding(top = 0)
            } else {
                requireActivity().hideSystemBars()
                (parentFragment?.parentFragment as? ToolbarFragment)?.setToolbarVisibility(false)
                rootLayout.updatePadding(top = statusBarSize + requireActivity().getActionBarHeight())
            }
            isFullScreen = isFullScreen.not()
        }

        rootLayout.doOnApplyWindowInsets { topInset, _ ->
            if (topInset != 0) {
                statusBarSize = topInset
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_FULL_SCREEN, isFullScreen)
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val IS_FULL_SCREEN = "IS_FULL_SCREEN"
    }
}