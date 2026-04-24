package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistSong(
    val id: Int? = null,
    val title: String? = null,
    val themes: List<ArtistSongTheme>? = null
)
