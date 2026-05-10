package com.jeluchu.core.models.jikan.anime

import com.jeluchu.core.enums.Day
import com.jeluchu.core.extensions.toYouTubeWatchUrl
import com.jeluchu.features.anime.models.seasons.UpcomingAnimeSeasonEntity
import com.jeluchu.features.anime.models.seasons.UpcomingAnimeSeasonEntity.AnimeSeasonStartEntity
import com.jeluchu.features.rankings.models.AnimeTopEntity
import com.jeluchu.features.schedule.models.DayEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * AnimeInfo data class.
 */
@Serializable
data class AnimeData(
    /**
     * ID associated with MyAnimeList.
     */
    @SerialName("mal_id")
    val malId: Int? = 0,

    /**
     * Anime's MyAnimeList link.
     */
    @SerialName("url")
    val url: String? = "",

    /**
     * Anime's MyAnimeList cover/image link.
     * @see Images for the detail.
     */
    @SerialName("images")
    val images: Images? = Images(),

    /**
     * Anime's official trailer URL.
     * @see Trailer for the detail.
     */
    @SerialName("trailer")
    val trailer: Trailer? = Trailer(),

    /**
     * When entry is pending approval on MAL.
     */
    @SerialName("approved")
    val approved: Boolean? = false,

    /**
     * Title of the anime.
     * @see Title for the detail.
     */
    @SerialName("titles")
    val titles: List<Title>? = emptyList(),

    /**
     * Title of the anime.
     */
    @Deprecated("Use 'titles: List<Title>' to get the title")
    @SerialName("title")
    val title: String? = "",

    /**
     * Title of the anime in English.
     */
    @Deprecated("Use 'titles: List<Title>' to get the title")
    @SerialName("title_english")
    val titleEnglish: String? = "",

    /**
     * Title of the anime in Japanese.
     */
    @Deprecated("Use 'titles: List<Title>' to get the title")
    @SerialName("title_japanese")
    val titleJapanese: String? = "",

    /**
     * List of anime's synonyms.
     * @return null if there's none.
     */
    @Deprecated("Use 'titles: List<Title>' to get the title")
    @SerialName("title_synonyms")
    val titleSynonyms: List<String>? = emptyList(),

    /**
     * Type of the anime.
     */
    @SerialName("type")
    val type: String? = "",

    /**
     * Source of the anime.
     */
    @SerialName("source")
    val source : String? = "",

    /**
     * Total episode(s) of the anime.
     */
    @SerialName("episodes")
    val episodes: Int? = 0,

    /**
     * Status of the anime (e.g "Airing", "Not yet airing", etc).
     */
    @SerialName("status")
    val status : String? = "",

    /**
     * Whether the anime is currently airing or not.
     */
    @SerialName("airing")
    val airing: Boolean? = false,

    /**
     * Interval of airing time in ISO8601 format.
     * @see Aired for the detail.
     * @return null if there's none
     */
    @SerialName("aired")
    val aired: Aired? = Aired(),

    /**
     * Duration per episode.
     */
    @SerialName("duration")
    val duration : String? = "",

    /**
     * Age rating of the anime.
     */
    @SerialName("rating")
    val rating : String? = "",

    /**
     * Score at MyAnimeList. Formatted up to 2 decimal places.
     */
    @SerialName("score")
    val score: Float? = 0f,

    /**
     * Number of people/users that scored the anime.
     */
    @SerialName("scored_by")
    val scoredBy: Int? = 0,

    /**
     * Anime's score rank on MyAnimeList.
     */
    @SerialName("rank")
    val rank: Int? = 0,

    /**
     * Anime's popularity rank on MyAnimeList.
     */
    @SerialName("popularity")
    val popularity: Int? = 0,

    /**
     * Anime's members count on MyAnimeList.
     */
    @SerialName("members")
    val members: Int? = 0,

    /**
     * Anime's favorites count on MyAnimeList.
     */
    @SerialName("favorites")
    val favorites: Int? = 0,

    /**
     * Synopsis of the anime.
     */
    @SerialName("synopsis")
    val synopsis : String? = "",

    /**
     * Background info of the anime.
     */
    @SerialName("background")
    val background : String? = "",

    /**
     * Season where anime premiered.
     */
    @SerialName("season")
    val season: String? = "",

    /**
     * Year where anime premiered.
     */
    @SerialName("year")
    val year: Int? = 0,

    /**
     * Broadcast date of the anime (day and time).
     * @see Broadcast for the detail.
     */
    @SerialName("broadcast")
    val broadcast: Broadcast? = Broadcast(),

    /**
     * List of producers of this anime.
     * @see Producer for the detail.
     */
    @SerialName("producers")
    val producers: List<Producer>? = emptyList(),

    /**
     * List of licensors of this anime.
     * @see Licensor for the detail.
     */
    @SerialName("licensors")
    val licensors: List<Licensor>? = emptyList(),

    /**
     * List of studios of this anime.
     * @see Studio for the detail.
     *
     */
    @SerialName("studios")
    val studios: List<Studio>? = emptyList(),

    /**
     * List of genre of this anime.
     * @see Genre for the detail.
     */
    @SerialName("genres")
    val genres: List<Genre>? = emptyList(),

    /**
     * List of explicit genre of this anime.
     * @see ExplicitGenre for the detail.
     */
    @SerialName("explicit_genres")
    val explicitGenres: List<ExplicitGenre>? = emptyList(),

    /**
     * List of themes of this anime.
     * @see Themes for the detail.
     */
    @SerialName("themes")
    val themes: List<Themes>? = emptyList(),

    /**
     * Demographic of this anime.
     * @see Demographic for the detail.
     */
    @SerialName("demographics")
    val demographics: List<Demographic>? = emptyList(),

    /**
     * Relation of this anime.
     * @see Relation for the detail.
     */
    @SerialName("relations")
    val relations: List<Relation>? = emptyList(),

    /**
     * Theme of this anime.
     * @see Theme for the detail.
     */
    @SerialName("theme")
    val theme: Theme? = Theme(),

    /**
     * Theme of this anime.
     * @see External for the detail.
     */
    @SerialName("external")
    val external: List<External>? = emptyList(),

    /**
     * Theme of this anime.
     * @see Streaming for the detail.
     */
    @SerialName("streaming")
    val streaming: List<Streaming>? = emptyList()
) {
    companion object {
        fun AnimeData.toDayEntity(day: Day) = DayEntity(
            malId = malId ?: 0,
            day = day.name.lowercase(),
            image = images?.webp?.large.orEmpty(),
            title = titles?.first()?.title.orEmpty()
        )

        fun AnimeData.toAnimeTopEntity(
            page: Int,
            top: String,
            type: String,
            subType: String,
        ) = AnimeTopEntity(
            malId = malId,
            rank = rank,
            score = score,
            image = images?.webp?.large.orEmpty(),
            title = titles?.first()?.title.orEmpty(),
            url = url,
            promo = trailer?.embedUrl.toYouTubeWatchUrl().orEmpty(),
            season = season,
            year = year,
            airing = airing,
            top = top,
            type = type,
            subtype = subType,
            page = page
        )

        fun AnimeData.toUpcomingAnime(
            page: Int,
            sfw: Boolean,
            filter: String

        ) = UpcomingAnimeSeasonEntity(
            sfw = sfw,
            page = page,
            filter = filter,
            malId = malId ?: 0,
            url = url.orEmpty(),
            type = type.orEmpty(),
            rating = rating.orEmpty(),
            image = images?.webp?.large.orEmpty(),
            title = titles?.first()?.title.orEmpty(),
            start = AnimeSeasonStartEntity(
                day = aired?.prop?.from?.day ?: 0,
                month = aired?.prop?.from?.month ?: 0,
                year = aired?.prop?.from?.year ?: 0
            )
        )
    }
}