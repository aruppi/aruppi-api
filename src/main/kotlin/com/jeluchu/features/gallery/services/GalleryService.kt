package com.jeluchu.features.gallery.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.GalleryProvider
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseGalleryProvider
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.parseSfwPreference
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.toJson
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.PaginationResponse
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.Endpoints
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.gallery.mappers.documentToProcessedPost
import com.jeluchu.features.gallery.mappers.toProcessedPost
import com.jeluchu.features.gallery.mappers.toProcessedPostQuery
import com.jeluchu.features.gallery.models.DanbooruPost
import com.jeluchu.features.gallery.models.DanbooruTag
import com.jeluchu.features.gallery.models.GalleryTagSuggestion
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import com.jeluchu.features.gallery.models.SafebooruPost
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.util.Base64
import kotlin.random.Random

class GalleryService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val queryPosts = database.getCollection(Collections.ANIME_PICTURES_QUERY)
    private val recentPosts = database.getCollection(Collections.ANIME_PICTURES_RECENT)
    private val danbooruLogin = System.getenv("DANBOORU_LOGIN").orEmpty()
    private val danbooruApiKey = System.getenv("DANBOORU_API_KEY").orEmpty()
    private val danbooruBaseUrl = System.getenv("DANBOORU_BASE_URL").takeUnless { it.isNullOrBlank() }
        ?: BaseUrls.DANBOORU
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLastPosts(call: RoutingCall) {
        val size = 80
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val includeNsfw = parseGalleryNsfw(call) ?: return
        val provider = parseGalleryProvider(call.request.queryParameters["provider"])
        val timerKey = "${Collections.ANIME_PICTURES_RECENT}_${provider.value}_${page}_${includeNsfw}"
        val filters = Filters.and(
            Filters.eq("page", page),
            Filters.eq("provider", provider.value),
            Filters.eq("includeNsfw", includeNsfw)
        )
        val cachedPosts = recentPosts
            .find(filters)
            .limit(size)
            .toList()
        val needsUpdate = cachedPosts.isEmpty() || timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            recentPosts.deleteMany(filters)

            val fetched = when (provider) {
                GalleryProvider.ANIME_PICTURES -> fetchAnimePicturesRecent(page)
                GalleryProvider.DANBOORU -> fetchDanbooruRecent(page, size, includeNsfw)
                GalleryProvider.SAFEBOORU -> fetchSafebooruRecent(page, size)
            }

            val documentsToInsert = parseDataToDocuments(fetched.data, ProcessedPost.serializer()).onEach {
                it.append("includeNsfw", includeNsfw)
            }
            if (documentsToInsert.isNotEmpty()) recentPosts.insertMany(documentsToInsert)
            if (documentsToInsert.isNotEmpty()) timers.update(timerKey)

            val elements = documentsToInsert
                .map { documentToProcessedPost(it) }
                .filterByNsfw(includeNsfw)

            val paginationResponse = PaginationResponse(
                page = page,
                size = size,
                totalPages = fetched.totalPages,
                totalItems = fetched.totalItems,
                data = elements
            )

            call.respond(HttpStatusCode.OK, paginationResponse.toJson())
        } else {
            val elements = cachedPosts
                .map { documentToProcessedPost(it) }
                .filterByNsfw(includeNsfw)
            val first = cachedPosts.firstOrNull()
            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, response.toJson())
        }
    }

    suspend fun getQueryImages(call: RoutingCall) {
        val size = 80
        val query = call.request.queryParameters["query"].orEmpty().trim()
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val includeNsfw = parseGalleryNsfw(call) ?: return
        val provider = parseGalleryProvider(call.request.queryParameters["provider"])
        val timerKey = "${Collections.ANIME_PICTURES_QUERY}_${provider.value}_${query}_${page}_${includeNsfw}"
        val filters = Filters.and(
            Filters.eq("page", page),
            Filters.eq("query", query),
            Filters.eq("provider", provider.value),
            Filters.eq("includeNsfw", includeNsfw)
        )
        val cachedPosts = queryPosts
            .find(filters)
            .limit(size)
            .toList()

        val needsUpdate = cachedPosts.isEmpty() || timers.needsUpdate(
            amount = 30,
            key = timerKey,
            unit = TimeUnit.DAY
        )

        if (needsUpdate) {
            queryPosts.deleteMany(filters)

            try {
                val fetched = when (provider) {
                    GalleryProvider.ANIME_PICTURES -> fetchAnimePicturesQuery(query, page)
                    GalleryProvider.DANBOORU -> fetchDanbooruQuery(query, page, size, includeNsfw)
                    GalleryProvider.SAFEBOORU -> fetchSafebooruQuery(query, page, size)
                }

                val documentsToInsert = parseDataToDocuments(fetched.data, ProcessedPost.serializer()).onEach {
                    it.append("includeNsfw", includeNsfw)
                }
                if (documentsToInsert.isNotEmpty()) queryPosts.insertMany(documentsToInsert)
                timers.update(timerKey)

                val elements = documentsToInsert
                    .map { documentToProcessedPost(it) }
                    .filterByNsfw(includeNsfw)

                val paginationResponse = PaginationResponse(
                    page = page,
                    size = size,
                    totalPages = fetched.totalPages,
                    totalItems = fetched.totalItems,
                    data = elements
                )

                call.respond(HttpStatusCode.OK, paginationResponse.toJson())
            } catch (e: Exception) {
                val paginationResponse = PaginationResponse(
                    page = page,
                    size = size,
                    totalPages = 0,
                    totalItems = 0,
                    data = emptyList<ProcessedPost>()
                )

                call.respond(HttpStatusCode.OK, paginationResponse.toJson())
            }
        } else {
            val elements = cachedPosts
                .map { documentToProcessedPost(it) }
                .filterByNsfw(includeNsfw)
            val first = cachedPosts.firstOrNull()
            val response = PaginationResponse(
                page = page,
                size = size,
                totalPages = first?.getInteger("totalPages", 0) ?: 0,
                totalItems = first?.getInteger("totalItems", 0) ?: 0,
                data = elements
            )

            call.respond(HttpStatusCode.OK, response.toJson())
        }
    }

    suspend fun getTrendingPosts(call: RoutingCall) {
        val size = 80
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val includeNsfw = parseGalleryNsfw(call) ?: return
        val provider = parseGalleryProvider(call.request.queryParameters["provider"])
        val fetched = when (provider) {
            GalleryProvider.DANBOORU -> fetchDanbooruRecent(page, size, includeNsfw)
            GalleryProvider.ANIME_PICTURES -> fetchAnimePicturesRecent(page)
            GalleryProvider.SAFEBOORU -> fetchSafebooruRecent(page, size)
        }
        val posts = fetched.data
            .filterByNsfw(includeNsfw)
            .sortedByDescending(ProcessedPost::score)

        call.respond(
            HttpStatusCode.OK,
            PaginationResponse(
                page = page,
                size = size,
                totalPages = fetched.totalPages,
                totalItems = fetched.totalItems,
                data = posts
            ).toJson()
        )
    }

    suspend fun getRandomPost(call: RoutingCall) {
        val size = 80
        val includeNsfw = parseGalleryNsfw(call) ?: return
        val provider = parseGalleryProvider(call.request.queryParameters["provider"])
        val query = call.request.queryParameters["query"].orEmpty().trim()
        val page = Random.nextInt(1, 11)
        val fetched = if (query.isBlank()) {
            when (provider) {
                GalleryProvider.ANIME_PICTURES -> fetchAnimePicturesRecent(page)
                GalleryProvider.DANBOORU -> fetchDanbooruRecent(page, size, includeNsfw)
                GalleryProvider.SAFEBOORU -> fetchSafebooruRecent(page, size)
            }
        } else {
            fetchProviderQuery(provider, query, page, size, includeNsfw)
        }
        val randomPost = fetched.data.filterByNsfw(includeNsfw).randomOrNull()
        val data = randomPost?.let(::listOf).orEmpty()

        call.respond(
            HttpStatusCode.OK,
            PaginationResponse(page = 1, size = 1, totalPages = 1, totalItems = data.size, data = data).toJson()
        )
    }

    suspend fun getRelatedPosts(call: RoutingCall) {
        val size = 80
        val includeNsfw = parseGalleryNsfw(call) ?: return
        val provider = parseGalleryProvider(call.request.queryParameters["provider"])
        val page = (call.request.queryParameters["page"]?.toIntOrNull() ?: 1).coerceAtLeast(1)
        val tags = call.request.queryParameters["tags"].orEmpty()
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
            .take(4)
        if (tags.isEmpty()) {
            call.respondError(
                status = HttpStatusCode.BadRequest,
                message = "At least one tag is required",
                code = "GALLERY_TAGS_REQUIRED"
            )
            return
        }

        val fetched = fetchProviderQuery(provider, tags.joinToString(","), page, size, includeNsfw)
        call.respond(
            HttpStatusCode.OK,
            PaginationResponse(
                page = page,
                size = size,
                totalPages = fetched.totalPages,
                totalItems = fetched.totalItems,
                data = fetched.data.filterByNsfw(includeNsfw)
            ).toJson()
        )
    }

    suspend fun getTagSuggestions(call: RoutingCall) {
        val query = call.request.queryParameters["query"].orEmpty().trim()
        val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 12).coerceIn(1, 30)
        if (query.length < 2) {
            call.respond(HttpStatusCode.OK, emptyList<GalleryTagSuggestion>().toJson())
            return
        }
        val encodedQuery = URLEncoder.encode("${normalizeDanbooruQuery(query)}*", "UTF-8")
        val tags = RestClient.request(
            "$danbooruBaseUrl/tags.json?search%5Bname_matches%5D=$encodedQuery&search%5Border%5D=count&limit=$limit",
            ListSerializer(DanbooruTag.serializer())
        ) {
            applyDanbooruAuth()
        }
        val suggestions = tags
            .filter { it.name.isNotBlank() }
            .map { GalleryTagSuggestion(name = it.name, postCount = it.postCount, category = it.category) }
        call.respond(HttpStatusCode.OK, suggestions.toJson())
    }

    private suspend fun fetchProviderQuery(
        provider: GalleryProvider,
        query: String,
        page: Int,
        size: Int,
        includeNsfw: Boolean
    ): GalleryFetchResult = when (provider) {
        GalleryProvider.ANIME_PICTURES -> fetchAnimePicturesQuery(query, page)
        GalleryProvider.DANBOORU -> fetchDanbooruQuery(query, page, size, includeNsfw)
        GalleryProvider.SAFEBOORU -> fetchSafebooruQuery(query, page, size)
    }

    private suspend fun fetchAnimePicturesRecent(page: Int): GalleryFetchResult {
        val rawResponse = RestClient.request(
            BaseUrls.ANIME_PICTURES + Endpoints.POSTS + "?page=$page",
            PostsResponse.serializer()
        )

        return GalleryFetchResult(
            totalPages = rawResponse.maxPages,
            totalItems = rawResponse.postsCount,
            data = rawResponse.posts.map { it.toProcessedPost(page, rawResponse.maxPages, rawResponse.postsCount) }
        )
    }

    private suspend fun fetchAnimePicturesQuery(query: String, page: Int): GalleryFetchResult {
        val animePicturesPage = page - 1
        val rawResponse = RestClient.request(
            BaseUrls.ANIME_PICTURES + Endpoints.POSTS +
                "?search_tag=${URLEncoder.encode(query, "UTF-8")}&lang=en&type=json_v3&page=$animePicturesPage",
            PostsResponse.serializer()
        )

        return GalleryFetchResult(
            totalPages = rawResponse.maxPages,
            totalItems = rawResponse.postsCount,
            data = rawResponse.posts.map { it.toProcessedPostQuery(page, query, rawResponse.maxPages, rawResponse.postsCount) }
        )
    }

    private suspend fun fetchDanbooruRecent(page: Int, size: Int, includeNsfw: Boolean): GalleryFetchResult {
        val safeTags = if (includeNsfw) "" else "&tags=rating%3Asafe"
        val posts = fetchDanbooruPosts(
            "$danbooruBaseUrl/posts.json?page=$page&limit=$size$safeTags"
        )

        return GalleryFetchResult(
            totalPages = 0,
            totalItems = 0,
            data = posts.map { it.toProcessedPost(page = page) }
        )
    }

    private suspend fun fetchDanbooruQuery(query: String, page: Int, size: Int, includeNsfw: Boolean): GalleryFetchResult {
        val normalizedQuery = normalizeDanbooruQuery(query)
        val tags = if (includeNsfw) normalizedQuery else listOf("rating:safe", normalizedQuery).filter { it.isNotBlank() }.joinToString(" ")
        val encodedQuery = URLEncoder.encode(tags, "UTF-8")
        val posts = fetchDanbooruPosts(
            "$danbooruBaseUrl/posts.json?page=$page&limit=$size&tags=$encodedQuery"
        )

        return GalleryFetchResult(
            totalPages = 0,
            totalItems = 0,
            data = posts.map { it.toProcessedPost(page = page, query = query) }
        )
    }

    private suspend fun fetchSafebooruRecent(page: Int, size: Int): GalleryFetchResult {
        val posts = fetchSafebooruPosts(page = page, size = size)

        return GalleryFetchResult(
            totalPages = 0,
            totalItems = 0,
            data = posts.map { it.toProcessedPost(page = page) }
        )
    }

    private suspend fun fetchDanbooruPosts(url: String): List<DanbooruPost> {
        val raw = RestClient.request(url, JsonElement.serializer()) {
            applyDanbooruAuth()
        }
        return when (raw) {
            is JsonArray -> json.decodeFromJsonElement(ListSerializer(DanbooruPost.serializer()), raw)
            is JsonObject -> {
                val message = raw.stringValue("message")
                    .ifBlank { raw.stringValue("error") }
                    .ifBlank { "Danbooru returned an unexpected response" }
                throw IllegalStateException(message)
            }
            else -> throw IllegalStateException("Danbooru returned an invalid response")
        }
    }

    private suspend fun fetchSafebooruQuery(query: String, page: Int, size: Int): GalleryFetchResult {
        val normalizedQuery = normalizeDanbooruQuery(query)
        val posts = fetchSafebooruPosts(page = page, size = size, tags = normalizedQuery)

        return GalleryFetchResult(
            totalPages = 0,
            totalItems = 0,
            data = posts.map { it.toProcessedPost(page = page, query = query) }
        )
    }

    private suspend fun fetchSafebooruPosts(page: Int, size: Int, tags: String? = null): List<SafebooruPost> {
        val queryParams = buildList {
            add("page=dapi")
            add("s=post")
            add("q=index")
            add("json=1")
            add("pid=$page")
            add("limit=$size")
            if (!tags.isNullOrBlank()) add("tags=${URLEncoder.encode(tags, "UTF-8")}")
        }.joinToString("&")

        val raw = RestClient.request(
            "${BaseUrls.SAFEBOORU}/index.php?$queryParams",
            JsonElement.serializer()
        )

        val elements = when (raw) {
            is JsonArray -> raw
            is JsonObject -> {
                val postNode = raw["post"]
                when (postNode) {
                    is JsonArray -> postNode
                    is JsonObject -> JsonArray(listOf(postNode))
                    else -> JsonArray(emptyList())
                }
            }
            else -> JsonArray(emptyList())
        }

        return elements.mapNotNull { it.toSafebooruPostOrNull() }
    }

    private fun JsonElement.toSafebooruPostOrNull(): SafebooruPost? {
        val obj = this as? JsonObject ?: return null
        return SafebooruPost(
            id = obj.intValue("id"),
            width = obj.intValue("width"),
            height = obj.intValue("height"),
            createdAt = obj.stringValue("created_at"),
            fileSize = obj.intValue("file_size"),
            rating = obj.stringValue("rating", "s"),
            score = obj.intValue("score"),
            tags = obj.stringValue("tags"),
            fileUrl = normalizeBooruUrl(obj.stringValue("file_url")),
            sampleUrl = normalizeBooruUrl(obj.stringValue("sample_url")),
            previewUrl = normalizeBooruUrl(obj.stringValue("preview_url")),
            source = obj.stringValue("source")
        )
    }

    private fun JsonObject.stringValue(key: String, defaultValue: String = "") =
        runCatching { this[key]?.jsonPrimitive?.content ?: defaultValue }.getOrDefault(defaultValue)

    private fun JsonObject.intValue(key: String, defaultValue: Int = 0) =
        runCatching { this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: defaultValue }.getOrDefault(defaultValue)

    private fun normalizeBooruUrl(url: String): String {
        if (url.isBlank()) return ""
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return "https:$url".takeIf { url.startsWith("//") } ?: "${BaseUrls.SAFEBOORU}/$url"
    }

    private fun normalizeDanbooruQuery(query: String): String {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return trimmed

        return trimmed
            .split(',')
            .map { token -> token.trim().replace(Regex("\\s+"), "_") }
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    private suspend fun parseGalleryNsfw(call: RoutingCall): Boolean? {
        val sfw = call.parseSfwPreference() ?: return null
        return !sfw
    }

    private fun List<ProcessedPost>.filterByNsfw(includeNsfw: Boolean): List<ProcessedPost> {
        if (includeNsfw) return this
        return filter { post ->
            !post.erotics && post.rating.lowercase() !in setOf("explicit", "questionable", "q", "e")
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.applyDanbooruAuth() {
        if (danbooruLogin.isBlank() || danbooruApiKey.isBlank()) return

        val credentials = Base64.getEncoder().encodeToString("$danbooruLogin:$danbooruApiKey".toByteArray())
        headers {
            append(HttpHeaders.Authorization, "Basic $credentials")
        }
    }

    private data class GalleryFetchResult(
        val totalPages: Int,
        val totalItems: Int,
        val data: List<ProcessedPost>
    )
}
