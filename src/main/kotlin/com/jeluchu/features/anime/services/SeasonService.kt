package com.jeluchu.features.anime.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.extensions.isSafeAnimeData
import com.jeluchu.core.enums.parseSeasons
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.parseSfwPreference
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.models.documentToSimpleAnimeEntity
import com.jeluchu.core.models.jikan.anime.AnimeData.Companion.toUpcomingAnime
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.utils.*
import com.jeluchu.features.anime.mappers.documentToUpcomingAnimeSeason
import com.jeluchu.features.anime.models.seasons.UpcomingAnimeSeasonEntity
import com.jeluchu.features.anime.models.seasons.YearSeasons
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.bson.Document
import java.time.Year

class SeasonService(
    private val database: MongoDatabase,
    private val directory: MongoCollection<Document> = database.getCollection(Collections.ANIME_DIRECTORY)
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val upcomingSeasonFilters = setOf("tv", "movie", "ova", "special", "ona", "music")

    suspend fun getAnimeBySeason(call: RoutingCall) {
        val sfw = call.parseSfwPreference() ?: return
        val year = call.request.queryParameters["year"]?.toInt() ?: SeasonCalendar.currentYear
        val station = parseSeasons(call.request.queryParameters["station"] ?: SeasonCalendar.currentSeason.name)
            ?: SeasonCalendar.currentSeason

        val filters = mutableListOf(
            Filters.eq("season.year", year),
            Filters.eq("season.station", station),
            Filters.ne("type", "MUSIC"),
            Filters.ne("type", "PV"),
        )

        if (sfw) {
            filters += Filters.eq("nsfw", false)
            filters += Filters.nin("ageRating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
            filters += Filters.nin("rating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
        }

        val query = directory.find(Filters.and(filters))
            .toList()
            .map { documentToSimpleAnimeEntity(it) }

        call.respond(HttpStatusCode.OK, Json.encodeToString(query))
    }

    suspend fun getUpcomingAnimeSeason(call: RoutingCall) {
        val sfw = call.parseSfwPreference() ?: return
        val filter = call.request.queryParameters["filter"]?.lowercase() ?: "tv"
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["limit"]?.toIntOrNull()
            ?: call.request.queryParameters["size"]?.toIntOrNull()
            ?: 25

        if (size > 25) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidValueTopPage.message)
        if (filter !in upcomingSeasonFilters) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidTopAnimeType.message)
        if (page < 1 || size < 1) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)

        val timerKey = "${TimerKey.UPCOMING_SEASON}_sfw_${sfw}_${filter}_page_${page}_limit_${size}"
        val collection = database.getCollection(Collections.UPCOMING_SEASON)

        val needsUpdate = timers.needsUpdate(
            amount = 6,
            key = timerKey,
            unit = TimeUnit.HOUR
        )

        if (needsUpdate) {
            collection.deleteMany(
                Filters.and(
                    Filters.eq("sfw", sfw),
                    Filters.eq("page", page),
                    Filters.eq("limit", size),
                    Filters.eq("filter", filter),
                )
            )

            val params = mutableListOf<String>().apply {
                add("page=$page")
                add("limit=$size")
                add("filter=$filter")
            }

            val paramsPath = if (sfw) "?sfw&${params.joinToString(separator = "&")}"
            else "?${params.joinToString(separator = "&")}"

            val rawResponse = RestClient.request(
                url = "${BaseUrls.TENRAI}seasons/upcoming$paramsPath",
                deserializer = AnimeSearch.serializer()
            )
            val totalItems = rawResponse.pagination.itemsPage?.total ?: 0
            val totalPages = rawResponse.pagination.lastPage ?: 0
            val raw = rawResponse.data
                .filter { anime -> !sfw || anime.isSafeAnimeData() }
                .map {
                it.toUpcomingAnime(
                    sfw = sfw,
                    page = page,
                    limit = size,
                    filter = filter
                )
                }.distinctBy { it.malId }

            val documentsToInsert = parseDataToDocuments(
                data = raw,
                serializer = UpcomingAnimeSeasonEntity.serializer()
            )
                .onEach {
                    it.append("totalPages", totalPages)
                    it.append("totalItems", totalItems)
                }

            if (documentsToInsert.isNotEmpty()) collection.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.map { documentToUpcomingAnimeSeason(it) }

            val paginationResponse = PaginationResponse(
                page = page,
                data = elements,
                size = size,
                totalPages = totalPages,
                totalItems = totalItems
            )

            call.respond(status = HttpStatusCode.OK, message = Json.encodeToString(value = paginationResponse))
        } else {
            val documents = collection
                .find(
                    Filters.and(
                        Filters.eq("sfw", sfw),
                        Filters.eq("page", page),
                        Filters.eq("limit", size),
                        Filters.eq("filter", filter),
                    )
                )
                .toList()
            val elements = documents
                .map { documentToUpcomingAnimeSeason(it) }
                .distinctBy { it.malId }
                .take(size)
            val first = documents.firstOrNull()

            val response = PaginationResponse(
                page = page,
                data = elements,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0
            )

            call.respond(status = HttpStatusCode.OK, message = Json.encodeToString(value = response))
        }
    }

    suspend fun getYearsAndSeasons(call: RoutingCall) {
        val currentYear = Year.now().value
        val validSeasons = listOf("SUMMER", "FALL", "WINTER", "SPRING")

        val pipeline = listOf(
            Aggregates.match(
                Document(
                    "\$and", listOf(
                        Document("season.year", Document("\$gt", 0)),
                        Document("season.year", Document("\$lte", currentYear)),
                        Document("season.station", Document("\$in", validSeasons))
                    )
                )
            ),
            Aggregates.group(
                "\$season.year",
                Accumulators.addToSet("seasons", "\$season.station")
            ),
            Aggregates.project(
                Document().apply {
                    put("year", "\$_id")
                    put("seasons", 1)
                    put("_id", 0)
                }
            ),
            Aggregates.sort(Sorts.descending("year"))
        )

        val results = directory.aggregate(pipeline).toList()
        val index = results.map { document ->
            YearSeasons(
                year = document.getInteger("year"),
                seasons = document.getList("seasons", String::class.java)
            )
        }

        call.respond(HttpStatusCode.OK, Json.encodeToString(index))
    }
}
