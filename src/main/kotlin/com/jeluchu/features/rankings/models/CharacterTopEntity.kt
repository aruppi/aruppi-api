package com.jeluchu.features.rankings.models

import kotlinx.serialization.Serializable

@Serializable
data class CharacterTopEntity(
    val rank: Int? = 0,
    val malId: Int? = 0,
    val name: String? = "",
    val nameKanji: String? = "",
    val image: String? = "",
    val top: String? = "",
    val page: Int? = 0
)