package com.jeluchu.features.search.models

import com.jeluchu.core.models.jikan.anime.AnimeData
import com.jeluchu.core.models.jikan.manga.MangaData
import fordelete.CharacterData
import kotlinx.serialization.Serializable

@Serializable
data class UnifiedSearchResponse(
    val query: String,
    val page: Int,
    val size: Int,
    val animePagination: SearchPagination = SearchPagination(),
    val mangaPagination: SearchPagination = SearchPagination(),
    val charactersPagination: SearchPagination = SearchPagination(),
    val anime: List<SearchResult> = emptyList(),
    val manga: List<SearchResult> = emptyList(),
    val characters: List<SearchResult> = emptyList()
)

@Serializable
data class SearchPagination(
    val totalPages: Int = 0,
    val totalItems: Int = 0
)

@Serializable
data class SearchResult(
    val malId: Int = 0,
    val type: String = "",
    val title: String = "",
    val image: String = "",
    val score: Double? = null,
    val url: String = ""
)

fun AnimeData.toAnimeSearchResult() = SearchResult(
    malId = malId ?: 0,
    type = type.orEmpty(),
    title = titles.orEmpty().firstOrNull { it.type.equals("Default", ignoreCase = true) }?.title
        ?: title
        ?: titleEnglish
        ?: "",
    image = images?.webp?.large ?: images?.jpg?.large ?: images?.jpg?.generic.orEmpty(),
    score = score?.toDouble(),
    url = url.orEmpty()
)

fun MangaData.toMangaSearchResult() = SearchResult(
    malId = malId ?: 0,
    type = type.orEmpty(),
    title = titles.orEmpty().firstOrNull { it.type.equals("Default", ignoreCase = true) }?.title
        ?: title
        ?: titleEnglish
        ?: "",
    image = images?.webp?.large ?: images?.jpg?.large ?: images?.jpg?.generic.orEmpty(),
    score = score,
    url = url.orEmpty()
)

fun CharacterData.toCharacterSearchResult() = SearchResult(
    malId = malId ?: 0,
    type = "character",
    title = name.orEmpty(),
    image = images?.jpg?.generic.orEmpty(),
    score = null,
    url = url.orEmpty()
)
