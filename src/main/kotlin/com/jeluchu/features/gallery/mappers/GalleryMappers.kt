package com.jeluchu.features.gallery.mappers

import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import org.bson.Document

fun PostsResponse.Post.toProcessedPost(page: Int): ProcessedPost {
    val dir = if (md5.length > 3) md5.substring(0, 3) else ""
    val largeImage = "https://oimages.anime-pictures.net/$dir/$md5$ext"
    val thumbnail = "https://opreviews.anime-pictures.net/$dir/${md5}_lp.avif"

    return ProcessedPost(
        id = id,
        largeImage = largeImage,
        thumbnail = thumbnail,
        width = width,
        height = height,
        pubtime = pubtime,
        size = size,
        erotics = erotics != 0,
        spoiler = spoiler,
        haveAlpha = haveAlpha,
        page = page
    )
}

fun PostsResponse.Post.toProcessedPostQuery(page: Int, query: String): ProcessedPost {
    val dir = if (md5.length > 3) md5.substring(0, 3) else ""
    val largeImage = "https://oimages.anime-pictures.net/$dir/$md5$ext"
    val thumbnail = "https://opreviews.anime-pictures.net/$dir/${md5}_lp.avif"

    return ProcessedPost(
        id = id,
        largeImage = largeImage,
        thumbnail = thumbnail,
        width = width,
        height = height,
        pubtime = pubtime,
        size = size,
        erotics = erotics != 0,
        spoiler = spoiler,
        haveAlpha = haveAlpha,
        page = page,
        query = query
    )
}

fun documentToProcessedPost(doc: Document) = ProcessedPost(
    id = doc.getIntSafe("id"),
    largeImage = doc.getStringSafe("large_image"),
    thumbnail = doc.getStringSafe("thumbnail"),
    width = doc.getIntSafe("width"),
    height = doc.getIntSafe("height"),
    pubtime = doc.getStringSafe("pubtime"),
    size = doc.getIntSafe("size"),
    erotics = doc.getBooleanSafe("erotics"),
    spoiler = doc.getBooleanSafe("spoiler"),
    haveAlpha = doc.getBooleanSafe("haveAlpha")
)
