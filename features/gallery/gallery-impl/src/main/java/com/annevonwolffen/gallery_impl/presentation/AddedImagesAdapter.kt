package com.annevonwolffen.gallery_impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.gallery_impl.databinding.AddedImagesItemBinding
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.ui_utils_api.image.ImageLoader

internal class AddedImagesAdapter(private val imageLoader: ImageLoader) :
    ListAdapter<Image, AddedImagesAdapter.ViewHolder>(DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AddedImagesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, imageLoader)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem == newItem
    }

    class ViewHolder(
        private val binding: AddedImagesItemBinding,
        private val imageLoader: ImageLoader,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            imageLoader.loadImage(binding.ivAddedImage, image.url)
        }
    }
}