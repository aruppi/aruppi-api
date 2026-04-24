package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistSongTheme(
    val type: String? = null,
    val slug: String? = null,
    val sequence: Int? = null,
    val animeName: String? = null,
    val animeSlug: String? = null,
    val videoLink: String? = null
)
