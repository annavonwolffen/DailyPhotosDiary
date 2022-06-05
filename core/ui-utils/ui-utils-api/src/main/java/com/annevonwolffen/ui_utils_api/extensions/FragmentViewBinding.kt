package com.annevonwolffen.ui_utils_api.extensions

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> fragmentViewBinding(viewBindingFactory: (View) -> T): ReadOnlyProperty<Fragment, T> {
    return FragmentViewBindingDelegate(viewBindingFactory)
}

internal class FragmentViewBindingDelegate<T : ViewBinding>(
    private val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var viewBinding: T? = null
    private val lifecycleObserver = BindingLifecycleObserver()

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        checkIsMainThread()
        viewBinding?.let { return it }

        val view = thisRef.requireView()
        thisRef.viewLifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        return viewBindingFactory(view).also { vb -> viewBinding = vb }
    }

    private fun checkIsMainThread() {
        check(Looper.getMainLooper() === Looper.myLooper()) {
            "The method must be called on the main thread"
        }
    }

    private inner class BindingLifecycleObserver : DefaultLifecycleObserver {

        private val mainHandler = Handler(Looper.getMainLooper())

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            owner.lifecycle.removeObserver(this)
            mainHandler.post { viewBinding = null }
        }
    }
}