package com.jeluchu.features.manga.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseMangaStatusType
import com.jeluchu.core.enums.parseMangaType
import com.jeluchu.core.extensions.isSafeMangaData
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.parseSfwPreference
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.models.jikan.search.MangaSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.manga.models.MangaDataResponse
import com.jeluchu.features.manga.models.MangaDetail
import com.jeluchu.features.manga.models.MangaSummary
import com.jeluchu.features.manga.models.documentToMangaDetail
import com.jeluchu.features.manga.models.documentToMangaSummary
import com.jeluchu.features.manga.models.toMangaDetail
import com.jeluchu.features.manga.models.toMangaSummary
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.conversions.Bson
import java.net.URLEncoder

class MangaService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val directoryCollection = database.getCollection(Collections.MANGA_DIRECTORY)
    private val detailCollection = database.getCollection(Collections.MANGA_DETAIL)
    private val maxPageSize = 25

    suspend fun getManga(call: RoutingCall) = call.respondMangaPage()

    suspend fun getDirectory(call: RoutingCall) = call.respondMangaPage()

    suspend fun getMangaByMalId(call: RoutingCall) {
        try {
            val sfw = call.parseSfwPreference() ?: return
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return call.respondError(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorMessages.InvalidMalId.message,
                    code = "INVALID_MAL_ID"
                )

            val timerKey = "${Collections.MANGA_DETAIL}_$id"
            val needsUpdate = timers.needsUpdate(key = timerKey, amount = 30, unit = TimeUnit.DAY)

            val manga = if (needsUpdate) {
                val detailData = RestClient.request(
                    "${BaseUrls.TENRAI}manga/$id/full",
                    MangaDataResponse.serializer()
                ).data ?: return call.respondError(
                    status = HttpStatusCode.NotFound,
                    message = ErrorMessages.MangaNotFound.message,
                    code = "MANGA_NOT_FOUND"
                )

                if (sfw && !detailData.isSafeMangaData()) {
                    return call.respondError(
                        status = HttpStatusCode.NotFound,
                        message = ErrorMessages.MangaNotFound.message,
                        code = "MANGA_NOT_FOUND"
                    )
                }

                val detail = detailData.toMangaDetail()

                detailCollection.deleteMany(Filters.eq("malId", id))
                val documents = parseDataToDocuments(listOf(detail), MangaDetail.serializer())
                if (documents.isNotEmpty()) detailCollection.insertMany(documents)
                timers.update(timerKey)
                detail
            } else {
                detailCollection.find(Filters.eq("malId", id)).firstOrNull()?.let { documentToMangaDetail(it) }?.takeIf { detail ->
                    !sfw || detail.isSafe()
                }
                    ?: RestClient.request(
                        "${BaseUrls.TENRAI}manga/$id/full",
                        MangaDataResponse.serializer()
                    ).data?.takeIf { mangaData -> !sfw || mangaData.isSafeMangaData() }?.toMangaDetail()
                    ?: return call.respondError(
                        status = HttpStatusCode.NotFound,
                        message = ErrorMessages.MangaNotFound.message,
                        code = "MANGA_NOT_FOUND"
                    )
            }

            call.respond(HttpStatusCode.OK, Json.encodeToString(manga))
        } catch (ex: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.MangaNotFound.message, code = "MANGA_NOT_FOUND")
        }
    }

    suspend fun getRandomManga(call: RoutingCall) {
        try {
            val sfw = call.parseSfwPreference() ?: return
            val manga = RestClient.request(
                "${BaseUrls.TENRAI}random/manga?sfw=$sfw",
                MangaDataResponse.serializer()
            ).data?.takeIf { !sfw || it.isSafeMangaData() }

            val safeManga = manga ?: return call.respondError(
                    status = HttpStatusCode.NotFound,
                    message = ErrorMessages.MangaNotFound.message,
                    code = "MANGA_NOT_FOUND"
                )

            call.response.headers.append("Cache-Control", "no-store")
            call.respond(HttpStatusCode.OK, Json.encodeToString(safeManga.toMangaDetail()))
        } catch (ex: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.MangaNotFound.message, code = "MANGA_NOT_FOUND")
        }
    }

    private suspend fun RoutingCall.respondMangaPage() {
        try {
            val type = request.queryParameters["type"].orEmpty()
            val status = request.queryParameters["status"].orEmpty()
            val sfw = parseSfwPreference() ?: return
            val query = request.queryParameters["q"].orEmpty()
            val page = request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = request.queryParameters["size"]?.toIntOrNull() ?: 25

            if (page < 1 || size < 1 || size > maxPageSize) {
                return respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message, code = "INVALID_PAGE_SIZE")
            }

            if (type.isNotBlank() && parseMangaType(type) == null) {
                return respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidMangaType.message, code = "INVALID_MANGA_TYPE")
            }

            if (status.isNotBlank() && parseMangaStatusType(status) == null) {
                return respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidMangaStatusType.message, code = "INVALID_MANGA_STATUS")
            }

            val cacheKey = "${Collections.MANGA_DIRECTORY}_${query}_${type}_${status}_${sfw}_${page}_${size}"
            val cacheFilter = mangaPageFilter(query = query, type = type, status = status, sfw = sfw, page = page, size = size)
            val needsUpdate = timers.needsUpdate(key = cacheKey, amount = 7, unit = TimeUnit.DAY)

            if (!needsUpdate) {
                val cached = directoryCollection.find(cacheFilter).limit(size).toList()
                if (cached.isNotEmpty()) {
                    val first = cached.first()
                    val response = PaginationResponse(
                        page = page,
                        size = size,
                        totalPages = first.getInteger("totalPages", 0),
                        totalItems = first.getInteger("totalItems", 0),
                        data = cached.map { documentToMangaSummary(it) }
                    )

                    return respond(HttpStatusCode.OK, Json.encodeToString(PaginationResponse.serializer(MangaSummary.serializer()), response))
                }
            }

            val params = mutableListOf("page=$page", "limit=$size", "sfw=$sfw")
            if (type.isNotBlank()) params.add("type=${type.lowercase()}")
            if (status.isNotBlank()) params.add("status=${status.lowercase()}")
            if (query.isNotBlank()) params.add("q=${URLEncoder.encode(query, "UTF-8")}")

            val response = RestClient.request(
                "${BaseUrls.TENRAI}manga?${params.joinToString("&")}",
                MangaSearch.serializer()
            )

            val totalItems = response.pagination?.itemsPage?.total ?: 0
            val totalPages = response.pagination?.lastPage ?: 0
            val elements = response.data.orEmpty()
                .filter { manga -> !sfw || manga.isSafeMangaData() }
                .map { it.toMangaSummary() }
            val documents = parseDataToDocuments(elements, MangaSummary.serializer()).onEach {
                it.append("cacheQuery", query)
                it.append("cacheType", type)
                it.append("cacheStatus", status)
                it.append("cacheSfw", sfw)
                it.append("cachePage", page)
                it.append("cacheSize", size)
                it.append("totalPages", totalPages)
                it.append("totalItems", totalItems)
            }

            directoryCollection.deleteMany(cacheFilter)
            if (documents.isNotEmpty()) directoryCollection.insertMany(documents)
            timers.update(cacheKey)

            val pagination = PaginationResponse(
                page = page,
                size = size,
                totalPages = totalPages,
                totalItems = totalItems,
                data = elements
            )

            respond(HttpStatusCode.OK, Json.encodeToString(PaginationResponse.serializer(MangaSummary.serializer()), pagination))
        } catch (ex: Exception) {
            respondError(HttpStatusCode.InternalServerError, ErrorMessages.InternalServerError.message)
        }
    }

    private fun mangaPageFilter(
        query: String,
        type: String,
        status: String,
        sfw: Boolean,
        page: Int,
        size: Int
    ): Bson = Filters.and(
        Filters.eq("cacheQuery", query),
        Filters.eq("cacheType", type),
        Filters.eq("cacheStatus", status),
        Filters.eq("cacheSfw", sfw),
        Filters.eq("cachePage", page),
        Filters.eq("cacheSize", size)
    )

    private fun MangaDetail.isSafe(): Boolean = explicitGenres.isEmpty()
}
