package com.jeluchu.features.anime.services

import com.jeluchu.core.extensions.getDocumentSafe
import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import org.bson.Document

internal object SmartDiscoveryEngine {
    fun profile(favorites: List<Document>): DiscoveryProfile {
        val genres = favorites.flatMap { it.discoveryGenres() }
        val studios = favorites.flatMap { it.discoveryStudios() }
        val types = favorites.map { it.getStringSafe("type") }.filter(String::isNotBlank)

        return DiscoveryProfile(
            preferredGenres = genres.distinct(),
            preferredStudios = studios.distinct(),
            preferredTypes = types.distinct(),
            genreWeights = genres.groupingBy { it.lowercase() }.eachCount(),
            studioWeights = studios.groupingBy { it.lowercase() }.eachCount(),
            typeWeights = types.groupingBy { it.lowercase() }.eachCount(),
        )
    }

    fun score(anime: Document, profile: DiscoveryProfile): DiscoveryScore {
        val genres = anime.discoveryGenres()
        val studios = anime.discoveryStudios()
        val affinity = genres.sumOf { profile.genreWeights[it.lowercase()] ?: 0 } * 3 +
            studios.sumOf { profile.studioWeights[it.lowercase()] ?: 0 } * 2 +
            (profile.typeWeights[anime.getStringSafe("type").lowercase()] ?: 0)

        return DiscoveryScore(
            affinity = affinity,
            matchedGenres = genres.filter { profile.genreWeights.containsKey(it.lowercase()) }.take(3),
            matchedStudios = studios.filter { profile.studioWeights.containsKey(it.lowercase()) }.take(2),
        )
    }

    fun bestSourceTitle(
        anime: Document,
        favorites: List<Document>,
    ): String = favorites.maxByOrNull { favorite ->
        val favoriteProfile = profile(listOf(favorite))
        score(anime, favoriteProfile).affinity
    }?.getStringSafe("title").orEmpty()

    private fun Document.discoveryGenres(): List<String> =
        listOf("tags", "genres")
            .flatMap { key ->
                getDocumentSafe(key)?.let { values ->
                    values.getListSafe<String>("es") + values.getListSafe<String>("en")
                }.orEmpty()
            }
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)

    private fun Document.discoveryStudios(): List<String> =
        getListSafe<Document>("studios")
            .map { it.getStringSafe("name") }
            .filter(String::isNotBlank)
}

internal data class DiscoveryProfile(
    val preferredGenres: List<String>,
    val preferredStudios: List<String>,
    val preferredTypes: List<String>,
    val genreWeights: Map<String, Int>,
    val studioWeights: Map<String, Int>,
    val typeWeights: Map<String, Int>,
)

internal data class DiscoveryScore(
    val affinity: Int,
    val matchedGenres: List<String>,
    val matchedStudios: List<String>,
)
