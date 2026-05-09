package com.jeluchu.core.models.jikan.search

import com.jeluchu.core.models.jikan.anime.AnimeData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Search data class.
 */
@Serializable
data class AnimeSearch(
    /**
     * Pagination info for request
     */
    @SerialName("pagination")
    val pagination: Pagination = Pagination(),

    /**
     * Data list of all anime found.
     */
    @SerialName("data")
    val data: List<AnimeData> = emptyList()
)