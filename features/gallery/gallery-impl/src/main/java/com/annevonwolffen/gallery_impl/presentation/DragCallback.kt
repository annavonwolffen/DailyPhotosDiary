package com.annevonwolffen.gallery_impl.presentation

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.gallery_impl.presentation.DragCallback.RemoveDragState.Dragging
import com.annevonwolffen.ui_utils_api.extensions.setVisibility

internal class DragCallback(
    internal var removeIcon: ImageView?,
    private val onRemoveImage: (imagePosition: Int) -> Unit,
    private val onMoveImage: (prevPosition: Int, newPosition: Int) -> Unit
) :
    ItemTouchHelper.SimpleCallback(RIGHT or LEFT or DOWN, 0) {

    private val imageRect = Rect()
    private val removeBinRect = Rect()

    override fun isLongPressDragEnabled(): Boolean = true

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        onMoveImage(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        itemView.getDrawingRect(imageRect)
        imageRect.offset(recyclerView.x.toInt() + dX.toInt(), recyclerView.y.toInt() + dY.toInt())
        removeIcon?.let {
            it.getDrawingRect(removeBinRect)
            removeBinRect.offset(it.x.toInt(), it.y.toInt())
        }

        val state: RemoveDragState = if (Rect.intersects(imageRect, removeBinRect)) {
            when {
                isCurrentlyActive -> {
                    RemoveDragState.DraggingInRemoveArea(viewHolder.itemView)
                }
                itemView.isVisible -> {
                    RemoveDragState.DroppedInRemoveArea(viewHolder)
                }
                else -> {
                    RemoveDragState.StoppedDragging(viewHolder.itemView)
                }
            }
        } else {
            if (isCurrentlyActive) {
                Dragging(viewHolder.itemView)
            } else {
                RemoveDragState.StoppedDragging(viewHolder.itemView)
            }
        }
        render(state)
    }

    private fun render(state: RemoveDragState) {
        when (state) {
            is Dragging -> {
                state.itemView.alpha = HALF_TRANSPARENT_ALPHA
                removeIcon?.apply {
                    setVisibility(true)
                    imageTintList = ContextCompat.getColorStateList(
                        context,
                        com.annevonwolffen.design_system.R.color.color_green_300_dark
                    )
                }
            }
            is RemoveDragState.DraggingInRemoveArea -> {
                state.itemView.alpha = HALF_TRANSPARENT_ALPHA
                removeIcon?.apply {
                    setVisibility(true)
                    imageTintList = ContextCompat.getColorStateList(
                        context,
                        com.annevonwolffen.design_system.R.color.color_red_800
                    )
                }
            }
            is RemoveDragState.DroppedInRemoveArea -> {
                val viewHolder = state.viewHolder
                onRemoveImage.invoke(viewHolder.absoluteAdapterPosition)
                viewHolder.itemView.setVisibility(false)
                removeIcon?.setVisibility(false)
            }
            is RemoveDragState.StoppedDragging -> {
                state.itemView.alpha = NOT_TRANSPARENT_ALPHA
                removeIcon?.setVisibility(false)
            }
        }
    }

    sealed class RemoveDragState {
        data class Dragging(val itemView: View) : RemoveDragState()
        data class DraggingInRemoveArea(val itemView: View) : RemoveDragState()
        data class DroppedInRemoveArea(val viewHolder: RecyclerView.ViewHolder) : RemoveDragState()
        data class StoppedDragging(val itemView: View) : RemoveDragState()
    }

    private companion object {
        const val NOT_TRANSPARENT_ALPHA = 1f
        const val HALF_TRANSPARENT_ALPHA = 0.5f
    }
}