package com.jeluchu.core.enums

import kotlinx.serialization.Serializable

@Serializable
enum class GalleryProvider(val value: String) {
    ANIME_PICTURES("anime-pictures"),
    DANBOORU("danbooru"),
    SAFEBOORU("safebooru"),
}

fun parseGalleryProvider(provider: String?) = GalleryProvider.entries.firstOrNull {
    it.value.equals(provider.orEmpty(), ignoreCase = true) || it.name.equals(provider.orEmpty(), ignoreCase = true)
} ?: GalleryProvider.ANIME_PICTURES
