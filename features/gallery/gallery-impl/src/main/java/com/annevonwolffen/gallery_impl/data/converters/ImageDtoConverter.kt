package com.annevonwolffen.gallery_impl.data.converters

import com.annevonwolffen.gallery_impl.data.ImageDto
import com.annevonwolffen.gallery_impl.data.OrderInDateGroup
import com.annevonwolffen.gallery_impl.domain.Image

internal object ImageDtoConverter {
    fun convertDataToDomain(image: ImageDto): Image =
        with(image) {
            Image(
                id.orEmpty(),
                name.orEmpty(),
                description.orEmpty(),
                createdAt ?: 0L,
                url.orEmpty(),
                orderWithinDateGroup
            )
        }

    fun convertDomainToData(image: Image): ImageDto =
        with(image) {
            ImageDto(
                id = id,
                name = name,
                description = description,
                createdAt = date,
                url = url,
                orderWithinDateGroup = orderWithinDateGroup?.let {
                    OrderInDateGroup(it.order, it.editTimeMillis)
                }
            )
        }
}
