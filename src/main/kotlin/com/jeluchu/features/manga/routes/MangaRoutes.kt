package com.jeluchu.features.manga.routes

import com.jeluchu.core.extensions.getToJson
import com.jeluchu.core.utils.Routes
import com.jeluchu.features.manga.services.MangaService
import com.mongodb.client.MongoDatabase
import io.ktor.server.routing.*

fun Route.mangaEndpoints(
    mongoDatabase: MongoDatabase,
    service: MangaService = MangaService(mongoDatabase)
) = route(Routes.MANGA) {
    getToJson { service.getManga(call) }
    getToJson(Routes.RANDOM) { service.getRandomManga(call) }
    getToJson(Routes.DIRECTORY) { service.getDirectory(call) }
    getToJson(Routes.ID) { service.getMangaByMalId(call) }
}
