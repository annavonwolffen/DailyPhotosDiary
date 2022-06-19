package com.annevonwolffen.gallery_impl.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.annevonwolffen.di.FeatureProvider
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.FragmentImageBinding
import com.annevonwolffen.ui_utils_api.UiUtilsApi
import com.annevonwolffen.ui_utils_api.extensions.fragmentViewBinding

internal class ImageFragment : Fragment(R.layout.fragment_image) {

    private val binding: FragmentImageBinding by fragmentViewBinding(FragmentImageBinding::bind)

    private val args: ImageFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FeatureProvider.getFeature(UiUtilsApi::class).imageLoader.loadImage(binding.image, args.imageUrl)
    }
}