package com.annevonwolffen.gallery_impl.data

import com.annevonwolffen.gallery_impl.domain.OrderInDateGroup
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ImageDto(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val createdAt: Long? = null,
    val url: String? = null,
    val orderWithinDateGroup: OrderInDateGroup? = null
)