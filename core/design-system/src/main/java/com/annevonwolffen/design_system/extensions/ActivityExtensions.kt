package com.annevonwolffen.design_system.extensions

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.removeNavBarInset(listener: OnSystemInsetsChangedListener = { _, _ -> }) {
    window.decorView.removeNavBarInset(listener)
}

fun Activity.setStatusBarColor(@ColorRes colorId: Int) {
    window.statusBarColor = resources.getColor(colorId, theme)
}

fun Activity.hideKeyboard() {
    currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Activity.hideSystemBars() {
    val windowInsetsController =
        ViewCompat.getWindowInsetsController(window.decorView) ?: return
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

fun Activity.showSystemBars() {
    val windowInsetsController =
        ViewCompat.getWindowInsetsController(window.decorView) ?: return
    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}

fun Activity.getActionBarHeight(): Int {
    val tv = TypedValue()
    return if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
    } else {
        0
    }
}