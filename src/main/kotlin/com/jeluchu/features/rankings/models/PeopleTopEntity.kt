package com.jeluchu.features.rankings.models

import kotlinx.serialization.Serializable

@Serializable
data class PeopleTopEntity(
    val rank: Int? = 0,
    val malId: Int? = 0,
    val name: String? = "",
    val givenName: String? = "",
    val familyName: String? = "",
    val image: String? = "",
    val birthday: String? = "",
    val top: String? = "",
    val page: Int? = 0
)