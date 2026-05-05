package com.jeluchu.features.preload.routes

import com.jeluchu.core.extensions.getToJson
import com.jeluchu.features.preload.services.PreloadService
import com.mongodb.client.MongoDatabase
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.preloadEndpoints(
    mongoDatabase: MongoDatabase,
    service: PreloadService = PreloadService(mongoDatabase)
) = route("/preload") {
    getToJson {
        val force = call.request.queryParameters["force"]?.toBooleanStrictOrNull() ?: false
        val response = service.preload(force)
        val status = if (response.failed > 0) HttpStatusCode.MultiStatus else HttpStatusCode.OK
        call.respond(status, Json.encodeToString(response))
    }
}
