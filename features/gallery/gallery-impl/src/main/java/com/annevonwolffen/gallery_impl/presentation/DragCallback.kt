package com.annevonwolffen.gallery_impl.presentation

import android.graphics.Canvas
import android.graphics.Rect
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.annevonwolffen.gallery_impl.R
import com.annevonwolffen.ui_utils_api.extensions.setVisibility

internal class DragCallback(private val onRemoveImage: (imagePosition: Int) -> Unit) :
    ItemTouchHelper.SimpleCallback(RIGHT or LEFT or DOWN, 0) {

    private lateinit var removeBinImage: ImageView
    private val imageRect = Rect()
    private val removeBinRect = Rect()

    private var inDeleteArea = false
    private var isToBeRemoved = false

    override fun isLongPressDragEnabled(): Boolean = true

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ACTION_STATE_IDLE) {
            if (inDeleteArea) {
                isToBeRemoved = true
            }
        }
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

        if (isToBeRemoved) {
            onRemoveImage.invoke(viewHolder.absoluteAdapterPosition)
            viewHolder.itemView.setVisibility(false)
            removeBinImage.setVisibility(false)
            isToBeRemoved = false
        }

        if (::removeBinImage.isInitialized.not()) {
            removeBinImage = recyclerView.rootView.findViewById(R.id.btn_cancel_add)
        }
        if (::removeBinImage.isInitialized && actionState == ACTION_STATE_DRAG && isCurrentlyActive) {
            removeBinImage.setVisibility(true)
        }

        val itemView = viewHolder.itemView

        itemView.getDrawingRect(imageRect)
        imageRect.offset(recyclerView.x.toInt() + dX.toInt(), recyclerView.y.toInt() + dY.toInt())
        removeBinImage.getDrawingRect(removeBinRect)
        removeBinRect.offset(removeBinImage.x.toInt(), removeBinImage.y.toInt())


        if (Rect.intersects(imageRect, removeBinRect)) {
            itemView.alpha = 0.5f
            removeBinImage.imageTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.annevonwolffen.design_system.R.color.color_red_800
            )
            inDeleteArea = true
        } else {
            inDeleteArea = false
            viewHolder.itemView.alpha = 1f
            removeBinImage.imageTintList = ContextCompat.getColorStateList(
                itemView.context,
                com.annevonwolffen.design_system.R.color.color_green_300_dark
            )
        }
    }
}