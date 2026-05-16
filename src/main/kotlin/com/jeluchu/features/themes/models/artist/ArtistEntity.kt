package com.jeluchu.features.themes.models.artist

import kotlinx.serialization.Serializable

@Serializable
data class ArtistEntity(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val image: String? = null,
    val songs: List<ArtistSong>? = null
) {
    companion object {
        fun ArtistData.toArtistEntity() = ArtistEntity(
            id = id,
            name = name,
            slug = slug,
            image = images?.firstOrNull { it.facet == "Large Cover" }?.link
                ?: images?.firstOrNull()?.link,
            songs = songs?.map { song ->
                ArtistSong(
                    id = song.id,
                    title = song.title,
                    themes = song.animethemes?.map { theme ->
                        val allVideos = theme.entries.orEmpty().flatMap { it.videos.orEmpty() }
                        val bestVideo = allVideos.bestVideoDataForEntryOrNull()

                        ArtistSongTheme(
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
        )
    }
}
