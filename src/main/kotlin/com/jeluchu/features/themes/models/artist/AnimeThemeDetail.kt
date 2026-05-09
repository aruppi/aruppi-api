package com.jeluchu.features.themes.models.artist

import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.extensions.toYouTubeWatchUrl
import kotlinx.serialization.Serializable
import org.bson.Document

@Serializable
data class AnimeThemeDetail(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val year: Int? = null,
    val season: String? = null,
    val synopsis: String? = null,
    val image: String? = null,
    val themes: List<AnimeThemeItem>? = null
) {
    @Serializable
    data class AnimeThemeItem(
        val id: Int? = null,
        val type: String? = null,
        val slug: String? = null,
        val sequence: Int? = null,
        val song: AnimeThemeSong? = null,
        val entries: List<AnimeThemeEntry>? = null
    )

    @Serializable
    data class AnimeThemeSong(
        val id: Int? = null,
        val title: String? = null
    )

    @Serializable
    data class AnimeThemeEntry(
        val id: Int? = null,
        val episodes: String? = null,
        val nsfw: Boolean? = null,
        val spoiler: Boolean? = null,
        val videos: List<AnimeThemeVideo>? = null
    )

    @Serializable
    data class AnimeThemeVideo(
        val link: String? = null,
        val resolution: Int? = null,
        val nc: Boolean? = null,
        val subbed: Boolean? = null,
        val lyrics: Boolean? = null,
        val source: String? = null,
        val overlap: String? = null
    )

    companion object {
        fun AnimeData.toAnimeThemeDetail() = AnimeThemeDetail(
            id = id,
            name = name,
            slug = slug,
            year = year,
            season = season,
            synopsis = synopsis,
            image = images?.firstOrNull { it.facet == "Large Cover" }?.link
                ?: images?.firstOrNull()?.link,
            themes = animethemes?.map { theme ->
                AnimeThemeItem(
                    id = theme.id,
                    type = theme.type,
                    slug = theme.slug,
                    sequence = theme.sequence,
                    song = theme.song?.let { AnimeThemeSong(id = it.id, title = it.title) },
                    entries = theme.entries?.map { entry ->
                        AnimeThemeEntry(
                            id = entry.id,
                            episodes = entry.episodes,
                            nsfw = entry.nsfw,
                            spoiler = entry.spoiler,
                            videos = entry.videos?.map { video ->
                                AnimeThemeVideo(
                                    link = video.link.toYouTubeWatchUrl(),
                                    resolution = video.resolution,
                                    nc = video.nc,
                                    subbed = video.subbed,
                                    lyrics = video.lyrics,
                                    source = video.source,
                                    overlap = video.overlap
                                )
                            }
                        )
                    }
                )
            }
        )
    }
}

// ── MongoDB document mappers ───────────────────────────────────────────────────

fun documentToAnimeThemeDetail(doc: Document) = AnimeThemeDetail(
    id = doc.getIntSafe("id"),
    name = doc.getStringSafe("name"),
    slug = doc.getStringSafe("slug"),
    year = doc.getIntSafe("year"),
    season = doc.getStringSafe("season"),
    synopsis = doc.getStringSafe("synopsis"),
    image = doc.getStringSafe("image"),
    themes = doc.getListSafe<Document>("themes").map { documentToAnimeThemeItem(it) }
)

fun documentToAnimeThemeItem(doc: Document) = AnimeThemeDetail.AnimeThemeItem(
    id = doc.getIntSafe("id"),
    type = doc.getStringSafe("type"),
    slug = doc.getStringSafe("slug"),
    sequence = doc.getIntSafe("sequence"),
    song = doc.get("song", Document::class.java)?.let { documentToAnimeThemeSong(it) },
    entries = doc.getListSafe<Document>("entries").map { documentToAnimeThemeEntry(it) }
)

fun documentToAnimeThemeSong(doc: Document) = AnimeThemeDetail.AnimeThemeSong(
    id = doc.getIntSafe("id"),
    title = doc.getStringSafe("title")
)

fun documentToAnimeThemeEntry(doc: Document) = AnimeThemeDetail.AnimeThemeEntry(
    id = doc.getIntSafe("id"),
    episodes = doc.getStringSafe("episodes"),
    nsfw = doc.getBooleanSafe("nsfw"),
    spoiler = doc.getBooleanSafe("spoiler"),
    videos = doc.getListSafe<Document>("videos").map { documentToAnimeThemeVideo(it) }
)

fun documentToAnimeThemeVideo(doc: Document) = AnimeThemeDetail.AnimeThemeVideo(
    link = doc.getStringSafe("link"),
    resolution = doc.getIntSafe("resolution"),
    nc = doc.getBooleanSafe("nc"),
    subbed = doc.getBooleanSafe("subbed"),
    lyrics = doc.getBooleanSafe("lyrics"),
    source = doc.getStringSafe("source"),
    overlap = doc.getStringSafe("overlap")
)
