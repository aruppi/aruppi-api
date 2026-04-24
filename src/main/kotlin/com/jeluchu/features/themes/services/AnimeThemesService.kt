package com.jeluchu.features.themes.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.extensions.badRequestError
import com.jeluchu.core.extensions.getIntSafeQueryParam
import com.jeluchu.core.extensions.getStringSafeParam
import com.jeluchu.core.extensions.getStringSafeQueryParam
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.ErrorResponse
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimesThemeEntity
import com.jeluchu.features.anime.mappers.documentToArtistEntity
import com.jeluchu.features.anime.mappers.documentToSongEntity
import com.jeluchu.features.themes.models.artist.AnimeThemeDetail
import com.jeluchu.features.themes.models.artist.AnimeThemeDetail.Companion.toAnimeThemeDetail
import com.jeluchu.features.themes.models.artist.AnimeThemeShow
import com.jeluchu.features.themes.models.artist.ArtistEntity
import com.jeluchu.features.themes.models.artist.ArtistEntity.Companion.toArtistEntity
import com.jeluchu.features.themes.models.artist.ArtistSearch
import com.jeluchu.features.themes.models.artist.ArtistShow
import com.jeluchu.features.themes.models.artist.SongSearch
import com.jeluchu.features.themes.models.artist.documentToAnimeThemeDetail
import com.jeluchu.features.themes.models.song.SongEntity
import com.jeluchu.features.themes.models.song.SongEntity.Companion.toSongEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AnimeThemesService(
    private val database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val themesDirectory = database.getCollection(Collections.ANIME_THEMES)
    private val artistsDirectory = database.getCollection(Collections.ARTISTS_INDEX)
    private val songsDirectory = database.getCollection(Collections.SONGS_INDEX)
    private val animeThemeDetail = database.getCollection(Collections.ANIME_THEMES_DETAIL)

    private val artistInclude = "songs.animethemes.anime,songs.animethemes.animethemeentries.videos"
    private val songInclude = "artists,animethemes.anime"
    private val animeDetailInclude = "animethemes.song,animethemes.animethemeentries.videos,images"

    private fun String.toAnimeThemesSlug() = trim()
        .lowercase()
        .replace("+", " ")
        .replace(Regex("[\\s-]+"), "_")
        .replace(Regex("_+"), "_")

    suspend fun getAnimeThemes(call: RoutingCall) {
        val page = call.getIntSafeQueryParam("page", 1)
        val size = call.getIntSafeQueryParam("size", 25)

        val skipCount = (page - 1) * size
        if (page < 1 || size < 1) call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

        val query = themesDirectory
            .find()
            .skip(skipCount)
            .limit(size)
            .toList()
            .map { documentToAnimesThemeEntity(it) }

        val paginate = PaginationResponse(page = page, data = query, size = query.size)
        call.respond(HttpStatusCode.OK, Json.encodeToString(paginate))
    }

    suspend fun getAnimeThemeBySlug(call: RoutingCall) = try {
        val slug = call.getStringSafeParam("slug").toAnimeThemesSlug()

        if (slug.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorMessages.NotFound.message))
        }

        val timerKey = "${TimerKey.THEMES}anime_$slug"
        val needsUpdate = timers.needsUpdate(key = timerKey, amount = 7, unit = TimeUnit.DAY)

        if (needsUpdate) {
            animeThemeDetail.deleteMany(Filters.eq("slug", slug))

            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}anime/$slug?include=$animeDetailInclude",
                AnimeThemeShow.serializer()
            )

            val detail = raw.anime?.toAnimeThemeDetail()
                ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.NotFound.message))

            val documents = parseDataToDocuments(listOf(detail), AnimeThemeDetail.serializer())
            if (documents.isNotEmpty()) animeThemeDetail.insertMany(documents)
            timers.update(timerKey)

            call.respond(HttpStatusCode.OK, Json.encodeToString(detail))
        } else {
            val cached = animeThemeDetail.find(Filters.eq("slug", slug)).firstOrNull()
                ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.NotFound.message))

            call.respond(HttpStatusCode.OK, Json.encodeToString(documentToAnimeThemeDetail(cached)))
        }
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, Json.encodeToString(ErrorResponse(ErrorMessages.NotFound.message)))
    }

    // ── GET /api/v5/themes/anime/{slug}/random ────────────────────────────────

    suspend fun getRandomAnimeTheme(call: RoutingCall) = try {
        val slug = call.getStringSafeParam("slug").toAnimeThemesSlug()

        if (slug.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorMessages.NotFound.message))
        }

        val cached = animeThemeDetail.find(Filters.eq("slug", slug)).firstOrNull()

        val detail = if (cached != null) {
            documentToAnimeThemeDetail(cached)
        } else {
            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}anime/$slug?include=$animeDetailInclude",
                AnimeThemeShow.serializer()
            )
            raw.anime?.toAnimeThemeDetail()
                ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.NotFound.message))
        }

        val randomTheme = detail.themes?.randomOrNull()
            ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.NotFoundContent.message))

        call.response.headers.append("Cache-Control", "no-store")
        call.respond(HttpStatusCode.OK, Json.encodeToString(randomTheme))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, Json.encodeToString(ErrorResponse(ErrorMessages.NotFound.message)))
    }

    suspend fun getRandomSong(call: RoutingCall) = try {
        val raw = RestClient.request(
            "${BaseUrls.ANIME_THEMES}song?include=$songInclude&page[number]=1&page[size]=1&sort=random",
            SongSearch.serializer()
        )

        val song = raw.songs?.firstOrNull()?.toSongEntity()
            ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.NotFoundContent.message))

        call.response.headers.append("Cache-Control", "no-store")
        call.respond(HttpStatusCode.OK, Json.encodeToString(song))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.InternalServerError, Json.encodeToString(ErrorResponse(ex.message ?: ErrorMessages.InvalidInput.message)))
    }

    suspend fun getArtists(call: RoutingCall) = try {
        val page = call.getIntSafeQueryParam("page", 1)
        val size = call.getIntSafeQueryParam("size", 25)

        if (page < 1 || size < 1) call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

        val skipCount = (page - 1) * size
        val timerKey = "${TimerKey.THEMES}artists_$page"
        val needsUpdate = timers.needsUpdate(key = timerKey, amount = 7, unit = TimeUnit.DAY)

        if (needsUpdate) {
            artistsDirectory.deleteMany(Filters.eq("page_cache", page))

            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}artist?include=$artistInclude&page[number]=$page&page[size]=$size",
                ArtistSearch.serializer()
            )
            val artists = raw.artists?.map { it.toArtistEntity() } ?: emptyList()

            val documents = parseDataToDocuments(artists, ArtistEntity.serializer())
                .onEach { it.append("page_cache", page) }

            if (documents.isNotEmpty()) artistsDirectory.insertMany(documents)
            timers.update(timerKey)

            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(PaginationResponse(page = page, size = artists.size, data = artists))
            )
        } else {
            val artists = artistsDirectory
                .find(Filters.eq("page_cache", page))
                .skip(skipCount)
                .limit(size)
                .toList()
                .map { documentToArtistEntity(it) }

            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(PaginationResponse(page = page, size = artists.size, data = artists))
            )
        }
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(ex.message ?: ErrorMessages.InvalidInput.message))
    }

    suspend fun getArtistBySlug(call: RoutingCall) = try {
        val slug = call.getStringSafeParam("slug")

        if (slug.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(ErrorMessages.ArtistNotFound.message))
        }

        val raw = RestClient.request(
            "${BaseUrls.ANIME_THEMES}artist/$slug?include=$artistInclude",
            ArtistShow.serializer()
        )

        val artist = raw.artist?.toArtistEntity()
            ?: return call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.ArtistNotFound.message))

        artistsDirectory.deleteMany(Filters.eq("slug", slug))
        val documents = parseDataToDocuments(listOf(artist), ArtistEntity.serializer())
        if (documents.isNotEmpty()) artistsDirectory.insertMany(documents)

        call.respond(HttpStatusCode.OK, Json.encodeToString(artist))
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse(ErrorMessages.ArtistNotFound.message))
    }

    suspend fun searchSongs(call: RoutingCall) = try {
        val query = call.getStringSafeQueryParam("q")
        val page = call.getIntSafeQueryParam("page", 1)
        val size = call.getIntSafeQueryParam("size", 25)

        if (page < 1 || size < 1) call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

        if (query.isNotBlank()) {
            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}song?q=${query.trim()}&include=$songInclude&page[number]=$page&page[size]=$size",
                SongSearch.serializer()
            )
            val songs = raw.songs?.map { it.toSongEntity() } ?: emptyList()

            call.respond(
                HttpStatusCode.OK,
                Json.encodeToString(PaginationResponse(page = page, size = songs.size, data = songs))
            )
        } else {
            val timerKey = "${TimerKey.THEMES}songs_$page"
            val needsUpdate = timers.needsUpdate(key = timerKey, amount = 7, unit = TimeUnit.DAY)

            if (needsUpdate) {
                songsDirectory.deleteMany(Filters.eq("page_cache", page))

                val raw = RestClient.request(
                    "${BaseUrls.ANIME_THEMES}song?include=$songInclude&page[number]=$page&page[size]=$size",
                    SongSearch.serializer()
                )
                val songs = raw.songs?.map { it.toSongEntity() } ?: emptyList()

                val documents = parseDataToDocuments(songs, SongEntity.serializer())
                    .onEach { it.append("page_cache", page) }

                if (documents.isNotEmpty()) songsDirectory.insertMany(documents)
                timers.update(timerKey)

                call.respond(
                    HttpStatusCode.OK,
                    Json.encodeToString(PaginationResponse(page = page, size = songs.size, data = songs))
                )
            } else {
                val skipCount = (page - 1) * size

                val songs = songsDirectory
                    .find(Filters.eq("page_cache", page))
                    .skip(skipCount)
                    .limit(size)
                    .toList()
                    .map { documentToSongEntity(it) }

                call.respond(
                    HttpStatusCode.OK,
                    Json.encodeToString(PaginationResponse(page = page, size = songs.size, data = songs))
                )
            }
        }
    } catch (ex: Exception) {
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(ex.message ?: ErrorMessages.InvalidInput.message))
    }
}
