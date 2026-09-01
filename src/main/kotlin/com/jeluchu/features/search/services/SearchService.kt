package com.jeluchu.features.search.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.extensions.isSafeAnimeData
import com.jeluchu.core.extensions.isSafeMangaData
import com.jeluchu.core.extensions.parseSfwPreference
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.jikan.character.CharacterSearch
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.models.jikan.search.MangaSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.features.search.models.SearchPagination
import com.jeluchu.features.search.models.UnifiedSearchResponse
import com.jeluchu.features.search.models.toAnimeSearchResult
import com.jeluchu.features.search.models.toCharacterSearchResult
import com.jeluchu.features.search.models.toMangaSearchResult
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder

class SearchService {
    private val maxPageSize = 10

    suspend fun search(call: RoutingCall) {
        try {
            val sfw = call.parseSfwPreference() ?: return
            val query = call.request.queryParameters["q"].orEmpty().trim()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 5
            val categories = call.request.queryParameters["type"]
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: setOf("anime", "manga", "characters")

            if (query.isBlank()) {
                return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.MissingQuery.message, code = "MISSING_QUERY")
            }

            if (page < 1 || size < 1 || size > maxPageSize) {
                return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message, code = "INVALID_PAGE_SIZE")
            }

            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            val animeResponse = if ("anime" in categories) {
                RestClient.request(
                    "${BaseUrls.TENRAI}anime?q=$encodedQuery&page=$page&limit=$size&sfw=$sfw",
                    AnimeSearch.serializer()
                )
            } else {
                null
            }

            val mangaResponse = if ("manga" in categories) {
                RestClient.request(
                    "${BaseUrls.TENRAI}manga?q=$encodedQuery&page=$page&limit=$size&sfw=$sfw",
                    MangaSearch.serializer()
                )
            } else {
                null
            }

            val filteredAnime = animeResponse?.data.orEmpty().filter { anime -> !sfw || anime.isSafeAnimeData() }
            val filteredManga = mangaResponse?.data.orEmpty().filter { manga -> !sfw || manga.isSafeMangaData() }

            val characterResponse = if ("characters" in categories || "character" in categories) {
                RestClient.request(
                    "${BaseUrls.TENRAI}characters?q=$encodedQuery&page=$page&limit=$size",
                    CharacterSearch.serializer()
                )
            } else {
                null
            }

            val response = UnifiedSearchResponse(
                query = query,
                page = page,
                size = size,
                animePagination = animeResponse.toSearchPagination(),
                mangaPagination = mangaResponse.toSearchPagination(),
                charactersPagination = characterResponse.toSearchPagination(),
                anime = filteredAnime.map { it.toAnimeSearchResult() },
                manga = filteredManga.map { it.toMangaSearchResult() },
                characters = characterResponse?.data.orEmpty().map { it.toCharacterSearchResult() }
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        } catch (ex: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, ErrorMessages.InternalServerError.message)
        }
    }

    private fun AnimeSearch?.toSearchPagination() = SearchPagination(
        totalPages = this?.pagination?.lastPage ?: 0,
        totalItems = this?.pagination?.itemsPage?.total ?: 0
    )

    private fun MangaSearch?.toSearchPagination() = SearchPagination(
        totalPages = this?.pagination?.lastPage ?: 0,
        totalItems = this?.pagination?.itemsPage?.total ?: 0
    )

    private fun CharacterSearch?.toSearchPagination() = SearchPagination(
        totalPages = this?.pagination?.lastPage ?: 0,
        totalItems = this?.pagination?.itemsPage?.total ?: 0
    )
}
