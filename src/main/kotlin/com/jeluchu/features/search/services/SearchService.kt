package com.jeluchu.features.search.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.jikan.character.CharacterSearch
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.models.jikan.search.MangaSearch
import com.jeluchu.core.utils.BaseUrls
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

            val anime = if ("anime" in categories) {
                RestClient.request(
                    "${BaseUrls.JIKAN}anime?q=$encodedQuery&page=$page&limit=$size&sfw=true",
                    AnimeSearch.serializer()
                ).data.orEmpty().map { it.toAnimeSearchResult() }
            } else {
                emptyList()
            }

            val manga = if ("manga" in categories) {
                RestClient.request(
                    "${BaseUrls.JIKAN}manga?q=$encodedQuery&page=$page&limit=$size&sfw=true",
                    MangaSearch.serializer()
                ).data.orEmpty().map { it.toMangaSearchResult() }
            } else {
                emptyList()
            }

            val characters = if ("characters" in categories || "character" in categories) {
                RestClient.request(
                    "${BaseUrls.JIKAN}characters?q=$encodedQuery&page=$page&limit=$size",
                    CharacterSearch.serializer()
                ).data.orEmpty().map { it.toCharacterSearchResult() }
            } else {
                emptyList()
            }

            val response = UnifiedSearchResponse(
                query = query,
                page = page,
                size = size,
                anime = anime,
                manga = manga,
                characters = characters
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        } catch (ex: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, ErrorMessages.InternalServerError.message)
        }
    }
}
