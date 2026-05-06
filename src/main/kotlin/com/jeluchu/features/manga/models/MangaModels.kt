package com.jeluchu.features.manga.models

import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getDocumentSafe
import com.jeluchu.core.extensions.getDoubleSafe
import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.models.jikan.anime.Demographic
import com.jeluchu.core.models.jikan.anime.Genre
import com.jeluchu.core.models.jikan.anime.Title
import com.jeluchu.core.models.jikan.manga.MangaData
import kotlinx.serialization.Serializable
import org.bson.Document

@Serializable
data class MangaSummary(
    val malId: Int = 0,
    val type: String = "",
    val title: String = "",
    val image: String = "",
    val score: Double = 0.0,
    val status: String = "",
    val volumes: Int = 0,
    val chapters: Int = 0,
    val url: String = ""
)

@Serializable
data class MangaDetail(
    val malId: Int = 0,
    val type: String = "",
    val title: String = "",
    val titles: List<MangaTitle> = emptyList(),
    val image: String = "",
    val score: Double = 0.0,
    val rank: Int = 0,
    val popularity: Int = 0,
    val members: Int = 0,
    val favorites: Int = 0,
    val status: String = "",
    val publishing: Boolean = false,
    val volumes: Int = 0,
    val chapters: Int = 0,
    val synopsis: String = "",
    val background: String = "",
    val published: String = "",
    val authors: List<MangaNamedResource> = emptyList(),
    val serializations: List<MangaNamedResource> = emptyList(),
    val genres: List<MangaNamedResource> = emptyList(),
    val explicitGenres: List<MangaNamedResource> = emptyList(),
    val themes: List<MangaNamedResource> = emptyList(),
    val demographics: List<MangaNamedResource> = emptyList(),
    val url: String = ""
)

@Serializable
data class MangaTitle(
    val type: String = "",
    val title: String = ""
)

@Serializable
data class MangaNamedResource(
    val malId: Int = 0,
    val type: String = "",
    val name: String = "",
    val url: String = ""
)

@Serializable
data class MangaDataResponse(
    val data: MangaData? = null
)

fun MangaData.toMangaSummary() = MangaSummary(
    malId = malId ?: 0,
    type = type.orEmpty(),
    title = preferredTitle(),
    image = images?.webp?.large ?: images?.jpg?.large ?: images?.jpg?.generic.orEmpty(),
    score = score ?: 0.0,
    status = status.orEmpty(),
    volumes = volumes ?: 0,
    chapters = chapters ?: 0,
    url = url.orEmpty()
)

fun MangaData.toMangaDetail() = MangaDetail(
    malId = malId ?: 0,
    type = type.orEmpty(),
    title = preferredTitle(),
    titles = titles.orEmpty().map { it.toMangaTitle() },
    image = images?.webp?.large ?: images?.jpg?.large ?: images?.jpg?.generic.orEmpty(),
    score = score ?: 0.0,
    rank = rank ?: 0,
    popularity = popularity ?: 0,
    members = members ?: 0,
    favorites = favorites ?: 0,
    status = status.orEmpty(),
    publishing = publishing ?: false,
    volumes = volumes ?: 0,
    chapters = chapters ?: 0,
    synopsis = synopsis.orEmpty(),
    background = background.orEmpty(),
    published = published?.string.orEmpty(),
    authors = authors.orEmpty().map { it.toMangaNamedResource() },
    serializations = serializations.orEmpty().map { it.toMangaNamedResource() },
    genres = genres.orEmpty().map { it.toMangaNamedResource() },
    explicitGenres = explicitGenres.orEmpty().map { it.toMangaNamedResource() },
    themes = emptyList(),
    demographics = demographics.orEmpty().map { it.toMangaNamedResource() },
    url = url.orEmpty()
)

private fun MangaData.preferredTitle(): String {
    return titles.orEmpty().firstOrNull { it.type.equals("Default", ignoreCase = true) }?.title
        ?: title
        ?: titleEnglish
        ?: titles.orEmpty().firstOrNull()?.title
        ?: ""
}

private fun Title.toMangaTitle() = MangaTitle(
    type = type.orEmpty(),
    title = title.orEmpty()
)

private fun Demographic.toMangaNamedResource() = MangaNamedResource(
    malId = malId ?: 0,
    type = type.orEmpty(),
    name = name.orEmpty(),
    url = url.orEmpty()
)

private fun Genre.toMangaNamedResource() = MangaNamedResource(
    malId = malId ?: 0,
    type = type.orEmpty(),
    name = name.orEmpty(),
    url = url.orEmpty()
)

fun documentToMangaSummary(doc: Document) = MangaSummary(
    malId = doc.getIntSafe("malId"),
    type = doc.getStringSafe("type"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("image"),
    score = doc.getDoubleSafe("score"),
    status = doc.getStringSafe("status"),
    volumes = doc.getIntSafe("volumes"),
    chapters = doc.getIntSafe("chapters"),
    url = doc.getStringSafe("url")
)

fun documentToMangaDetail(doc: Document) = MangaDetail(
    malId = doc.getIntSafe("malId"),
    type = doc.getStringSafe("type"),
    title = doc.getStringSafe("title"),
    titles = doc.getListSafe<Document>("titles").map { documentToMangaTitle(it) },
    image = doc.getStringSafe("image"),
    score = doc.getDoubleSafe("score"),
    rank = doc.getIntSafe("rank"),
    popularity = doc.getIntSafe("popularity"),
    members = doc.getIntSafe("members"),
    favorites = doc.getIntSafe("favorites"),
    status = doc.getStringSafe("status"),
    publishing = doc.getBooleanSafe("publishing"),
    volumes = doc.getIntSafe("volumes"),
    chapters = doc.getIntSafe("chapters"),
    synopsis = doc.getStringSafe("synopsis"),
    background = doc.getStringSafe("background"),
    published = doc.getStringSafe("published"),
    authors = doc.getListSafe<Document>("authors").map { documentToMangaNamedResource(it) },
    serializations = doc.getListSafe<Document>("serializations").map { documentToMangaNamedResource(it) },
    genres = doc.getListSafe<Document>("genres").map { documentToMangaNamedResource(it) },
    explicitGenres = doc.getListSafe<Document>("explicitGenres").map { documentToMangaNamedResource(it) },
    themes = doc.getListSafe<Document>("themes").map { documentToMangaNamedResource(it) },
    demographics = doc.getListSafe<Document>("demographics").map { documentToMangaNamedResource(it) },
    url = doc.getStringSafe("url")
)

private fun documentToMangaTitle(doc: Document) = MangaTitle(
    type = doc.getStringSafe("type"),
    title = doc.getStringSafe("title")
)

private fun documentToMangaNamedResource(doc: Document) = MangaNamedResource(
    malId = doc.getIntSafe("malId"),
    type = doc.getStringSafe("type"),
    name = doc.getStringSafe("name"),
    url = doc.getStringSafe("url")
)
