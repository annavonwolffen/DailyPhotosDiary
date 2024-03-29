package com.annevonwolffen.authorization_impl.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.annevonwolffen.authorization_impl.R
import com.annevonwolffen.design_system.extensions.doOnApplyWindowInsets
import com.annevonwolffen.design_system.extensions.setStatusBarColor
import com.google.android.material.appbar.AppBarLayout

class AuthorizationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().setStatusBarColor(com.annevonwolffen.design_system.R.color.color_green_300_dark)

        view.findViewById<AppBarLayout>(R.id.appbar).doOnApplyWindowInsets { topInset, _, _ ->
            updatePadding(top = topInset)
        }

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment? ?: return

        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        view.findViewById<Toolbar>(R.id.toolbar)
            .setupWithNavController(navController, appBarConfiguration)
    }
}