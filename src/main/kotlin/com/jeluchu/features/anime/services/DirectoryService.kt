package com.jeluchu.features.anime.services

import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseAnimeType
import com.jeluchu.core.extensions.*
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.getLocalData
import com.jeluchu.core.utils.getRemoteData
import com.jeluchu.features.anime.mappers.documentToAnimeDirectoryEntity
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

class DirectoryService(
    private val database: MongoDatabase,
    private val timers: MongoCollection<Document> = database.getCollection(Collections.TIMERS),
    private val directory: MongoCollection<Document> = database.getCollection(Collections.ANIME_DIRECTORY)
) {
    suspend fun getAnimeByType(call: RoutingCall) {
        val param = call.getStringSafeParam("type").uppercase()
        val page = call.getIntSafeQueryParam("page", 1)
        val size = call.getIntSafeQueryParam("size", 10)

        val skipCount = (page - 1) * size
        val timerKey = "${TimerKey.ANIME_TYPE}${param.lowercase()}"
        val collection = database.getCollection(timerKey)
        if (page < 1 || size < 1) return call.badRequestError(ErrorMessages.InvalidSizeAndPage.message)
        if (parseAnimeType(param) == null) return call.badRequestError(ErrorMessages.InvalidAnimeType.message)

        if (timers.needsUpdate(timerKey, 30, TimeUnit.DAY)) {
            getRemoteData(
                newCollection = collection,
                remoteCollection = directory,
                mapper = { documentToAnimeDirectoryEntity(it) },
                filters = Filters.eq("type", param),
                onQuerySuccess = { data ->
                    val documents = data.map { Document.parse(Json.encodeToString(it)) }
                    if (documents.isNotEmpty()) collection.insertMany(documents)
                    timers.update(timerKey)
                }
            )
        }

        getLocalData(
            page = page,
            size = size,
            skipCount = skipCount,
            collection = collection,
            mapper = { documentToAnimeDirectoryEntity(it) },
            onQuerySuccess = { data -> call.respond(HttpStatusCode.OK, Json.encodeToString(data)) }
        )
    }
}
