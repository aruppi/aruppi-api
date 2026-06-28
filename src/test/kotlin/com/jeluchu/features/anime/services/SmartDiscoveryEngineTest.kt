package com.jeluchu.features.anime.services

import org.bson.Document
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmartDiscoveryEngineTest {
    @Test
    fun `scores genres stored as tags`() {
        val profile = SmartDiscoveryEngine.profile(
            listOf(anime("Favorite", tags = listOf("Acción", "Aventura")))
        )

        val score = SmartDiscoveryEngine.score(
            anime("Candidate", tags = listOf("Acción", "Comedia")),
            profile,
        )

        assertEquals(3, score.affinity)
        assertEquals(listOf("Acción"), score.matchedGenres)
    }

    @Test
    fun `supports legacy genres field`() {
        val profile = SmartDiscoveryEngine.profile(
            listOf(anime("Favorite", genres = listOf("Drama")))
        )

        val score = SmartDiscoveryEngine.score(
            anime("Candidate", genres = listOf("Drama")),
            profile,
        )

        assertEquals(listOf("Drama"), score.matchedGenres)
    }

    @Test
    fun `scores shared studios when genres differ`() {
        val profile = SmartDiscoveryEngine.profile(
            listOf(anime("Favorite", studios = listOf("Bones")))
        )

        val score = SmartDiscoveryEngine.score(
            anime("Candidate", studios = listOf("Bones")),
            profile,
        )

        assertEquals(2, score.affinity)
        assertEquals(listOf("Bones"), score.matchedStudios)
        assertTrue(score.matchedGenres.isEmpty())
    }

    @Test
    fun `uses anime type as fallback signal`() {
        val profile = SmartDiscoveryEngine.profile(listOf(anime("Favorite", type = "TV")))

        val score = SmartDiscoveryEngine.score(anime("Candidate", type = "TV"), profile)

        assertEquals(1, score.affinity)
    }

    @Test
    fun `weights repeated preferences more strongly`() {
        val profile = SmartDiscoveryEngine.profile(
            listOf(
                anime("First", tags = listOf("Acción")),
                anime("Second", tags = listOf("Acción")),
            )
        )

        val score = SmartDiscoveryEngine.score(
            anime("Candidate", tags = listOf("Acción")),
            profile,
        )

        assertEquals(6, score.affinity)
    }

    @Test
    fun `selects the favorite with greatest affinity as explanation`() {
        val action = anime("Action favorite", tags = listOf("Acción"))
        val drama = anime("Drama favorite", tags = listOf("Drama"))
        val candidate = anime("Candidate", tags = listOf("Drama"))

        val source = SmartDiscoveryEngine.bestSourceTitle(candidate, listOf(action, drama))

        assertEquals("Drama favorite", source)
    }

    private fun anime(
        title: String,
        type: String = "",
        tags: List<String> = emptyList(),
        genres: List<String> = emptyList(),
        studios: List<String> = emptyList(),
    ): Document = Document()
        .append("title", title)
        .append("type", type)
        .apply {
            if (tags.isNotEmpty()) append("tags", Document("es", tags))
            if (genres.isNotEmpty()) append("genres", Document("es", genres))
            if (studios.isNotEmpty()) {
                append("studios", studios.map { Document("name", it) })
            }
        }
}
