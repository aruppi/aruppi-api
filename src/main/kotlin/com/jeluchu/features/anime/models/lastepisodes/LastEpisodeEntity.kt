package com.jeluchu.features.anime.models.lastepisodes

import com.jeluchu.core.models.jikan.anime.AnimeData
import kotlinx.serialization.Serializable

@Serializable
data class LastEpisodeEntity(
    val malId: Int = 0,
    val title: String?,
    val score: String?,
    val image: String?,
    val day: String?,
    val time: String?,
    val timezone: String?,
    val sfw: Boolean,
) {
    companion object {
        fun AnimeData.toLastEpisodeData(sfw: Boolean) = LastEpisodeEntity(
            malId = malId ?: 0,
            day = broadcast?.day.orEmpty(),
            time = broadcast?.time.orEmpty(),
            score = score?.toString().orEmpty(),
            image = images?.webp?.large.orEmpty(),
            timezone = broadcast?.timezone.orEmpty(),
            title = titles?.first()?.title.orEmpty(),
            sfw = sfw
        )
    }
}
