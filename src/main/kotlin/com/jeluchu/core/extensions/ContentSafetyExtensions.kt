package com.jeluchu.core.extensions

import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.jikan.anime.AnimeData
import com.jeluchu.core.models.jikan.manga.MangaData
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingCall
import org.bson.Document

private val blockedAnimeRatings = setOf(
    "R+ - Mild Nudity",
    "Rx - Hentai"
)

suspend fun RoutingCall.parseSfwPreference(defaultSfw: Boolean = true): Boolean? {
    val rawSfw = request.queryParameters["sfw"]
    val rawNsfw = request.queryParameters["nsfw"]
    val sfw = rawSfw?.toBooleanStrictOrNull()
    val nsfw = rawNsfw?.toBooleanStrictOrNull()

    if ((rawSfw != null && sfw == null) || (rawNsfw != null && nsfw == null)) {
        respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidRequest.message)
        return null
    }

    if (sfw != null && nsfw != null && sfw == nsfw) {
        respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidRequest.message)
        return null
    }

    return when {
        sfw != null -> sfw
        nsfw != null -> !nsfw
        else -> defaultSfw
    }
}

fun Document.isSafeAnimeDocument(): Boolean {
    val nsfw = getBooleanSafe("nsfw", false)
    val ageRating = getStringSafe("ageRating")
    val rating = getStringSafe("rating")
    return !nsfw && isSafeAnimeRating(ageRating) && isSafeAnimeRating(rating)
}

fun AnimeData.isSafeAnimeData(): Boolean = isSafeAnimeRating(rating)

fun MangaData.isSafeMangaData(): Boolean = explicitGenres.orEmpty().isEmpty()

fun isSafeAnimeRating(rawRating: String?): Boolean {
    val normalized = rawRating.orEmpty().trim()
    if (normalized.isBlank()) return true

    return blockedAnimeRatings.none { blocked ->
        normalized.equals(blocked, ignoreCase = true) ||
            normalized.startsWith(blocked, ignoreCase = true)
    }
}
