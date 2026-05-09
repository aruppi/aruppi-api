package com.jeluchu.features.rankings.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.*
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.ErrorResponse
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.models.jikan.anime.AnimeData.Companion.toAnimeTopEntity
import com.jeluchu.core.models.jikan.character.CharacterSearch
import com.jeluchu.core.models.jikan.manga.MangaData.Companion.toMangaTopEntity
import com.jeluchu.core.models.jikan.people.PeopleData.Companion.toPeopleTopEntity
import com.jeluchu.core.models.jikan.people.PeopleSearch
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.models.jikan.search.MangaSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.Endpoints
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimeTopEntity
import com.jeluchu.features.anime.mappers.documentToCharacterTopEntity
import com.jeluchu.features.anime.mappers.documentToMangaTopEntity
import com.jeluchu.features.anime.mappers.documentToPeopleTopEntity
import com.jeluchu.features.rankings.models.AnimeTopEntity
import com.jeluchu.features.rankings.models.CharacterTopEntity
import com.jeluchu.features.rankings.models.MangaTopEntity
import com.jeluchu.features.rankings.models.PeopleTopEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import fordelete.CharacterData.Companion.toCharacterTopEntity
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.times

class RankingsService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val animeRanking = database.getCollection(Collections.ANIME_RANKING)
    private val mangaRanking = database.getCollection(Collections.MANGA_RANKING)
    private val peopleRanking = database.getCollection(Collections.PEOPLE_RANKING)
    private val characterRanking = database.getCollection(Collections.CHARACTER_RANKING)
    private val animeRankingTopTen = database.getCollection(Collections.ANIME_RANKING_TOP_TEN)

    suspend fun getAnimeRanking(call: RoutingCall) {
        val filter = call.request.queryParameters["filter"] ?: "airing"
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
        val type = call.parameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopAnimeType.message)

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        if (parseAnimeType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeType.message)
        if (parseAnimeFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeFilterType.message)

        val timerKey = "${Collections.ANIME_RANKING}_${type}_${filter}_${page}"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        val offset = (page - 1) * size
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (needsUpdate) {
            animeRanking.deleteMany(
                Filters.and(
                    Filters.eq("page", page),
                    Filters.eq("type", type),
                    Filters.eq("subtype", filter)
                )
            )

            val params = mutableListOf<String>()
            params.add("type=$type")
            params.add("page=$page")
            params.add("filter=$filter")

            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?${params.joinToString("&")}",
                AnimeSearch.serializer()
            ).data.map { anime ->
                anime.toAnimeTopEntity(
                    top = "anime",
                    page = page,
                    type = type,
                    subType = filter
                )
            }

            val documentsToInsert = parseDataToDocuments(response, AnimeTopEntity.serializer())
            if (documentsToInsert.isNotEmpty()) animeRanking.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val animes = animeRanking
                .find(
                    Filters.and(
                        Filters.eq("page", page),
                        Filters.eq("type", type),
                        Filters.eq("subtype", filter)
                    )
                )
                .limit(size)
                .toList()

            val elements = animes.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getMangaRanking(call: RoutingCall) {
        val filter = call.request.queryParameters["filter"] ?: "publishing"
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
        val type = call.parameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopMangaType.message)

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        if (parseMangaType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopMangaType.message)
        if (parseMangaFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopMangaFilterType.message)

        val timerKey = "${Collections.MANGA_RANKING}_${type}_${filter}_${page}"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        val offset = (page - 1) * size
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (needsUpdate) {
            mangaRanking.deleteMany(
                Filters.and(
                    Filters.eq("page", page),
                    Filters.eq("type", type),
                    Filters.eq("subtype", filter)
                )
            )

            val params = mutableListOf<String>()
            params.add("type=$type")
            params.add("page=$page")
            params.add("filter=$filter")

            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_MANGA + "?${params.joinToString("&")}",
                MangaSearch.serializer()
            ).data?.map { anime ->
                anime.toMangaTopEntity(
                    top = "manga",
                    page = page,
                    type = type,
                    subType = filter
                )
            }

            val documentsToInsert = parseDataToDocuments(response, MangaTopEntity.serializer())
            if (documentsToInsert.isNotEmpty()) mangaRanking.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.mapIndexed { index, document ->
                documentToMangaTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val mangas = mangaRanking
                .find(
                    Filters.and(
                        Filters.eq("page", page),
                        Filters.eq("type", type),
                        Filters.eq("subtype", filter)
                    )
                )
                .limit(size)
                .toList()

            val elements = mangas.mapIndexed { index, document ->
                documentToMangaTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getPeopleRanking(call: RoutingCall) {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        val timerKey = "${Collections.PEOPLE_RANKING}_${page}"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        val offset = (page - 1) * size
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (needsUpdate) {
            peopleRanking.deleteMany(Filters.and(Filters.eq("page", page)))
            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_PEOPLE + "?page=$page",
                PeopleSearch.serializer()
            ).data?.map { anime ->
                anime.toPeopleTopEntity(
                    top = "people",
                    page = page
                )
            }

            val documentsToInsert = parseDataToDocuments(response, PeopleTopEntity.serializer())
            if (documentsToInsert.isNotEmpty()) peopleRanking.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.mapIndexed { index, document ->
                documentToPeopleTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val peoples = peopleRanking
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()

            val elements = peoples.mapIndexed { index, document ->
                documentToPeopleTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getCharacterRanking(call: RoutingCall) {
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        val timerKey = "${Collections.CHARACTER_RANKING}_${page}"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        val offset = (page - 1) * size
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (needsUpdate) {
            characterRanking.deleteMany(Filters.and(Filters.eq("page", page)))
            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_CHARACTER + "?page=$page",
                CharacterSearch.serializer()
            ).data?.map { anime ->
                anime.toCharacterTopEntity(
                    top = "character",
                    page = page
                )
            }

            val documentsToInsert = parseDataToDocuments(response, CharacterTopEntity.serializer())
            if (documentsToInsert.isNotEmpty()) characterRanking.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.mapIndexed { index, document ->
                documentToCharacterTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val characters = characterRanking
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()

            val elements = characters.mapIndexed { index, document ->
                documentToCharacterTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getAnimeTopTenRanking(call: RoutingCall) {
        val filter = call.request.queryParameters["filter"] ?: "airing"
        val type = call.parameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopAnimeType.message)

        if (parseAnimeType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeType.message)
        if (parseAnimeFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeFilterType.message)

        val timerKey = "${Collections.ANIME_RANKING}_${Collections.TOP_TEN}_${type}_${filter}"

        val needsUpdate = timers.needsUpdate(
            amount = 7,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            animeRankingTopTen.deleteMany(
                Filters.and(
                    Filters.eq("type", type),
                    Filters.eq("subtype", filter)
                )
            )

            val params = mutableListOf<String>()
            params.add("type=$type")
            params.add("filter=$filter")

            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?${params.joinToString("&")}",
                AnimeSearch.serializer()
            ).data?.map { anime ->
                anime.toAnimeTopEntity(
                    page = 0,
                    top = "anime",
                    type = type,
                    subType = filter
                )
            }.orEmpty().take(11).distinctBy { it.malId }

            val documentsToInsert = parseDataToDocuments(response, AnimeTopEntity.serializer())
            if (documentsToInsert.isNotEmpty()) animeRankingTopTen.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = index
                )
            }

            call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
        } else {
            val animes = animeRankingTopTen
                .find(
                    Filters.and(
                        Filters.eq("type", type),
                        Filters.eq("subtype", filter)
                    )
                )
                .toList()

            val elements = animes.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = index
                )
            }

            call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
        }
    }
}
