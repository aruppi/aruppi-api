package com.jeluchu.features.themes.models.artist

import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getDocumentSafe
import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
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
        // From AnimeThemeEntry
        val entryId: Int? = null,
        val episodes: String? = null,
        val nsfw: Boolean? = null,
        val spoiler: Boolean? = null,
        val link: String? = null,
        val filename: String? = null,
        val embedUrl: String? = null,
        val resolution: Int? = null,
        val nc: Boolean? = null,
        val subbed: Boolean? = null,
        val lyrics: Boolean? = null,
        val source: String? = null,
        val overlap: String? = null,
        val video: AnimeThemeVideo? = null
    )

    @Serializable
    data class AnimeThemeSong(
        val id: Int? = null,
        val title: String? = null
    )

    @Serializable
    data class AnimeThemeVideo(
        val link: String? = null,
        val filename: String? = null,
        val embedUrl: String? = null,
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
            themes = animethemes?.flatMap { theme ->
                theme.entries.orEmpty().map { entry ->
                    val bestVideo = entry.videos.orEmpty().bestVideoDataForEntryOrNull()
                    AnimeThemeItem(
                        id = theme.id,
                        type = theme.type,
                        slug = theme.slug,
                        sequence = theme.sequence,
                        song = theme.song?.let { AnimeThemeSong(id = it.id, title = it.title) },
                        entryId = entry.id,
                        episodes = entry.episodes,
                        nsfw = entry.nsfw,
                        spoiler = entry.spoiler,
                        link = bestVideo?.link,
                        filename = bestVideo?.filename,
                        embedUrl = bestVideo?.embedUrl,
                        resolution = bestVideo?.resolution,
                        nc = bestVideo?.nc,
                        subbed = bestVideo?.subbed,
                        lyrics = bestVideo?.lyrics,
                        source = bestVideo?.source,
                        overlap = bestVideo?.overlap,
                        video = bestVideo?.let {
                            AnimeThemeVideo(
                                link = it.link,
                                filename = it.filename,
                                embedUrl = it.embedUrl,
                                resolution = it.resolution,
                                nc = it.nc,
                                subbed = it.subbed,
                                lyrics = it.lyrics,
                                source = it.source,
                                overlap = it.overlap
                            )
                        }
                    )
                }
            }
        )
    }
}

@Serializable
data class AnimeThemeDetailResponse(
    val id: Int? = null,
    val name: String? = null,
    val slug: String? = null,
    val year: Int? = null,
    val season: String? = null,
    val synopsis: String? = null,
    val image: String? = null,
    val themes: List<AnimeThemeItemResponse>? = null
)

@Serializable
data class AnimeThemeItemResponse(
    val id: Int? = null,
    val type: String? = null,
    val slug: String? = null,
    val sequence: Int? = null,
    val title: String? = null,
    val video: AnimeThemeVideoResponse? = null
)

@Serializable
data class AnimeThemeVideoResponse(
    val id: Int? = null,
    val episodes: String? = null,
    val nsfw: Boolean? = null,
    val spoiler: Boolean? = null,
    val link: String? = null,
    val resolution: Int? = null,
    val nc: Boolean? = null,
    val subbed: Boolean? = null,
    val lyrics: Boolean? = null,
    val source: String? = null,
    val overlap: String? = null
)

