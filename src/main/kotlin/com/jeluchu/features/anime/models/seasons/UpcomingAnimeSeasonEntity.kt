package com.jeluchu.features.anime.models.seasons

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class UpcomingAnimeSeasonEntity(
    val page: Int? = 0,
    val malId: Int? = 0,
    val limit: Int? = 0,
    val url: String? = "",
    val type: String? = "",
    val image: String? = "",
    val title: String? = "",
    val rating: String? = "",
    val filter: String? = "",
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    val sfw: Boolean? = false,
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    val start: AnimeSeasonStartEntity? = null,
) {
    @Serializable
    data class AnimeSeasonStartEntity(
        val day: Int? = 0,
        val month: Int? = 0,
        val year: Int? = 0
    )
}