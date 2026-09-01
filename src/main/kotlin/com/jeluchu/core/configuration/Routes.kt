package com.jeluchu.core.configuration

import com.jeluchu.core.extensions.respondError
import com.jeluchu.features.anime.routes.animeEndpoints
import com.jeluchu.features.anitakume.routes.anitakumeEndpoints
import com.jeluchu.features.gallery.routes.galleryEndpoints
import com.jeluchu.features.manga.routes.mangaEndpoints
import com.jeluchu.features.news.routes.newsEndpoints
import com.jeluchu.features.preload.routes.preloadEndpoints
import com.jeluchu.features.radiostations.routes.radioStationsEndpoints
import com.jeluchu.features.rankings.routes.rankingsEndpoints
import com.jeluchu.features.schedule.routes.scheduleEndpoints
import com.jeluchu.features.search.routes.searchEndpoints
import com.jeluchu.features.themes.services.AnimeThemesService
import com.jeluchu.features.themes.routes.themesEndpoints
import com.jeluchu.core.messages.ErrorMessages
import com.mongodb.client.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.initRoutes(
    mongoDatabase: MongoDatabase = connectToMongoDB()
) = routing {
    route("api/v5") {
        initDocumentation()

        newsEndpoints(mongoDatabase)
        animeEndpoints(mongoDatabase)
        mangaEndpoints(mongoDatabase)
        searchEndpoints()
        themesEndpoints(mongoDatabase)
        galleryEndpoints(mongoDatabase)
        rankingsEndpoints(mongoDatabase)
        scheduleEndpoints(mongoDatabase)
        anitakumeEndpoints(mongoDatabase)
        radioStationsEndpoints(mongoDatabase)
        preloadEndpoints(mongoDatabase)

        route("{path...}") {
            handle {
                call.respondError(
                    status = HttpStatusCode.NotFound,
                    message = notFoundMessage(call.request.path()),
                    code = notFoundCode(call.request.path())
                )
            }
        }
    }
}

private fun notFoundMessage(path: String): String {
    return when {
        path.startsWith("/api/v5/news") -> ErrorMessages.InvalidNewsEndpoint.message
        path.startsWith("/api/v5/anime") -> ErrorMessages.InvalidAnimeEndpoint.message
        path.startsWith("/api/v5/manga") -> ErrorMessages.InvalidMangaEndpoint.message
        path.startsWith("/api/v5/search") -> ErrorMessages.InvalidSearchEndpoint.message
        path.startsWith("/api/v5/themes/songs") -> ErrorMessages.InvalidThemesSongsEndpoint.message
        path.startsWith("/api/v5/themes/anime") -> ErrorMessages.InvalidThemesAnimeEndpoint.message
        path.startsWith("/api/v5/themes/artists") -> ErrorMessages.InvalidThemesArtistsEndpoint.message
        path.startsWith("/api/v5/themes") -> ErrorMessages.InvalidThemesEndpoint.message
        path.startsWith("/api/v5/gallery") -> ErrorMessages.InvalidGalleryEndpoint.message
        path.startsWith("/api/v5/top") -> ErrorMessages.InvalidTopEndpoint.message
        path.startsWith("/api/v5/schedule") -> ErrorMessages.InvalidScheduleEndpoint.message
        path.startsWith("/api/v5/anitakume") -> ErrorMessages.InvalidAnitakumeEndpoint.message
        path.startsWith("/api/v5/radio") -> ErrorMessages.InvalidRadioEndpoint.message
        else -> ErrorMessages.NotFound.message
    }
}

private fun notFoundCode(path: String): String {
    return when {
        path.startsWith("/api/v5/news") -> "INVALID_NEWS_ENDPOINT"
        path.startsWith("/api/v5/anime") -> "INVALID_ANIME_ENDPOINT"
        path.startsWith("/api/v5/manga") -> "INVALID_MANGA_ENDPOINT"
        path.startsWith("/api/v5/search") -> "INVALID_SEARCH_ENDPOINT"
        path.startsWith("/api/v5/themes/songs") -> "INVALID_THEMES_SONGS_ENDPOINT"
        path.startsWith("/api/v5/themes/anime") -> "INVALID_THEMES_ANIME_ENDPOINT"
        path.startsWith("/api/v5/themes/artists") -> "INVALID_THEMES_ARTISTS_ENDPOINT"
        path.startsWith("/api/v5/themes") -> "INVALID_THEMES_ENDPOINT"
        path.startsWith("/api/v5/gallery") -> "INVALID_GALLERY_ENDPOINT"
        path.startsWith("/api/v5/top") -> "INVALID_RANKINGS_ENDPOINT"
        path.startsWith("/api/v5/schedule") -> "INVALID_SCHEDULE_ENDPOINT"
        path.startsWith("/api/v5/anitakume") -> "INVALID_ANITAKUME_ENDPOINT"
        path.startsWith("/api/v5/radio") -> "INVALID_RADIO_ENDPOINT"
        else -> "ENDPOINT_NOT_FOUND"
    }
}
