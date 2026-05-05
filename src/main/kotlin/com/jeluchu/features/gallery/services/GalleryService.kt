package com.jeluchu.features.gallery.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.update
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.Endpoints
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.gallery.mappers.documentToProcessedPost
import com.jeluchu.features.gallery.mappers.toProcessedPost
import com.jeluchu.features.gallery.mappers.toProcessedPostQuery
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.IOException
import java.net.URLEncoder

class GalleryService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val queryPosts = database.getCollection(Collections.ANIME_PICTURES_QUERY)
    private val recentPosts = database.getCollection(Collections.ANIME_PICTURES_RECENT)

    suspend fun getLastPosts(call: RoutingCall) {
        val size = 80
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val timerKey = "${Collections.ANIME_PICTURES_RECENT}_${page}"
        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            recentPosts.deleteMany(Filters.and(Filters.eq("page", page)))
            val response = RestClient.request(
                BaseUrls.ANIME_PICTURES + Endpoints.POSTS + "?page=$page",
                PostsResponse.serializer()
            ).posts.map { it.toProcessedPost(page) }

            val documentsToInsert = parseDataToDocuments(response, ProcessedPost.serializer())
            if (documentsToInsert.isNotEmpty()) recentPosts.insertMany(documentsToInsert)
            timers.update(timerKey)

            val elements = documentsToInsert.map { documentToProcessedPost(it) }

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
        } else {
            val posts = recentPosts
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()

            val elements = posts.map { documentToProcessedPost(it) }
            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }

    suspend fun getQueryImages(call: RoutingCall) {
        val size = 80
        val query = call.request.queryParameters["query"].orEmpty()
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val animePicturesPage = page - 1
        val timerKey = "${Collections.ANIME_PICTURES_QUERY}_${query}_${page}"

        val needsUpdate = timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            queryPosts.deleteMany(Filters.and(Filters.eq("page", page)))

            try {
                val response = RestClient.request(
                    BaseUrls.ANIME_PICTURES + Endpoints.POSTS + "?search_tag=${URLEncoder.encode(query, "UTF-8")}" + "&lang=en&type=json_v3" + "&page=$animePicturesPage",
                    PostsResponse.serializer()
                ).posts.map { it.toProcessedPostQuery(page, query) }

                val documentsToInsert = parseDataToDocuments(response, ProcessedPost.serializer())
                if (documentsToInsert.isNotEmpty()) queryPosts.insertMany(documentsToInsert)
                timers.update(timerKey)

                val elements = documentsToInsert.map { documentToProcessedPost(it) }

                val paginationResponse = PaginationResponse(
                    page = page,
                    size = size,
                    data = elements
                )

                call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
            } catch (e: IOException) {
                val paginationResponse = PaginationResponse(
                    page = page,
                    size = size,
                    data = emptyList<ProcessedPost>()
                )

                call.respond(HttpStatusCode.OK, Json.encodeToString(paginationResponse))
            }
        } else {
            val posts = queryPosts
                .find(Filters.and(Filters.eq("page", page)))
                .limit(size)
                .toList()

            val elements = posts.map { documentToProcessedPost(it) }
            val response = PaginationResponse(
                page = page,
                size = size,
                data = elements
            )

            call.respond(HttpStatusCode.OK, Json.encodeToString(response))
        }
    }
}
