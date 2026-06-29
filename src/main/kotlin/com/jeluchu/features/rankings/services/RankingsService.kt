package com.jeluchu.features.rankings.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.*
import com.jeluchu.core.extensions.isSafeAnimeData
import com.jeluchu.core.extensions.isSafeMangaData
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.parseSfwPreference
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
        val sfw = call.parseSfwPreference() ?: return
        val filter = call.request.queryParameters["filter"]
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
        val type = call.request.queryParameters["type"]

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (type != null && parseAnimeType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeType.message)
        if (filter != null && parseAnimeFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeFilterType.message)

        val cacheType = type ?: DEFAULT_ANIME_TYPE
        val cacheFilter = filter ?: DEFAULT_ANIME_FILTER
        val timerKey = "${Collections.ANIME_RANKING}_${cacheType}_${cacheFilter}_${page}_sfw_$sfw"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )
        val cacheQuery = Filters.and(
            Filters.eq("page", page),
            Filters.eq("type", cacheType),
            Filters.eq("subtype", cacheFilter),
            Filters.eq("sfw", sfw)
        )
        val cachedAnimes = animeRanking.find(cacheQuery).limit(size).toList()

        val offset = (page - 1) * size
        if (needsUpdate || cachedAnimes.isEmpty()) {
            val params = mutableListOf<String>()
            params.add("type=$cacheType")
            params.add("page=$page")
            params.add("filter=$cacheFilter")

            val rawResponse = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?${params.joinToString("&")}",
                AnimeSearch.serializer()
            )
            val totalItems = rawResponse.pagination.itemsPage?.total ?: 0
            val totalPages = rawResponse.pagination.lastPage ?: 0
            val response = rawResponse.data
                .filter { anime -> !sfw || anime.isSafeAnimeData() }
                .map { anime ->
                anime.toAnimeTopEntity(
                    top = "anime",
                    page = page,
                    type = cacheType,
                    subType = cacheFilter
                )
                }

            val documentsToInsert = parseDataToDocuments(response, AnimeTopEntity.serializer()).onEach {
                it.append("sfw", sfw)
                it.append("totalPages", totalPages)
                it.append("totalItems", totalItems)
            }
            if (documentsToInsert.isNotEmpty()) {
                animeRanking.deleteMany(cacheQuery)
                animeRanking.insertMany(documentsToInsert)
                timers.update(timerKey)
            }
            val responseDocuments = documentsToInsert.ifEmpty { cachedAnimes }
            val cachedFirst = responseDocuments.firstOrNull()

            val elements = responseDocuments.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                totalPages = totalPages.takeIf { it > 0 }
                    ?: cachedFirst?.getInteger("totalPages", 0)
                    ?: 0,
                totalItems = totalItems.takeIf { it > 0 }
                    ?: cachedFirst?.getInteger("totalItems", 0)
                    ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val first = cachedAnimes.firstOrNull()

            val elements = cachedAnimes.mapIndexed { index, document ->
                documentToAnimeTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getMangaRanking(call: RoutingCall) {
        val sfw = call.parseSfwPreference() ?: return
        val filter = call.request.queryParameters["filter"]
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
        val type = call.request.queryParameters["type"]

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
        if (type != null && parseMangaType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopMangaType.message)
        if (filter != null && parseMangaFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopMangaFilterType.message)

        val cacheType = type ?: DEFAULT_MANGA_TYPE
        val cacheFilter = filter ?: DEFAULT_MANGA_FILTER
        val timerKey = "${Collections.MANGA_RANKING}_${cacheType}_${cacheFilter}_${page}_sfw_$sfw"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )
        val cacheQuery = Filters.and(
            Filters.eq("page", page),
            Filters.eq("type", cacheType),
            Filters.eq("subtype", cacheFilter),
            Filters.eq("sfw", sfw)
        )
        val cachedMangas = mangaRanking.find(cacheQuery).limit(size).toList()

        val offset = (page - 1) * size
        if (needsUpdate || cachedMangas.isEmpty()) {
            val params = mutableListOf<String>()
            params.add("type=$cacheType")
            params.add("page=$page")
            params.add("filter=$cacheFilter")

            val rawResponse = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_MANGA + "?${params.joinToString("&")}",
                MangaSearch.serializer()
            )
            val totalItems = rawResponse.pagination?.itemsPage?.total ?: 0
            val totalPages = rawResponse.pagination?.lastPage ?: 0
            val response = rawResponse.data
                ?.filter { manga -> !sfw || manga.isSafeMangaData() }
                ?.map { anime ->
                anime.toMangaTopEntity(
                    top = "manga",
                    page = page,
                    type = cacheType,
                    subType = cacheFilter
                )
                }

            val documentsToInsert = parseDataToDocuments(response, MangaTopEntity.serializer()).onEach {
                it.append("sfw", sfw)
                it.append("totalPages", totalPages)
                it.append("totalItems", totalItems)
            }
            if (documentsToInsert.isNotEmpty()) {
                mangaRanking.deleteMany(cacheQuery)
                mangaRanking.insertMany(documentsToInsert)
                timers.update(timerKey)
            }
            val responseDocuments = documentsToInsert.ifEmpty { cachedMangas }
            val cachedFirst = responseDocuments.firstOrNull()

            val elements = responseDocuments.mapIndexed { index, document ->
                documentToMangaTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                totalPages = totalPages.takeIf { it > 0 }
                    ?: cachedFirst?.getInteger("totalPages", 0)
                    ?: 0,
                totalItems = totalItems.takeIf { it > 0 }
                    ?: cachedFirst?.getInteger("totalItems", 0)
                    ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val first = cachedMangas.firstOrNull()

            val elements = cachedMangas.mapIndexed { index, document ->
                documentToMangaTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
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
            val rawResponse = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_PEOPLE + "?page=$page",
                PeopleSearch.serializer()
            )
            val totalItems = rawResponse.pagination?.itemsPage?.total ?: 0
            val totalPages = rawResponse.pagination?.lastPage ?: 0
            val response = rawResponse.data?.map { anime ->
                anime.toPeopleTopEntity(
                    top = "people",
                    page = page
                )
            }

            val documentsToInsert = parseDataToDocuments(response, PeopleTopEntity.serializer()).onEach {
                it.append("totalPages", totalPages)
                it.append("totalItems", totalItems)
            }
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
                totalPages = totalPages,
                totalItems = totalItems,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val peoples = peopleRanking
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()
            val first = peoples.firstOrNull()

            val elements = peoples.mapIndexed { index, document ->
                documentToPeopleTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
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
            val rawResponse = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_CHARACTER + "?page=$page",
                CharacterSearch.serializer()
            )
            val totalItems = rawResponse.pagination?.itemsPage?.total ?: 0
            val totalPages = rawResponse.pagination?.lastPage ?: 0
            val response = rawResponse.data?.map { anime ->
                anime.toCharacterTopEntity(
                    top = "character",
                    page = page
                )
            }

            val documentsToInsert = parseDataToDocuments(response, CharacterTopEntity.serializer()).onEach {
                it.append("totalPages", totalPages)
                it.append("totalItems", totalItems)
            }
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
                totalPages = totalPages,
                totalItems = totalItems,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val characters = characterRanking
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()
            val first = characters.firstOrNull()

            val elements = characters.mapIndexed { index, document ->
                documentToCharacterTopEntity(
                    doc = document,
                    position = offset + index
                )
            }

            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getAnimeTopTenRanking(call: RoutingCall) {
        val sfw = call.parseSfwPreference() ?: return
        val filter = call.request.queryParameters["filter"] ?: "airing"
        val type = call.parameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopAnimeType.message)

        if (parseAnimeType(type) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeType.message)
        if (parseAnimeFilterType(filter) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeFilterType.message)

        val timerKey = "${Collections.ANIME_RANKING}_${Collections.TOP_TEN}_${type}_${filter}_sfw_$sfw"

        val needsUpdate = timers.needsUpdate(
            amount = 7,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            animeRankingTopTen.deleteMany(
                Filters.and(
                    Filters.eq("type", type),
                    Filters.eq("subtype", filter),
                    Filters.eq("sfw", sfw)
                )
            )

            val params = mutableListOf<String>()
            params.add("type=$type")
            params.add("filter=$filter")

            val response = RestClient.request(
                BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?${params.joinToString("&")}",
                AnimeSearch.serializer()
            ).data
                .filter { anime -> !sfw || anime.isSafeAnimeData() }
                .map { anime ->
                anime.toAnimeTopEntity(
                    page = 0,
                    top = "anime",
                    type = type,
                    subType = filter
                )
                }.orEmpty().take(11).distinctBy { it.malId }

            val documentsToInsert = parseDataToDocuments(response, AnimeTopEntity.serializer()).onEach {
                it.append("sfw", sfw)
            }
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
                        Filters.eq("subtype", filter),
                        Filters.eq("sfw", sfw)
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

private const val DEFAULT_ANIME_TYPE = "tv"
private const val DEFAULT_ANIME_FILTER = "bypopularity"
private const val DEFAULT_MANGA_TYPE = "manga"
private const val DEFAULT_MANGA_FILTER = "publishing"
