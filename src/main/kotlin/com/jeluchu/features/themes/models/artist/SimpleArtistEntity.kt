package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.Serializable

@Serializable
data class SimpleArtistEntity(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val image: String? = null
) {
    companion object {
        fun ArtistData.toSimpleArtistEntity() = SimpleArtistEntity(
            id = id,
            name = name,
            slug = slug,
            image = images?.firstOrNull { it.facet == "Large Cover" }?.link
                ?: images?.firstOrNull()?.link
        )
    }
}