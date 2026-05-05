package com.jeluchu.features.themes.routes

import com.jeluchu.core.extensions.getToJson
import com.jeluchu.core.utils.Path
import com.jeluchu.core.utils.Routes
import com.jeluchu.features.themes.services.AnimeThemesService
import com.mongodb.client.MongoDatabase
import io.ktor.server.routing.*

fun Route.themesEndpoints(
    mongoDatabase: MongoDatabase,
    service: AnimeThemesService = AnimeThemesService(mongoDatabase)
) = route(path = Routes.THEMES) {
        route(path = Routes.ANIME) {
            getToJson { service.getAnimeThemes(call) }
            getToJson(Routes.SLUG) { service.getAnimeThemeBySlug(call) }
            getToJson("${Routes.SLUG}${Routes.RANDOM}") { service.getRandomAnimeTheme(call) }
        }

        route(Routes.ARTISTS) {
            getToJson { service.getArtists(call) }
            getToJson(Routes.SLUG) { service.getArtistBySlug(call) }
        }

        route(Routes.SONGS) {
            getToJson(Path.RANDOM) { service.getRandomSong(call) }
            getToJson { service.searchSongs(call) }
        }
}