// Mapper from internal model to response model - picks best video per theme (used in cache retrieval)
@Suppress("unused")
fun AnimeThemeDetail.toResponse(): AnimeThemeDetailResponse {
    return AnimeThemeDetailResponse(
        id = id,
        name = name,
        slug = slug,
        year = year,
        season = season,
        synopsis = synopsis,
        image = image,
        themes = themes?.map { theme ->
            AnimeThemeItemResponse(
                id = theme.id,
                type = theme.type,
                slug = theme.slug,
                sequence = theme.sequence,
                title = theme.song?.title,
                video = theme.video?.let { video ->
                    AnimeThemeVideoResponse(
                        id = theme.entryId,
                        episodes = theme.episodes,
                        nsfw = theme.nsfw,
                        spoiler = theme.spoiler,
                        link = video.resolveLink(),
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

// ── MongoDB document mappers ───────────────────────────────────────────────────

// Helper to convert Maps to Documents (BSON parsing sometimes converts objects to Maps)
private fun Any?.asDocument(): Document? {
    return when (this) {
        is Document -> this
        is Map<*, *> -> @Suppress("UNCHECKED_CAST") Document(this as Map<String, Any>)
        else -> null
    }
}

private fun List<*>.asDocuments(): List<Document> {
    return this.mapNotNull { item ->
        when (item) {
            is Document -> item
            is Map<*, *> -> try {
                @Suppress("UNCHECKED_CAST")
                Document(item as Map<String, Any>)
            } catch (_: Exception) {
                null
            }
            else -> null
        }
    }
}

fun documentToAnimeThemeDetail(doc: Document): AnimeThemeDetail {
    println("DEBUG documentToAnimeThemeDetail START")
    println("DEBUG: doc keys = ${doc.keys}")

    val themesRaw = doc["themes"]
    println("DEBUG: themes raw = $themesRaw (type: ${themesRaw?.javaClass?.simpleName})")

    // Convert themes to Documents if they are Maps
    val themesAsDocuments = if (themesRaw is List<*>) {
        println("DEBUG: themes is List, size = ${themesRaw.size}")
        themesRaw.firstOrNull()?.let { println("DEBUG: first element type = ${it.javaClass.simpleName}") }
        themesRaw.asDocuments()
    } else {
        emptyList()
    }

    return  AnimeThemeDetail(
        id = doc.getIntSafe(key = "id"),
        year = doc.getIntSafe(key = "year"),
        name = doc.getStringSafe(key = "name"),
        slug = doc.getStringSafe(key = "slug"),
        image = doc.getStringSafe(key = "image"),
        season = doc.getStringSafe(key = "season"),
        synopsis = doc.getStringSafe(key = "synopsis"),
        themes = themesAsDocuments.flatMap { themeDoc -> documentToAnimeThemeItem(themeDoc) }
    )
}

fun documentToAnimeThemeItem(doc: Document): List<AnimeThemeDetail.AnimeThemeItem> {
    val entries = doc.getListSafe<Document>(key = "entries")

    return if (entries.isNotEmpty()) {
        entries.map { entryDoc ->
            AnimeThemeDetail.AnimeThemeItem(
                id = doc.getIntSafe(key = "id"),
                type = doc.getStringSafe(key = "type"),
                slug = doc.getStringSafe(key = "slug"),
                nc = entryDoc.getBooleanSafe(key = "nc"),
                entryId = entryDoc.getIntSafe(key = "id"),
                sequence = doc.getIntSafe(key = "sequence"),
                link = entryDoc.getStringSafe(key = "link"),
                nsfw = entryDoc.getBooleanSafe(key = "nsfw"),
                source = entryDoc.getStringSafe(key = "source"),
                subbed = entryDoc.getBooleanSafe(key = "subbed"),
                lyrics = entryDoc.getBooleanSafe(key = "lyrics"),
                spoiler = entryDoc.getBooleanSafe(key = "spoiler"),
                episodes = entryDoc.getStringSafe(key = "episodes"),
                filename = entryDoc.getStringSafe(key = "filename"),
                embedUrl = entryDoc.getStringSafe(key = "embedUrl"),
                resolution = entryDoc.getIntSafe(key = "resolution"),
                song = doc.getDocumentSafe(key = "song")?.let { documentToAnimeThemeSong(it) }
            )
        }
    } else {
        listOf(
            AnimeThemeDetail.AnimeThemeItem(
                id = doc.getIntSafe(key = "id"),
                link = doc.getStringSafe(key = "link"),
                type = doc.getStringSafe(key = "type"),
                slug = doc.getStringSafe(key = "slug"),
                nsfw = doc.getBooleanSafe(key = "nsfw"),
                entryId = doc.getIntSafe(key = "entryId"),
                source = doc.getStringSafe(key = "source"),
                sequence = doc.getIntSafe(key = "sequence"),
                subbed = doc.getBooleanSafe(key = "subbed"),
                lyrics = doc.getBooleanSafe(key = "lyrics"),
                spoiler = doc.getBooleanSafe(key = "spoiler"),
                episodes = doc.getStringSafe(key = "episodes"),
                filename = doc.getStringSafe(key = "filename"),
                resolution = doc.getIntSafe(key = "resolution"),
                song = doc.getDocumentSafe(key = "song")?.let { documentToAnimeThemeSong(it) }
            )
        )
    }
}

fun documentToAnimeThemeSong(doc: Document) = AnimeThemeDetail.AnimeThemeSong(
    id = doc.getIntSafe(key = "id"),
    title = doc.getStringSafe(key = "title")
)

// Removed documentToAnimeThemeEntry - now handled in documentToAnimeThemeItem

fun documentToAnimeThemeVideo(doc: Document) = AnimeThemeDetail.AnimeThemeVideo(
    // Keep link as null when empty or missing to preserve original semantics from the API
    link = (doc.get("link") as? String)?.takeIf { it.isNotBlank() },
    filename = doc.getStringSafe("filename").ifBlank { null },
    embedUrl = doc.getStringSafe("embedUrl").ifBlank { null },
    resolution = doc.getIntSafe("resolution"),
    nc = doc.getBooleanSafe("nc"),
    subbed = doc.getBooleanSafe("subbed"),
    lyrics = doc.getBooleanSafe("lyrics"),
    source = doc.getStringSafe("source"),
    overlap = doc.getStringSafe("overlap")
)

private val preferredAnimeThemeResolutions = listOf(1080, 720, 480, 360, 240, 144)

fun List<VideoData>.bestVideoDataForEntryOrNull(): VideoData? {
    if (isEmpty()) return null

    preferredAnimeThemeResolutions.forEach { resolution ->
        firstOrNull { it.resolution == resolution && !it.link.isNullOrBlank() }?.let { return it }
    }

    preferredAnimeThemeResolutions.forEach { resolution ->
        firstOrNull { it.resolution == resolution }?.let { return it }
    }

    firstOrNull { !it.link.isNullOrBlank() }?.let { return it }

    return maxByOrNull { it.resolution ?: Int.MIN_VALUE } ?: firstOrNull()
}

@Suppress("unused")
fun List<AnimeThemeDetail.AnimeThemeVideo>.bestVideoForEntryOrNull(): AnimeThemeDetail.AnimeThemeVideo? {
    if (isEmpty()) return null

    preferredAnimeThemeResolutions.forEach { resolution ->
        firstOrNull { it.resolution == resolution && !it.resolveLink().isNullOrBlank() }?.let { return it }
    }

    preferredAnimeThemeResolutions.forEach { resolution ->
        firstOrNull { it.resolution == resolution }?.let { return it }
    }

    firstOrNull { !it.resolveLink().isNullOrBlank() }?.let { return it }

    return maxByOrNull { it.resolution ?: Int.MIN_VALUE } ?: firstOrNull()
}

fun AnimeThemeDetail.AnimeThemeVideo.resolveLink(): String? {
    return link?.takeIf { it.isNotBlank() }
        ?: embedUrl?.takeIf { it.isNotBlank() }
        ?: filename?.takeIf { it.isNotBlank() }
}
