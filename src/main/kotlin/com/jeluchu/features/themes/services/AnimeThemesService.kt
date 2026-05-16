package com.jeluchu.features.themes.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.extensions.*
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.Timers
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimesThemeEntity
import com.jeluchu.features.anime.mappers.documentToSimpleArtistEntity
import com.jeluchu.features.anime.mappers.documentToSongEntity
import com.jeluchu.features.themes.models.artist.*
import com.jeluchu.features.themes.models.artist.AnimeThemeDetail.Companion.toAnimeThemeDetail
import com.jeluchu.features.themes.models.artist.ArtistEntity.Companion.toArtistEntity
import com.jeluchu.features.themes.models.artist.SimpleArtistEntity.Companion.toSimpleArtistEntity
import com.jeluchu.features.themes.models.song.SongEntity
import com.jeluchu.features.themes.models.song.SongEntity.Companion.toSongEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

class AnimeThemesService(
    private val database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val themesDirectory = database.getCollection(Collections.ANIME_THEMES)
    private val artistsDirectory = database.getCollection(Collections.ARTISTS_INDEX)
    private val songsDirectory = database.getCollection(Collections.SONGS_INDEX)
    private val animeThemeDetail = database.getCollection(Collections.ANIME_THEMES_DETAIL)

    private val artistInclude = "songs.animethemes.anime,songs.animethemes.animethemeentries.videos,images"
    private val songInclude = "artists,animethemes.anime"
    private val animeDetailInclude = "animethemes.song,animethemes.animethemeentries.videos,images"

    suspend fun getAnimeThemes(call: RoutingCall) {
        val page = call.getIntSafeQueryParam(key = "page", defaultValue = 1)
        val size = call.getIntSafeQueryParam(key = "size", defaultValue = 25)
        if (page < 1 || size < 1) return call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

        val elements = themesDirectory
            .find()
            .skip((page - 1) * size)
            .limit(size)
            .toList()
            .map { documentToAnimesThemeEntity(it) }

        call.respond(
            status = HttpStatusCode.OK,
            message = PaginationResponse(
                page = page,
                data = elements,
                size = elements.size
            ).toJson()
        )
    }

    suspend fun getAnimeThemeBySlug(call: RoutingCall) {
        val slug = call.getStringSafeParam(key = "slug").toAnimeThemesSlug().ifBlank {
            return call.respondError(
                status = HttpStatusCode.BadRequest,
                message = ErrorMessages.InvalidAnimeThemeSlug.message
            )
        }

        if (database.isUpdate(timer = Timers.ANIME_THEMES, slug = slug)) {
            animeThemeDetail.deleteMany(Filters.eq("slug", slug))

            val raw = RestClient.request(
                deserializer = AnimeThemeShow.serializer(),
                url = "${BaseUrls.ANIME_THEMES}anime/$slug?include=$animeDetailInclude"
            ).anime?.toAnimeThemeDetail() ?: return call.respondError(
                status = HttpStatusCode.NotFound,
                message = ErrorMessages.InvalidAnimeThemeSlug.message
            )

            parseDataToDocuments(
                data = listOf(raw),
                serializer = AnimeThemeDetail.serializer()
            ).apply {
                if (isNotEmpty()) animeThemeDetail.insertMany(this)
                timers.update(key = "${Timers.ANIME_THEMES.key}_slug_$slug")

                call.respond(
                    status = HttpStatusCode.OK,
                    message = map { documentToAnimeThemeDetail(it) }.toJson()
                )
            }
        } else {
            val cached = animeThemeDetail.find(Filters.eq("slug", slug)).firstOrNull()
                ?: return call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidAnimeThemeSlug.message)

            call.respond(
                status = HttpStatusCode.OK,
                message = documentToAnimeThemeDetail(cached).toJson()
            )
        }
    }

    // ── GET /api/v5/themes/anime/{slug}/random ────────────────────────────────

    suspend fun getRandomAnimeTheme(call: RoutingCall) {
        try {
            val slug = call.getStringSafeParam("slug").toAnimeThemesSlug()

            if (slug.isBlank()) {
                return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidAnimeThemeSlug.message)
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
                    ?: return call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidAnimeThemeSlug.message)
            }

            val randomTheme = detail.themes?.randomOrNull()
                ?: return call.respondError(HttpStatusCode.NotFound, ErrorMessages.NotFoundContent.message)

            call.response.headers.append("Cache-Control", "no-store")
            call.respond(HttpStatusCode.OK, Json.encodeToString(randomTheme))
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.InvalidAnimeThemeSlug.message)
        }
    }

    suspend fun getRandomSong(call: RoutingCall) {
        try {
            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}song?include=$songInclude&page[number]=1&page[size]=1&sort=random",
                SongSearch.serializer()
            )

            val song = raw.songs?.firstOrNull()?.toSongEntity()
                ?: return call.respondError(HttpStatusCode.NotFound, ErrorMessages.NotFoundContent.message)

            call.response.headers.append("Cache-Control", "no-store")
            call.respond(HttpStatusCode.OK, Json.encodeToString(song.withVideoLinks()))
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, ErrorMessages.InvalidInput.message)
        }
    }

    suspend fun getArtists(call: RoutingCall) {
        try {
            val page = call.getIntSafeQueryParam("page", 1)
            val size = call.getIntSafeQueryParam("size", 25)

            if (page < 1 || size < 1) return call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

            val timerKey = "${TimerKey.THEMES}artists_$page"
            val needsUpdate = timers.needsUpdate(key = timerKey, amount = 7, unit = TimeUnit.DAY)

            if (needsUpdate) {
                artistsDirectory.deleteMany(Filters.eq("page_cache", page))

                val raw = RestClient.request(
                    "${BaseUrls.ANIME_THEMES}artist?include=$artistInclude&page[number]=$page&page[size]=$size",
                    ArtistSearch.serializer()
                )
                val artists = raw.artists?.map { it.toSimpleArtistEntity() } ?: emptyList()

                val documents = parseDataToDocuments(artists, SimpleArtistEntity.serializer())
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
                    .limit(size)
                    .toList()
                    .map { documentToSimpleArtistEntity(it) }

                call.respond(
                    HttpStatusCode.OK,
                    Json.encodeToString(PaginationResponse(page = page, size = artists.size, data = artists))
                )
            }
        } catch (ex: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, ex.message ?: ErrorMessages.InvalidInput.message)
        }
    }

    suspend fun getArtistBySlug(call: RoutingCall) {
        try {
            val slug = call.getStringSafeParam("slug")

            if (slug.isBlank()) {
                return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.ArtistNotFound.message)
            }

            val raw = RestClient.request(
                "${BaseUrls.ANIME_THEMES}artist/$slug?include=$artistInclude",
                ArtistShow.serializer()
            )

            val artist = raw.artist?.toArtistEntity()
                ?: return call.respondError(HttpStatusCode.NotFound, ErrorMessages.ArtistNotFound.message)

            artistsDirectory.deleteMany(Filters.eq("slug", slug))
            val documents = parseDataToDocuments(listOf(artist), ArtistEntity.serializer())
            if (documents.isNotEmpty()) artistsDirectory.insertMany(documents)

            call.respond(HttpStatusCode.OK, Json.encodeToString(artist))
        } catch (_: Exception) {
            call.respondError(HttpStatusCode.NotFound, ErrorMessages.ArtistNotFound.message)
        }
    }

    suspend fun searchSongs(call: RoutingCall) {
        try {
            val query = call.getStringSafeQueryParam("q")
            val page = call.getIntSafeQueryParam("page", 1)
            val size = call.getIntSafeQueryParam("size", 25)

            if (page < 1 || size < 1) return call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)

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
                    val songs = songsDirectory
                        .find(Filters.eq("page_cache", page))
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
            call.respondError(HttpStatusCode.InternalServerError, ex.message ?: ErrorMessages.InvalidInput.message)
        }
    }

    private suspend fun SongEntity.withVideoLinks(): SongEntity {
        val updatedThemes = themes?.map { theme ->
            if (!theme.videoLink.isNullOrBlank()) return@map theme

            val animeSlug = theme.animeSlug ?: return@map theme
            val anime = RestClient.request(
                "${BaseUrls.ANIME_THEMES}anime/$animeSlug?include=$animeDetailInclude",
                AnimeThemeShow.serializer()
            ).anime?.toAnimeThemeDetail() ?: return@map theme

            val matchingTheme = anime.themes?.firstOrNull { animeTheme ->
                animeTheme.slug == theme.slug &&
                        animeTheme.type == theme.type &&
                        (theme.sequence == null || animeTheme.sequence == theme.sequence)
            }

            theme.copy(videoLink = matchingTheme?.video?.link)
        }

        return copy(themes = updatedThemes)
    }
}
