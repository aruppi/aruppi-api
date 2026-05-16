package com.jeluchu.features.themes.models.artist

import com.jeluchu.core.extensions.toYouTubeWatchUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistData(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val songs: List<SongData>? = null,
    val images: List<ArtistImageData>? = null
)

@Serializable
data class ArtistImageData(
    val link: String? = null,
    val facet: String? = null
)

@Serializable
data class SongData(
    val id: Int? = null,
    val title: String? = null,
    val artists: List<ArtistData>? = null,
    @SerialName("animethemes") val animethemes: List<ThemeData>? = null
)

@Serializable
data class ThemeData(
    val id: Int? = null,
    val type: String? = null,
    val slug: String? = null,
    val sequence: Int? = null,
    val anime: AnimeData? = null,
    val song: SongData? = null,
    @SerialName("animethemeentries") val entries: List<EntryData>? = null
)

@Serializable
data class EntryData(
    val id: Int? = null,
    val episodes: String? = null,
    val nsfw: Boolean? = null,
    val spoiler: Boolean? = null,
    val videos: List<VideoData>? = null
)

@Serializable
data class VideoData(
    val link: String? = null,
    val filename: String? = null,
    val embedUrl: String? = null,
    val resolution: Int? = null,
    val nc: Boolean? = null,
    val subbed: Boolean? = null,
    val lyrics: Boolean? = null,
    val uncen: Boolean? = null,
    val source: String? = null,
    val overlap: String? = null
)

@Serializable
data class AnimeData(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val year: Int? = null,
    val season: String? = null,
    val synopsis: String? = null,
    @SerialName("animethemes") val animethemes: List<ThemeData>? = null,
    val images: List<AnimeImageData>? = null
)

@Serializable
data class AnimeImageData(
    val link: String? = null,
    val facet: String? = null
)
