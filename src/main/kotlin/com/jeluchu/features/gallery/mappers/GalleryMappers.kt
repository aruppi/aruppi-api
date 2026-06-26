package com.jeluchu.features.gallery.mappers

import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.enums.GalleryProvider
import com.jeluchu.features.gallery.models.DanbooruPost
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import com.jeluchu.features.gallery.models.SafebooruPost
import org.bson.Document

fun PostsResponse.Post.toProcessedPost(page: Int, totalPages: Int = 0, totalItems: Int = 0): ProcessedPost {
    val dir = if (md5.length > 3) md5.substring(0, 3) else ""
    val largeImage = "https://oimages.anime-pictures.net/$dir/$md5$ext"
    val thumbnail = "https://opreviews.anime-pictures.net/$dir/${md5}_lp.avif"

    return ProcessedPost(
        id = id,
        largeImage = largeImage,
        thumbnail = thumbnail,
        provider = GalleryProvider.ANIME_PICTURES.value,
        sourceUrl = "https://anime-pictures.net/posts/$id",
        originalUrl = largeImage,
        width = width,
        height = height,
        pubtime = pubtime,
        size = size,
        erotics = erotics != 0,
        spoiler = spoiler,
        haveAlpha = haveAlpha,
        rating = if (erotics != 0) "explicit" else "safe",
        page = page,
        totalPages = totalPages,
        totalItems = totalItems
    )
}

fun PostsResponse.Post.toProcessedPostQuery(page: Int, query: String, totalPages: Int = 0, totalItems: Int = 0): ProcessedPost {
    val dir = if (md5.length > 3) md5.substring(0, 3) else ""
    val largeImage = "https://oimages.anime-pictures.net/$dir/$md5$ext"
    val thumbnail = "https://opreviews.anime-pictures.net/$dir/${md5}_lp.avif"

    return ProcessedPost(
        id = id,
        largeImage = largeImage,
        thumbnail = thumbnail,
        provider = GalleryProvider.ANIME_PICTURES.value,
        sourceUrl = "https://anime-pictures.net/posts/$id",
        originalUrl = largeImage,
        width = width,
        height = height,
        pubtime = pubtime,
        size = size,
        erotics = erotics != 0,
        spoiler = spoiler,
        haveAlpha = haveAlpha,
        rating = if (erotics != 0) "explicit" else "safe",
        page = page,
        totalPages = totalPages,
        totalItems = totalItems,
        query = query
    )
}

fun DanbooruPost.toProcessedPost(
    page: Int,
    query: String = "0",
    totalPages: Int = 0,
    totalItems: Int = 0
): ProcessedPost {
    val image = largeFileUrl ?: fileUrl ?: previewFileUrl.orEmpty()
    val thumbnail = previewFileUrl ?: largeFileUrl ?: fileUrl.orEmpty()

    return ProcessedPost(
        id = id,
        largeImage = image,
        thumbnail = thumbnail,
        provider = GalleryProvider.DANBOORU.value,
        sourceUrl = "https://danbooru.donmai.us/posts/$id",
        originalUrl = fileUrl ?: image,
        width = imageWidth,
        height = imageHeight,
        pubtime = createdAt,
        size = fileSize,
        erotics = rating.equals("e", ignoreCase = true),
        spoiler = false,
        haveAlpha = false,
        rating = when (rating.lowercase()) {
            "s" -> "safe"
            "q" -> "questionable"
            "e" -> "explicit"
            else -> rating
        },
        tags = tagString.split(" ").filter { it.isNotBlank() },
        page = page,
        totalPages = totalPages,
        totalItems = totalItems,
        query = query
    )
}

fun SafebooruPost.toProcessedPost(
    page: Int,
    query: String = "0",
    totalPages: Int = 0,
    totalItems: Int = 0
): ProcessedPost {
    val image = if (sampleUrl.isNotBlank()) sampleUrl else fileUrl
    val thumbnail = if (previewUrl.isNotBlank()) previewUrl else image

    return ProcessedPost(
        id = id,
        largeImage = image,
        thumbnail = thumbnail,
        provider = GalleryProvider.SAFEBOORU.value,
        sourceUrl = "${
            if (source.isNotBlank()) source else "https://safebooru.org/index.php?page=post&s=view&id=$id"
        }",
        originalUrl = fileUrl.ifBlank { image },
        width = width,
        height = height,
        pubtime = createdAt,
        size = fileSize,
        erotics = false,
        spoiler = false,
        haveAlpha = false,
        rating = "safe",
        tags = tags.split(" ").filter { it.isNotBlank() },
        page = page,
        totalPages = totalPages,
        totalItems = totalItems,
        query = query
    )
}

fun documentToProcessedPost(doc: Document) = ProcessedPost(
    id = doc.getIntSafe("id"),
    largeImage = doc.getStringSafe("large_image"),
    thumbnail = doc.getStringSafe("thumbnail"),
    provider = doc.getStringSafe("provider", GalleryProvider.ANIME_PICTURES.value),
    sourceUrl = doc.getStringSafe("source_url"),
    originalUrl = doc.getStringSafe("original_url"),
    width = doc.getIntSafe("width"),
    height = doc.getIntSafe("height"),
    pubtime = doc.getStringSafe("pubtime"),
    size = doc.getIntSafe("size"),
    erotics = doc.getBooleanSafe("erotics"),
    spoiler = doc.getBooleanSafe("spoiler"),
    haveAlpha = doc.getBooleanSafe("haveAlpha"),
    rating = doc.getStringSafe("rating"),
    tags = doc.getListSafe("tags"),
    page = doc.getIntSafe("page"),
    totalPages = doc.getIntSafe("totalPages"),
    totalItems = doc.getIntSafe("totalItems"),
    query = doc.getStringSafe("query")
)
