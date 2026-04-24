package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistSearch(
    @SerialName("artists") val artists: List<ArtistData>? = null
)

@Serializable
data class ArtistShow(
    @SerialName("artist") val artist: ArtistData? = null
)

@Serializable
data class SongSearch(
    @SerialName("songs") val songs: List<SongData>? = null
)

@Serializable
data class AnimeThemeShow(
    @SerialName("anime") val anime: AnimeData? = null
)

@Serializable
data class AnimeThemeSearch(
    @SerialName("animethemes") val animethemes: List<ThemeData>? = null
)
