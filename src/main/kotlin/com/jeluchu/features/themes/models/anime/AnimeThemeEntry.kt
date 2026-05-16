package com.jeluchu.features.themes.models.anime

import kotlinx.serialization.Serializable

@Serializable
data class AnimeThemeEntry(
    val episodes: String?,
    val id: Int?,
    val notes: String?,
    val nsfw: Boolean?,
    val spoiler: Boolean?,
    val videos: List<Video>?
)