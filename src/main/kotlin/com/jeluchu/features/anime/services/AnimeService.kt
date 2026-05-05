package com.jeluchu.features.anime.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.AnimeStatusTypes
import com.jeluchu.core.enums.AnimeTypes
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseAnimeStatusType
import com.jeluchu.core.enums.parseAnimeType
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.ErrorResponse
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.models.documentToSimpleAnimeEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity.Companion.toLastEpisodeData
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimeLastEpisodeEntity
import com.jeluchu.features.anime.mappers.documentToAnimeTypeEntity
import com.jeluchu.features.anime.mappers.documentToMoreInfoEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class AnimeService(
    private val database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val directoryCollection = database.getCollection(Collections.ANIME_DIRECTORY)

    suspend fun getDirectory(call: RoutingCall) {
        try {
            val type = call.request.queryParameters["type"].orEmpty()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

            if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
            val skipCount = (page - 1) * size

            if (parseAnimeType(type) == null) {
                val animes = directoryCollection
                    .find()
                    .skip(skipCount)
                    .limit(size)
                    .toList()

                val elements = animes.map { documentToAnimeTypeEntity(it) }

                val response = PaginationResponse(
                    page = page,
                    size = size,
                    data = elements
                )

                call.respond(HttpStatusCode.OK, Json.encodeToString(response))
            } else {
                val animes = directoryCollection
                    .find(Filters.eq("type", type.uppercase()))
                    .skip(skipCount)
                    .limit(size)
                    .toList()

                val elements = animes.map { documentToAnimeTypeEntity(it) }

                val response = PaginationResponse(
                    page = page,
                    size = size,
                    data = elements
                )

                call.respond(HttpStatusCode.OK, Json.encodeToString(response))
            }
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(ErrorMessages.UnauthorizedMongo.message))
        }
    }

    suspend fun getAnimeByMalId(call: RoutingCall) = try {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException(ErrorMessages.InvalidMalId.message)
        directoryCollection.find(Filters.eq("malId", id)).firstOrNull()?.let { anime ->
            val info = documentToMoreInfoEntity(anime)
            call.respond(HttpStatusCode.OK, Json.encodeToString(info))
        } ?: call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.AnimeNotFound.message))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.InvalidInput.message))
    }

    suspend fun getRandomAnime(call: RoutingCall) = try {
        val nsfw = call.request.queryParameters["nsfw"]?.toBoolean() ?: false

        val filters = mutableListOf<org.bson.conversions.Bson>().apply {
            add(Filters.`in`("type", listOf(
                AnimeTypes.TV,
                AnimeTypes.MOVIE,
                AnimeTypes.OVA,
                AnimeTypes.SPECIAL,
                AnimeTypes.ONA,
                AnimeTypes.TV_SPECIAL
            )))

            add(Filters.nin("status", listOf(
                AnimeStatusTypes.UPCOMING
            )))

            if (!nsfw) add(Filters.eq("nsfw", false))
        }

        val aggregates = listOf(
            Aggregates.match(Filters.and(filters)),
            Aggregates.sample(1)
        )

        directoryCollection.aggregate(aggregates).firstOrNull()?.let { anime ->
            val info = documentToMoreInfoEntity(anime)

            call.response.headers.append("Cache-Control", "no-store")

            call.respond(HttpStatusCode.OK, Json.encodeToString(info))
        } ?: call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.AnimeNotFound.message))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.InvalidInput.message))
    }

    suspend fun getLastEpisodes(call: RoutingCall) = try {
        val dayOfWeek = LocalDate.now()
            .dayOfWeek
            .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            .plus("s")

        val timerKey = TimerKey.LAST_EPISODES
        val collection = database.getCollection(timerKey)

        val needsUpdate = timers.needsUpdate(
            amount = 6,
            key = timerKey,
            unit = TimeUnit.HOUR,
        )

        if (needsUpdate) {
            collection.deleteMany(Document())

            val animes = mutableListOf<LastEpisodeEntity>()

            RestClient.request(
                BaseUrls.JIKAN + "anime?status=airing&type=tv&page=1",
                AnimeSearch.serializer()
            ).let { firstPage ->
                val totalPage = firstPage.pagination?.lastPage ?: 2

                firstPage.data?.let { firstAnimes ->
                    firstAnimes.map { it.toLastEpisodeData() }.let { animes.addAll(it) }
                }

                for (page in 2..totalPage) {
                    RestClient.request(
                        BaseUrls.JIKAN + "anime?status=airing&type=tv&page=$page",
                        AnimeSearch.serializer()
                    ).data?.let { pagesAnimes ->
                        animes.addAll(pagesAnimes.map { it.toLastEpisodeData() })
                        delay(1000)
                    }
                }
            }

            val documentsToInsert = parseDataToDocuments(animes.distinctBy { it.malId }, LastEpisodeEntity.serializer())
            if (documentsToInsert.isNotEmpty()) collection.insertMany(documentsToInsert)
            timers.update(timerKey)

            val queryDb = collection
                .find(Filters.eq("day", dayOfWeek))
                .toList()

            val elements = queryDb.map { documentToAnimeLastEpisodeEntity(it) }
            call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
        } else {
            val elements = collection
                .find(Filters.eq("day", dayOfWeek))
                .toList()
                .map { documentToAnimeLastEpisodeEntity(it) }

            call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
        }
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.Unauthorized, ErrorResponse(ErrorMessages.UnauthorizedMongo.message))
    }

    suspend fun getAnimeByType(call: RoutingCall) = try {
        val type = call.request.queryParameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopAnimeType.message)
        val status = call.request.queryParameters["status"] ?: throw IllegalArgumentException(ErrorMessages.InvalidAnimeStatusType.message)
        val nsfw = call.request.queryParameters["nsfw"].toBoolean()

        val animes = directoryCollection.find(
            Filters.and(
                Filters.eq("type", parseAnimeType(type)),
                Filters.eq("status", parseAnimeStatusType(status)),
                Filters.eq("nsfw", nsfw),
            )
        )
            .sort(Sorts.descending("aired.from"))
            .toList()

        val elements = animes.map { documentToSimpleAnimeEntity(it) }
        call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.InvalidInput.message))
    }
}
