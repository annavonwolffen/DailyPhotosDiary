package com.annevonwolffen.gallery_impl.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.gallery_impl.databinding.ImagesGroupLayoutBinding
import com.annevonwolffen.gallery_impl.presentation.models.Image
import com.annevonwolffen.gallery_impl.presentation.models.ImagesGroup
import com.annevonwolffen.gallery_impl.presentation.utils.toCalendar
import com.annevonwolffen.gallery_impl.presentation.utils.toDateString
import com.annevonwolffen.gallery_impl.presentation.utils.toDayOfWeekString
import com.annevonwolffen.ui_utils_api.image.ImageLoader

class ImagesGroupListAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (Image) -> Unit,
    private val onLongClick: (String, String) -> Unit
) : ListAdapter<ImagesGroup, ImagesGroupListAdapter.ViewHolder>(DiffUtilCallback()) {

    class DiffUtilCallback : DiffUtil.ItemCallback<ImagesGroup>() {
        override fun areItemsTheSame(oldItem: ImagesGroup, newItem: ImagesGroup): Boolean =
            oldItem.date == newItem.date

        override fun areContentsTheSame(oldItem: ImagesGroup, newItem: ImagesGroup): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImagesGroupLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, imageLoader, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ImagesGroupLayoutBinding,
        private val imageLoader: ImageLoader,
        private val onClick: (Image) -> Unit,
        private val onLongClick: (String, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var imagesRecyclerView: RecyclerView

        fun bind(imagesGroup: ImagesGroup) {
            with(imagesGroup) {
                binding.tvDate.text = date.toCalendar().toDateString(binding.root.resources)
                binding.tvDayOfWeek.text = date.toCalendar().toDayOfWeekString(binding.root.resources)
                imagesRecyclerView = binding.rvImages
                val snapHelper = LinearSnapHelper()
                snapHelper.attachToRecyclerView(imagesRecyclerView)
                imagesRecyclerView.onFlingListener = null
                val adapter = ImagesListAdapter(imageLoader, onClick, onLongClick)
                imagesRecyclerView.adapter = adapter
                adapter.submitList(images)
            }
        }
    }
}