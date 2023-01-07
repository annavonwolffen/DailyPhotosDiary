package com.annevonwolffen.gallery_impl.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.annevonwolffen.gallery_impl.domain.Image as DomainImage

@Parcelize
data class Image(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val date: Long?,
    val url: String,
    val orderWithinDateGroup: OrderInDateGroup? = null
) : Parcelable

fun Image.toDomain(): DomainImage = DomainImage(id, name, description, date ?: 0L, url, orderWithinDateGroup)
fun DomainImage.toPresentation() =
    Image(id, name, description, date, url, orderWithinDateGroup?.let { OrderInDateGroup(it.order, it.editTimeMillis) })
