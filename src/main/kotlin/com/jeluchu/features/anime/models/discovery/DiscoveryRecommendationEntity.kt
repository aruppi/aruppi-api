package com.jeluchu.features.anime.models.discovery

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryRecommendationEntity(
    val malId: Int,
    val title: String,
    val image: String,
    val type: String,
    val basedOnTitle: String,
    val matchedGenres: List<String>,
    val matchedStudios: List<String>,
)
