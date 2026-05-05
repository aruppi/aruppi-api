package com.jeluchu.core.configuration

import com.jeluchu.features.anime.routes.animeEndpoints
import com.jeluchu.features.anitakume.routes.anitakumeEndpoints
import com.jeluchu.features.gallery.routes.galleryEndpoints
import com.jeluchu.features.news.routes.newsEndpoints
import com.jeluchu.features.preload.routes.preloadEndpoints
import com.jeluchu.features.radiostations.routes.radioStationsEndpoints
import com.jeluchu.features.rankings.routes.rankingsEndpoints
import com.jeluchu.features.schedule.routes.scheduleEndpoints
import com.jeluchu.features.themes.services.AnimeThemesService
import com.jeluchu.features.themes.routes.themesEndpoints
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.DocumentationLinks
import com.jeluchu.core.models.ErrorResponse
import com.mongodb.client.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.initRoutes(
    mongoDatabase: MongoDatabase = connectToMongoDB()
) = routing {
    route("api/v5") {
        initDocumentation()
        newsEndpoints(mongoDatabase)
        animeEndpoints(mongoDatabase)
        themesEndpoints(mongoDatabase)
        galleryEndpoints(mongoDatabase)
        rankingsEndpoints(mongoDatabase)
        scheduleEndpoints(mongoDatabase)
        anitakumeEndpoints(mongoDatabase)
        radioStationsEndpoints(mongoDatabase)
        preloadEndpoints(mongoDatabase)

        route("{path...}") {
            handle {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        error = notFoundMessage(call.request.path()),
                        status = HttpStatusCode.NotFound.value,
                        path = call.request.path(),
                        version = ApiMetadata.version,
                        documentation = DocumentationLinks(
                            redoc = ApiMetadata.docsPath,
                            swagger = ApiMetadata.swaggerPath,
                            openapi = ApiMetadata.openApiPath
                        )
                    )
                )
            }
        }
    }
}

private fun notFoundMessage(path: String): String {
    return when {
        path.startsWith("/api/v5/themes/songs") -> ErrorMessages.InvalidThemesSongsEndpoint.message
        path.startsWith("/api/v5/themes/anime") -> ErrorMessages.InvalidThemesAnimeEndpoint.message
        path.startsWith("/api/v5/themes/artists") -> ErrorMessages.InvalidThemesArtistsEndpoint.message
        else -> ErrorMessages.NotFound.message
    }
}
