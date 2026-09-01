package com.jeluchu.features.anime.utils

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity.Companion.toLastEpisodeData
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val AIRING_TV_ENDPOINT = "anime?status=airing&type=tv&page="

suspend fun fetchLastEpisodesFromTenrai(
    sfw: Boolean = true,
    pauseMillis: Long = 1_000L
): List<LastEpisodeEntity> {
    val animes = mutableListOf<LastEpisodeEntity>()
    val sfwQuery = "&sfw=$sfw"

    val firstPage = RestClient.request(
        url = BaseUrls.TENRAI + AIRING_TV_ENDPOINT + "1" + sfwQuery,
        deserializer = AnimeSearch.serializer()
    )

    animes.addAll(firstPage.data.map { anime -> anime.toLastEpisodeData(sfw = sfw) })

    val totalPages = firstPage.pagination.lastPage ?: 1
    for (page in 2..totalPages) {
        delay(pauseMillis.milliseconds)
        RestClient.request(
            url = BaseUrls.TENRAI + AIRING_TV_ENDPOINT + page + sfwQuery,
            deserializer = AnimeSearch.serializer()
        ).data.let { pageAnimes ->
            animes.addAll(pageAnimes.map { anime -> anime.toLastEpisodeData(sfw = sfw) })
        }
    }

    return animes.distinctBy { it.malId }
}
