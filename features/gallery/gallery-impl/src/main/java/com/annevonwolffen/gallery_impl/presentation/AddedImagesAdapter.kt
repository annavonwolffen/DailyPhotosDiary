package com.annevonwolffen.gallery_impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.gallery_impl.databinding.AddedImagesItemBinding
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.ui_utils_api.image.ImageLoader

internal class AddedImagesAdapter(
    private val imageLoader: ImageLoader,
    private val onDescriptionChanged: (Image, String) -> Unit,
    private val onRemoveFromAdded: (Image) -> Unit
) : ListAdapter<Image, AddedImagesAdapter.ViewHolder>(DiffUtilCallback()) {

    private lateinit var dragCallback: DragCallback

    class DiffUtilCallback : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
            oldItem.id == newItem.id && oldItem.url == newItem.url

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean = oldItem == newItem

        override fun getChangePayload(oldItem: Image, newItem: Image): Any? {
            if (oldItem.description != newItem.description) {
                return Payload.DESCRIPTION
            }
            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AddedImagesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, imageLoader, onDescriptionChanged)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == Payload.DESCRIPTION) {
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        dragCallback = DragCallback(recyclerView.rootView.findViewById(R.id.btn_cancel_add)) { position ->
            onRemoveFromAdded(getItem(position))
        }
        ItemTouchHelper(dragCallback).attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        dragCallback.removeIcon = null
    }

    class ViewHolder(
        private val binding: AddedImagesItemBinding,
        private val imageLoader: ImageLoader,
        private val onDescriptionChanged: (Image, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            imageLoader.loadImage(binding.ivAddedImage, image.url)
            binding.etAddedImageDescription.setText(image.description)
            binding.etAddedImageDescription.doAfterTextChanged { text ->
                onDescriptionChanged.invoke(image, text.toString())
            }
        }
    }

    enum class Payload {
        DESCRIPTION
    }
}