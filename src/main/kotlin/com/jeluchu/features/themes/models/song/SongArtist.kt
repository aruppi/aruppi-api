package com.jeluchu.features.themes.models.song

import kotlinx.serialization.Serializable

@Serializable
data class SongArtist(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null
)
