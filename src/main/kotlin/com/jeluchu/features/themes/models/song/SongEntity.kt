package com.jeluchu.features.themes.models.song

import com.jeluchu.features.themes.models.artist.SongData
import com.jeluchu.features.themes.models.artist.bestVideoDataForEntryOrNull
import kotlinx.serialization.Serializable

@Serializable
data class SongEntity(
    val id: Int? = null,
    val title: String? = null,
    val artists: List<SongArtist>? = null,
    val themes: List<SongTheme>? = null
) {
    companion object {
        fun SongData.toSongEntity() = SongEntity(
            id = id,
            title = title,
            artists = artists?.map { artist ->
                SongArtist(id = artist.id, name = artist.name, slug = artist.slug)
            },
            themes = animethemes?.map { theme ->
                val allVideos = theme.entries.orEmpty().flatMap { it.videos.orEmpty() }
                val bestVideo = allVideos.bestVideoDataForEntryOrNull()

                SongTheme(
                    type = theme.type,
                    slug = theme.slug,
                    sequence = theme.sequence,
                    animeName = theme.anime?.name,
                    animeSlug = theme.anime?.slug,
                    videoLink = bestVideo?.link?.takeIf { it.isNotBlank() }
                )
            }
        )
    }
}

