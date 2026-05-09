@file:Suppress("unused")

package com.jeluchu.core.extensions

private val youtubeIdPatterns = listOf(
    Regex("(?:youtube(?:-nocookie)?\\.com/(?:embed|v|shorts)/|youtu\\.be/|watch\\?v=)([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE),
    Regex("[?&]v=([A-Za-z0-9_-]{11})", RegexOption.IGNORE_CASE)
)

fun String?.extractYouTubeVideoId(): String? {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return null

    return youtubeIdPatterns
        .firstNotNullOfOrNull { pattern -> pattern.find(value)?.groupValues?.getOrNull(1) }
}

fun String?.toYouTubeWatchUrl(): String? {
    val videoId = extractYouTubeVideoId() ?: return null
    return "https://www.youtube.com/watch?v=$videoId"
}

