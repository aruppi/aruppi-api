package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistSearch(
    @SerialName("artists") val artists: List<ArtistData>? = null,
    @SerialName("meta") val meta: ThemePaginationMeta? = null,
    @SerialName("links") val links: ThemePaginationLinks? = null
)

@Serializable
data class ArtistShow(
    @SerialName("artist") val artist: ArtistData? = null
)

@Serializable
data class SongSearch(
    @SerialName("songs") val songs: List<SongData>? = null,
    @SerialName("meta") val meta: ThemePaginationMeta? = null,
    @SerialName("links") val links: ThemePaginationLinks? = null
)

@Serializable
data class AnimeThemeShow(
    @SerialName("anime") val anime: AnimeData? = null
)

@Serializable
data class AnimeThemeSearch(
    @SerialName("animethemes") val animethemes: List<ThemeData>? = null
)

@Serializable
data class ThemePaginationMeta(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("count") val count: Int? = null,
    @SerialName("per_page") val perPage: Int? = null
)

@Serializable
data class ThemePaginationLinks(
    @SerialName("first") val first: String? = null,
    @SerialName("last") val last: String? = null,
    @SerialName("prev") val prev: String? = null,
    @SerialName("next") val next: String? = null
)
