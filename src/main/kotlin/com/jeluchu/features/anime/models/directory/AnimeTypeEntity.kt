package com.jeluchu.features.anime.models.directory

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AnimeTypeEntity(
    val malId: Int? = 0,
    val type: String? = "",
    val year: Int? = 0,
    val season: String? = "",
    val title: String? = "",
    val image: String? = "",
    @EncodeDefault(mode = EncodeDefault.Mode.ALWAYS)
    val score: String? = ""
)