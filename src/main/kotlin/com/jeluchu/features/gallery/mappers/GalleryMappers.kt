package com.jeluchu.features.gallery.mappers

import com.jeluchu.core.extensions.getBooleanSafe
import com.jeluchu.core.extensions.getDocumentSafe
import com.jeluchu.core.extensions.getFloatSafe
import com.jeluchu.core.extensions.getIntSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.extensions.parseRssDate
import com.jeluchu.features.anime.mappers.documentToVideoPromo
import com.jeluchu.features.anime.models.anime.VideoPromo
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import com.jeluchu.features.news.models.NewEntity
import com.prof18.rssparser.model.RssChannel
import org.bson.Document

fun PostsResponse.Post.toProcessedPost(page: Int): ProcessedPost {
    val imageUrl = "https://oimages.anime-pictures.net/${md5.take(3)}/$md5$ext"

    return ProcessedPost(
        id = id,
        image = imageUrl,
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
    val imageUrl = "https://oimages.anime-pictures.net/${md5.take(3)}/$md5$ext"

    return ProcessedPost(
        id = id,
        image = imageUrl,
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
    image = doc.getStringSafe("image"),
    width = doc.getIntSafe("width"),
    height = doc.getIntSafe("height"),
    pubtime = doc.getStringSafe("pubtime"),
    size = doc.getIntSafe("size"),
    erotics = doc.getBooleanSafe("erotics"),
    spoiler = doc.getBooleanSafe("spoiler"),
    haveAlpha = doc.getBooleanSafe("haveAlpha")
)
