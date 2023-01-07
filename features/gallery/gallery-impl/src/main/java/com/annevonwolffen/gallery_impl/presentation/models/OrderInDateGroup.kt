package com.annevonwolffen.gallery_impl.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderInDateGroup(
    override val order: Long,
    override val editTimeMillis: Long
) : com.annevonwolffen.gallery_impl.domain.OrderInDateGroup, Parcelable
