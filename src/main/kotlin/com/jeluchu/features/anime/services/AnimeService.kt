package com.jeluchu.features.anime.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.AnimeStatusTypes
import com.jeluchu.core.enums.AnimeTypes
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseAnimeStatusType
import com.jeluchu.core.enums.parseSeasons
import com.jeluchu.core.enums.parseAnimeType
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.parseSfwPreference
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.isSafeAnimeDocument
import com.jeluchu.core.extensions.getDocumentSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.extensions.toJson
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.models.documentToSimpleAnimeEntity
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimeLastEpisodeEntity
import com.jeluchu.features.anime.mappers.documentToAnimeTypeEntity
import com.jeluchu.features.anime.mappers.documentToMoreInfoEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity
import com.jeluchu.features.anime.models.discovery.DiscoveryRecommendationEntity
import com.jeluchu.features.anime.utils.fetchLastEpisodesFromJikan
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.conversions.Bson
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.math.ceil

class AnimeService(
    private val database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val directoryCollection = database.getCollection(Collections.ANIME_DIRECTORY)
    private val maxAnimePageSize = 100

    suspend fun getDirectory(call: RoutingCall) {
        try {
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

            if (page < 1 || size < 1 || size > maxAnimePageSize) {
                return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidSizeAndPage.message)
            }

            val query = buildDirectoryQuery(call) ?: return
            val filter = query.toFilter()
            val skipCount = (page - 1) * size
            val totalItems = directoryCollection.countDocuments(filter).toInt()
            val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / size).toInt()

            val elements = directoryCollection
                .find(filter)
                .sort(query.sort)
                .skip(skipCount)
                .limit(size)
                .toList()

            call.respond(
                status = HttpStatusCode.OK,
                message = PaginationResponse(
                    page = page,
                    size = size,
                    totalPages = totalPages,
                    totalItems = totalItems,
                    data = elements.map { documentToAnimeTypeEntity(it) }
                ).toJson()
            )
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.Unauthorized, ErrorMessages.UnauthorizedMongo.message)
        }
    }

    suspend fun getAnimeByMalId(call: RoutingCall) {
        try {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException(ErrorMessages.InvalidMalId.message)
            val sfw = call.parseSfwPreference() ?: return
            directoryCollection.find(Filters.eq("malId", id)).firstOrNull()?.let { anime ->
                if (sfw && !anime.isSafeAnimeDocument()) {
                    return call.respondError(HttpStatusCode.NotFound, ErrorMessages.AnimeNotFound.message)
                }

                val info = documentToMoreInfoEntity(anime)
                call.respond(HttpStatusCode.OK, Json.encodeToString(info))
            } ?: call.respondError(HttpStatusCode.NotFound, ErrorMessages.AnimeNotFound.message)
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidInput.message)
        }
    }

    suspend fun getRandomAnime(call: RoutingCall) {
        try {
            val sfw = call.parseSfwPreference() ?: return

            val filters = mutableListOf<Bson>().apply {
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

                if (sfw) {
                    add(Filters.eq("nsfw", false))
                    add(Filters.nin("ageRating", listOf("R+ - Mild Nudity", "Rx - Hentai")))
                    add(Filters.nin("rating", listOf("R+ - Mild Nudity", "Rx - Hentai")))
                }
            }

            val aggregates = listOf(
                Aggregates.match(Filters.and(filters)),
                Aggregates.sample(1)
            )

            directoryCollection.aggregate(aggregates).firstOrNull()?.let { anime ->
                val info = documentToMoreInfoEntity(anime)

                call.response.headers.append("Cache-Control", "no-store")

                call.respond(HttpStatusCode.OK, Json.encodeToString(info))
            } ?: call.respondError(HttpStatusCode.NotFound, ErrorMessages.AnimeNotFound.message)
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidInput.message)
        }
    }

    suspend fun getDiscovery(call: RoutingCall) {
        try {
            val favoriteIds = call.request.queryParameters["favoriteIds"].toAnimeIds()
            val excludeIds = call.request.queryParameters["excludeIds"].toAnimeIds()
            val sfw = call.parseSfwPreference() ?: return
            val size = call.request.queryParameters["size"]?.toIntOrNull()?.coerceIn(1, 30) ?: 20

            if (favoriteIds.isEmpty()) {
                return call.respond(HttpStatusCode.OK, Json.encodeToString(emptyList<DiscoveryRecommendationEntity>()))
            }

            val favoriteDocuments = directoryCollection
                .find(Filters.`in`("malId", favoriteIds))
                .toList()

            val profile = SmartDiscoveryEngine.profile(favoriteDocuments)

            val filters = mutableListOf<Bson>(
                Filters.nin("malId", (favoriteIds + excludeIds).distinct()),
                Filters.nin("status", listOf(AnimeStatusTypes.UPCOMING)),
            )

            val affinityFilters = mutableListOf<Bson>()
            if (profile.preferredGenres.isNotEmpty()) {
                val genres = profile.preferredGenres
                affinityFilters += Filters.`in`("tags.es", genres)
                affinityFilters += Filters.`in`("tags.en", genres)
                affinityFilters += Filters.`in`("genres.es", genres)
                affinityFilters += Filters.`in`("genres.en", genres)
            }
            if (profile.preferredStudios.isNotEmpty()) {
                affinityFilters += Filters.`in`("studios.name", profile.preferredStudios)
            }
            if (profile.preferredTypes.isNotEmpty()) {
                affinityFilters += Filters.`in`("type", profile.preferredTypes)
                affinityFilters += Filters.`in`("type", profile.preferredTypes.map(String::uppercase))
            }
            if (affinityFilters.isNotEmpty()) {
                filters += Filters.or(affinityFilters)
            }

            if (sfw) {
                filters += Filters.eq("nsfw", false)
                filters += Filters.nin("ageRating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
                filters += Filters.nin("rating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
            }

            val recommendations = directoryCollection
                .find(Filters.and(filters))
                .limit(1000)
                .toList()
                .map { anime ->
                    val score = SmartDiscoveryEngine.score(anime, profile)
                    val quality = anime.getStringSafe("score").toDoubleOrNull() ?: 0.0
                    DiscoveryCandidate(
                        anime = anime,
                        affinity = score.affinity,
                        quality = quality,
                        basedOnTitle = SmartDiscoveryEngine.bestSourceTitle(anime, favoriteDocuments),
                        matchedGenres = score.matchedGenres,
                        matchedStudios = score.matchedStudios,
                    )
                }
                .filter { it.affinity > 0 }
                .sortedWith(
                    compareByDescending<DiscoveryCandidate> { it.affinity }
                        .thenByDescending { it.quality }
                )
                .take(size)
                .map { candidate ->
                    val anime = documentToSimpleAnimeEntity(candidate.anime)
                    DiscoveryRecommendationEntity(
                        malId = anime.malId,
                        title = anime.title,
                        image = anime.image,
                        type = anime.type,
                        basedOnTitle = candidate.basedOnTitle,
                        matchedGenres = candidate.matchedGenres,
                        matchedStudios = candidate.matchedStudios,
                    )
                }

            call.respond(HttpStatusCode.OK, Json.encodeToString(recommendations))
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidInput.message)
        }
    }

    suspend fun getLastEpisodes(call: RoutingCall) {
        try {
            val sfw = call.parseSfwPreference() ?: return
            val dayOfWeek = LocalDate.now()
                .dayOfWeek
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                .plus("s")

            val timerKey = "${TimerKey.LAST_EPISODES}_sfw_$sfw"
            val collection = database.getCollection(Collections.LAST_EPISODES)
            val dayFilter = Filters.and(
                Filters.eq("day", dayOfWeek),
                Filters.eq("sfw", sfw)
            )

            val cacheHasCurrentDay = collection.countDocuments(dayFilter) > 0
            val needsUpdate = !cacheHasCurrentDay || timers.needsUpdate(
                amount = 6,
                key = timerKey,
                unit = TimeUnit.HOUR,
            )

            var refreshFailure: Throwable? = null
            if (needsUpdate) {
                runCatching {
                    val animes = fetchLastEpisodesFromJikan(sfw = sfw)
                    call.application.environment.log.info(
                        "Fetched ${animes.size} last episodes from Jikan for sfw=$sfw; " +
                            "requestedDay=$dayOfWeek, days=${animes.groupingBy { it.day }.eachCount()}"
                    )
                    val documentsToInsert = parseDataToDocuments(animes, serializer = LastEpisodeEntity.serializer())

                    if (documentsToInsert.isNotEmpty()) {
                        collection.deleteMany(Filters.eq("sfw", sfw))
                        collection.insertMany(documentsToInsert)
                        timers.update(timerKey)
                    }
                }.onFailure { cause ->
                    if (cause is CancellationException) throw cause
                    refreshFailure = cause
                    call.application.environment.log.warn(
                        "Unable to refresh last episodes from Jikan for sfw=$sfw; serving cached data",
                        cause
                    )
                }
            }

            val elements = collection
                .find(dayFilter)
                .toList()
                .map { documentToAnimeLastEpisodeEntity(it) }

            if (elements.isEmpty() && refreshFailure != null) {
                return call.respondError(
                    HttpStatusCode.BadGateway,
                    ErrorMessages.AnimeProviderUnavailable.message
                )
            }

            call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.Unauthorized, ErrorMessages.UnauthorizedMongo.message)
        }
    }

    private fun String?.toAnimeIds(): List<Int> = orEmpty()
        .split(',')
        .mapNotNull(String::toIntOrNull)
        .filter { it > 0 }
        .distinct()
        .take(50)

    private data class DiscoveryCandidate(
        val anime: Document,
        val affinity: Int,
        val quality: Double,
        val basedOnTitle: String,
        val matchedGenres: List<String>,
        val matchedStudios: List<String>,
    )

    suspend fun getAnimeByType(call: RoutingCall) {
        try {
            val type = call.request.queryParameters["type"] ?: throw IllegalArgumentException(ErrorMessages.InvalidTopAnimeType.message)
            val status = call.request.queryParameters["status"] ?: throw IllegalArgumentException(ErrorMessages.InvalidAnimeStatusType.message)
            val sfw = call.parseSfwPreference() ?: return
            val pageParam = call.request.queryParameters["page"]
            val sizeParam = call.request.queryParameters["size"]
            val isPaginatedRequest = pageParam != null || sizeParam != null
            val page = pageParam?.toIntOrNull()
            val size = sizeParam?.toIntOrNull()

            if ((pageParam != null && page == null) || (sizeParam != null && size == null)) {
                return call.respondError(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorMessages.InvalidSizeAndPage.message,
                    code = "INVALID_PAGE_SIZE"
                )
            }

            val safePage = page ?: 1
            val safeSize = size ?: 25

            if (safePage < 1 || safeSize < 1 || safeSize > maxAnimePageSize) {
                return call.respondError(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorMessages.InvalidSizeAndPage.message,
                    code = "INVALID_PAGE_SIZE"
                )
            }

            val filter = Filters.and(
                Filters.eq("type", parseAnimeType(type)),
                Filters.eq("status", parseAnimeStatusType(status)),
                if (sfw) Filters.eq("nsfw", false) else Document(),
                if (sfw) Filters.nin("ageRating", listOf("R+ - Mild Nudity", "Rx - Hentai")) else Document(),
                if (sfw) Filters.nin("rating", listOf("R+ - Mild Nudity", "Rx - Hentai")) else Document(),
            )

            if (isPaginatedRequest) call.respondAnimePage(filter = filter, page = safePage, size = safeSize)
            else {
                val animes = directoryCollection.find(filter)
                    .sort(Sorts.descending("aired.from"))
                    .toList()

                val elements = animes.map { documentToSimpleAnimeEntity(it) }
                call.respond(HttpStatusCode.OK, Json.encodeToString(elements))
            }
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidInput.message)
        }
    }

    private suspend fun RoutingCall.respondAnimePage(
        filter: Bson,
        page: Int,
        size: Int
    ) {
        val skipCount = (page - 1) * size
        val totalItems = directoryCollection.countDocuments(filter).toInt()
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / size).toInt()
        val animes = directoryCollection.find(filter)
            .sort(Sorts.descending("aired.from"))
            .skip(skipCount)
            .limit(size)
            .toList()
            .map { documentToSimpleAnimeEntity(it) }

        val response = PaginationResponse(
            page = page,
            size = size,
            totalPages = totalPages,
            totalItems = totalItems,
            data = animes
        )

        respond(HttpStatusCode.OK, Json.encodeToString(response))
    }

    private suspend fun buildDirectoryQuery(call: RoutingCall): DirectoryQuery? {
        val types = parseAnimeTypeList(call.request.queryParameters.getAll("type"), call.request.queryParameters["types"])
            ?: return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidAnimeType.message).let { null }

        val statuses = parseAnimeStatusList(call.request.queryParameters.getAll("status"), call.request.queryParameters["statuses"])
            ?: return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidAnimeStatusType.message).let { null }

        val rawSeason = call.request.queryParameters["season"] ?: call.request.queryParameters["station"]
        val season = parseDirectorySeason(rawSeason)
        if (!rawSeason.isNullOrBlank() && season == null) {
            return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidAnimeSeason.message).let { null }
        }

        val year = call.request.queryParameters["year"]?.toIntOrNull()
        val years = parseIntCsv(call.request.queryParameters["years"])
        val yearFrom = call.request.queryParameters["yearFrom"]?.toIntOrNull()
        val yearTo = call.request.queryParameters["yearTo"]?.toIntOrNull()
        val sfw = call.parseSfwPreference() ?: return null

        if (hasInvalidIntValue(call.request.queryParameters["year"], year) ||
            hasInvalidIntValue(call.request.queryParameters["yearFrom"], yearFrom) ||
            hasInvalidIntValue(call.request.queryParameters["yearTo"], yearTo) ||
            hasInvalidIntCsv(call.request.queryParameters["years"], years)
        ) {
            return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidRequest.message).let { null }
        }

        if (yearFrom != null && yearTo != null && yearFrom > yearTo) {
            return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidRequest.message).let { null }
        }

        val sortBy = call.request.queryParameters["sortBy"].orEmpty().ifBlank { "recent" }
        val sortOrder = call.request.queryParameters["sortOrder"].orEmpty().ifBlank { "desc" }
        val sort = buildDirectorySort(sortBy = sortBy, sortOrder = sortOrder)
            ?: return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidDirectorySort.message).let { null }

        return DirectoryQuery(
            types = types,
            statuses = statuses,
            sfw = sfw,
            season = season,
            year = year,
            years = years.orEmpty(),
            yearFrom = yearFrom,
            yearTo = yearTo,
            sort = sort
        )
    }

    private fun parseAnimeTypeList(values: List<String>?, csvValue: String?): List<String>? {
        val raw = parseMergedValues(values, csvValue)
        if (raw.isEmpty()) return emptyList()

        val parsed = raw.map { parseAnimeType(it)?.name ?: return null }
        return parsed.distinct()
    }

    private fun parseAnimeStatusList(values: List<String>?, csvValue: String?): List<String>? {
        val raw = parseMergedValues(values, csvValue)
        if (raw.isEmpty()) return emptyList()

        val parsed = raw.map { parseAnimeStatusType(it)?.name ?: return null }
        return parsed.distinct()
    }

    private fun parseDirectorySeason(rawSeason: String?): String? {
        if (rawSeason.isNullOrBlank()) return null
        return parseSeasons(rawSeason)?.name
    }

    private fun parseMergedValues(values: List<String>?, csvValue: String?): List<String> {
        return buildList {
            values.orEmpty().forEach { addAll(splitCsvValues(it)) }
            addAll(splitCsvValues(csvValue))
        }.distinct()
    }

    private fun splitCsvValues(value: String?): List<String> {
        return value
            ?.split(",")
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun parseIntCsv(value: String?): List<Int>? {
        if (value.isNullOrBlank()) return emptyList()
        return splitCsvValues(value).map { it.toIntOrNull() ?: return null }
    }

    private fun hasInvalidIntValue(rawValue: String?, parsedValue: Int?): Boolean {
        return rawValue != null && parsedValue == null
    }

    private fun hasInvalidIntCsv(rawValue: String?, parsedValues: List<Int>?): Boolean {
        return rawValue != null && parsedValues == null
    }

    private fun hasInvalidBooleanValue(rawValue: String?, parsedValue: Boolean?): Boolean {
        return rawValue != null && parsedValue == null
    }

    private fun buildDirectorySort(sortBy: String, sortOrder: String): Bson? {
        val descending = !sortOrder.equals("asc", ignoreCase = true)
        return when (sortBy.lowercase()) {
            "recent", "airedfrom" -> if (descending) Sorts.descending("aired.from") else Sorts.ascending("aired.from")
            "score" -> if (descending) Sorts.descending("score") else Sorts.ascending("score")
            "title" -> if (descending) Sorts.descending("title") else Sorts.ascending("title")
            "year", "seasonyear" -> {
                val primary = if (descending) Sorts.descending("season.year") else Sorts.ascending("season.year")
                val fallback = if (descending) Sorts.descending("year") else Sorts.ascending("year")
                Sorts.orderBy(primary, fallback)
            }
            "malid", "id" -> if (descending) Sorts.descending("malId") else Sorts.ascending("malId")
            else -> null
        }
    }

    private data class DirectoryQuery(
        val types: List<String>,
        val statuses: List<String>,
        val sfw: Boolean,
        val season: String?,
        val year: Int?,
        val years: List<Int>,
        val yearFrom: Int?,
        val yearTo: Int?,
        val sort: Bson
    ) {
        fun toFilter(): Bson {
            val filters = mutableListOf<Bson>()

            if (types.isNotEmpty()) filters += Filters.`in`("type", types)
            if (statuses.isNotEmpty()) filters += Filters.`in`("status", statuses)
            if (sfw) {
                filters += Filters.eq("nsfw", false)
                filters += Filters.nin("ageRating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
                filters += Filters.nin("rating", listOf("R+ - Mild Nudity", "Rx - Hentai"))
            }
            season?.let {
                filters += Filters.or(
                    Filters.eq("season.station", it),
                    Filters.eq("season", it)
                )
            }

            when {
                years.isNotEmpty() -> filters += Filters.or(
                    Filters.`in`("season.year", years),
                    Filters.`in`("year", years)
                )
                year != null -> filters += Filters.or(
                    Filters.eq("season.year", year),
                    Filters.eq("year", year)
                )
                yearFrom != null || yearTo != null -> {
                    val rangeFilters = listOfNotNull(
                        yearFrom?.let { Filters.gte("season.year", it) },
                        yearTo?.let { Filters.lte("season.year", it) }
                    )
                    val legacyRangeFilters = listOfNotNull(
                        yearFrom?.let { Filters.gte("year", it) },
                        yearTo?.let { Filters.lte("year", it) }
                    )

                    filters += Filters.or(
                        Filters.and(rangeFilters),
                        Filters.and(legacyRangeFilters)
                    )
                }
            }

            return if (filters.isEmpty()) Document() else Filters.and(filters)
        }
    }
}
