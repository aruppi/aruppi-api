package com.jeluchu.features.anime.services

import com.jeluchu.core.enums.AnimeStatusTypes
import com.jeluchu.core.enums.AnimeTypes
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.documentToSimpleAnimeEntity
import com.jeluchu.core.utils.Collections
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

class TagsService(
    private val database: MongoDatabase,
    private val directory: MongoCollection<Document> = database.getCollection(Collections.ANIME_DIRECTORY)
) {
    suspend fun getAnimeByAnyTag(call: RoutingCall) {
        val tags = call.request.queryParameters["tags"].orEmpty()
        val nsfw = call.request.queryParameters["nsfw"].toBoolean()

        val tagsList = if (tags.isNotEmpty()) {
            tags.split(",").map { it.trim() }
        } else emptyList()

        if (tagsList.isEmpty()) {
            return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.MissingTags.message)
        }

        val filters = mutableListOf<org.bson.conversions.Bson>().apply {
            add(Filters.or(
                Filters.`in`("tags.es", tagsList),
                Filters.`in`("tags.en", tagsList)
            ))

            add(Filters.`in`("status", listOf(AnimeStatusTypes.FINISHED, AnimeStatusTypes.ONGOING)))

            add(Filters.ne("type", AnimeTypes.MUSIC))
            add(Filters.ne("type", AnimeTypes.PV))
            add(Filters.ne("type", AnimeTypes.CM))

            if (!nsfw) add(Filters.eq("nsfw", false))
        }

        val query = directory.find(Filters.and(filters))
            .toList()
            .map { documentToSimpleAnimeEntity(it) }

        call.respond(HttpStatusCode.OK, Json.encodeToString(query))
    }
}
