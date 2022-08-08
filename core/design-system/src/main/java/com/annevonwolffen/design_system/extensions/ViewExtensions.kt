package com.annevonwolffen.design_system.extensions

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

typealias OnSystemInsetsChangedListener = View.(topInset: Int, bottomInset: Int) -> Unit

fun View.doOnApplyWindowInsets(listener: OnSystemInsetsChangedListener) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->

        val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom

        listener(topInset, bottomInset)
        insets
    }

    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}