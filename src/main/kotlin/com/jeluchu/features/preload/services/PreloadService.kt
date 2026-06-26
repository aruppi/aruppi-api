package com.jeluchu.features.preload.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.AnimeFilterTypes
import com.jeluchu.core.enums.AnimeTypes
import com.jeluchu.core.enums.Day
import com.jeluchu.core.enums.MangaFilterTypes
import com.jeluchu.core.enums.MangaTypes
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.update
import com.jeluchu.core.models.PreloadResponse
import com.jeluchu.core.models.PreloadTaskResult
import com.jeluchu.core.models.jikan.anime.AnimeData.Companion.toAnimeTopEntity
import com.jeluchu.core.models.jikan.anime.AnimeData.Companion.toDayEntity
import com.jeluchu.core.models.jikan.character.CharacterSearch
import com.jeluchu.core.models.jikan.manga.MangaData.Companion.toMangaTopEntity
import com.jeluchu.core.models.jikan.people.PeopleData.Companion.toPeopleTopEntity
import com.jeluchu.core.models.jikan.people.PeopleSearch
import com.jeluchu.core.models.jikan.search.AnimeSearch
import com.jeluchu.core.models.jikan.search.MangaSearch
import com.jeluchu.core.utils.BaseUrls
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.Endpoints
import com.jeluchu.core.utils.RssSources
import com.jeluchu.core.utils.RssUrls
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.anime.mappers.documentToAnimeDirectoryEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity
import com.jeluchu.features.anime.utils.fetchLastEpisodesFromJikan
import com.jeluchu.features.anitakume.mappers.toPodcast
import com.jeluchu.features.anitakume.models.AnitakumeEntity
import com.jeluchu.features.gallery.mappers.toProcessedPost
import com.jeluchu.features.gallery.models.PostsResponse
import com.jeluchu.features.gallery.models.ProcessedPost
import com.jeluchu.features.news.mappers.toNews
import com.jeluchu.features.news.models.NewEntity
import com.jeluchu.features.rankings.models.AnimeTopEntity
import com.jeluchu.features.rankings.models.CharacterTopEntity
import com.jeluchu.features.rankings.models.MangaTopEntity
import com.jeluchu.features.rankings.models.PeopleTopEntity
import com.jeluchu.features.schedule.models.DayEntity
import com.jeluchu.features.schedule.models.ScheduleEntity
import com.jeluchu.features.themes.models.artist.ArtistEntity
import com.jeluchu.features.themes.models.artist.ArtistEntity.Companion.toArtistEntity
import com.jeluchu.features.themes.models.artist.ArtistSearch
import com.jeluchu.features.themes.models.artist.SongSearch
import com.jeluchu.features.themes.models.song.SongEntity
import com.jeluchu.features.themes.models.song.SongEntity.Companion.toSongEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.prof18.rssparser.RssParser
import fordelete.CharacterData.Companion.toCharacterTopEntity
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import kotlin.time.Duration.Companion.milliseconds

class PreloadService(
    private val database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val pauseMillis = 1_200L
    private val preloadPages = 3

    suspend fun preload(force: Boolean = false): PreloadResponse {
        val results = mutableListOf<PreloadTaskResult>()

        suspend fun runTask(key: String, block: suspend () -> Boolean) {
            runCatching { block() }
                .onSuccess { refreshed ->
                    results.add(PreloadTaskResult(key, if (refreshed) "refreshed" else "skipped"))
                }
                .onFailure { error ->
                    results.add(PreloadTaskResult(key, "failed", error.message))
                }
            delay(pauseMillis.milliseconds)
        }

        runTask(Collections.NEWS_ES) { refreshSpanishNews(force) }
        runTask(Collections.NEWS_EN) { refreshEnglishNews(force) }
        runTask(Collections.ANITAKUME) { refreshAnitakume(force) }
        runTask(TimerKey.SCHEDULE) { refreshSchedule(force) }
        runTask(TimerKey.LAST_EPISODES) { refreshLastEpisodes(force) }
        for (page in 1..preloadPages) {
            runTask("${Collections.ANIME_PICTURES_RECENT}_$page") { refreshRecentGallery(page, force) }
            runTask("${TimerKey.THEMES}artists_$page") { refreshArtists(page, force) }
            runTask("${TimerKey.THEMES}songs_$page") { refreshSongs(page, force) }
        }

        AnimeTypes.entries.forEach { type ->
            runTask("${TimerKey.ANIME_TYPE}${type.name.lowercase()}") { refreshAnimeDirectoryType(type, force) }
            AnimeFilterTypes.entries.forEach { filter ->
                for (page in 1..preloadPages) {
                    runTask("${Collections.ANIME_RANKING}_${type.name.lowercase()}_${filter.name.lowercase()}_$page") {
                        refreshAnimeRanking(type.name.lowercase(), filter.name.lowercase(), page, force)
                    }
                }
                runTask("${Collections.ANIME_RANKING}_${Collections.TOP_TEN}_${type.name.lowercase()}_${filter.name.lowercase()}") {
                    refreshAnimeTopTenRanking(type.name.lowercase(), filter.name.lowercase(), force)
                }
            }
        }

        MangaTypes.entries.forEach { type ->
            MangaFilterTypes.entries.forEach { filter ->
                for (page in 1..preloadPages) {
                    runTask("${Collections.MANGA_RANKING}_${type.name.lowercase()}_${filter.name.lowercase()}_$page") {
                        refreshMangaRanking(type.name.lowercase(), filter.name.lowercase(), page, force)
                    }
                }
            }
        }

        for (page in 1..preloadPages) {
            runTask("${Collections.PEOPLE_RANKING}_$page") { refreshPeopleRanking(page, force) }
            runTask("${Collections.CHARACTER_RANKING}_$page") { refreshCharacterRanking(page, force) }
        }

        val refreshed = results.count { it.status == "refreshed" }
        val skipped = results.count { it.status == "skipped" }
        val failed = results.count { it.status == "failed" }
        return PreloadResponse(refreshed, skipped, failed, results)
    }

    private fun shouldRefresh(key: String, amount: Long, unit: TimeUnit, force: Boolean) =
        force || timers.needsUpdate(key = key, amount = amount, unit = unit)

    private suspend fun refreshSpanishNews(force: Boolean): Boolean {
        if (!shouldRefresh(Collections.NEWS_ES, 1, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.NEWS_ES)
        collection.deleteMany(Document())
        val response = mutableListOf<NewEntity>().apply {
            with(RssParser()) {
                getRssChannel(RssUrls.MANGALATAM).toNews(RssSources.MANGALATAM, this@apply)
                getRssChannel(RssUrls.SOMOSKUDASAI).toNews(RssSources.SOMOSKUDASAI, this@apply)
                getRssChannel(RssUrls.CRUNCHYROLL).toNews(RssSources.CRUNCHYROLL, this@apply)
                getRssChannel(RssUrls.RAMENPARADOS).toNews(RssSources.RAMENPARADOS, this@apply)
                getRssChannel(RssUrls.ANMOSUGOI).toNews(RssSources.ANMOSUGOI, this@apply)
            }
        }.shuffled()
        parseDataToDocuments(response, NewEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(Collections.NEWS_ES)
        return true
    }

    private suspend fun refreshEnglishNews(force: Boolean): Boolean {
        if (!shouldRefresh(Collections.NEWS_EN, 1, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.NEWS_EN)
        collection.deleteMany(Document())
        val response = mutableListOf<NewEntity>().apply {
            with(RssParser()) {
                getRssChannel(RssUrls.OTAKUMODE).toNews(RssSources.OTAKUMODE, this@apply)
                getRssChannel(RssUrls.MYANIMELIST).toNews(RssSources.MYANIMELIST, this@apply)
                getRssChannel(RssUrls.HONEYSANIME).toNews(RssSources.HONEYSANIME, this@apply)
                getRssChannel(RssUrls.ANIMEHUNCH).toNews(RssSources.ANIMEHUNCH, this@apply)
            }
        }.shuffled()
        parseDataToDocuments(response, NewEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(Collections.NEWS_EN)
        return true
    }

    private suspend fun refreshAnitakume(force: Boolean): Boolean {
        if (!shouldRefresh(Collections.ANITAKUME, 1, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.ANITAKUME)
        collection.deleteMany(Document())
        val response = mutableListOf<AnitakumeEntity>().apply {
            RssParser().getRssChannel(RssUrls.ANITAKUME).toPodcast(this@apply)
        }
        parseDataToDocuments(response, AnitakumeEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(Collections.ANITAKUME)
        return true
    }

    private suspend fun refreshSchedule(force: Boolean): Boolean {
        if (!shouldRefresh(TimerKey.SCHEDULE, 7, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.SCHEDULES)
        collection.deleteMany(Document())
        Day.entries.forEach { day ->
            val data = RestClient.requestWithDelay(
                BaseUrls.JIKAN + Endpoints.SCHEDULES + "/" + day,
                deserializer = ScheduleEntity.serializer()
            ).data?.map { it.toDayEntity(day) }.orEmpty()
            parseDataToDocuments(data, DayEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        }
        timers.update(TimerKey.SCHEDULE)
        return true
    }

    private suspend fun refreshLastEpisodes(force: Boolean): Boolean {
        if (!shouldRefresh(TimerKey.LAST_EPISODES, 6, TimeUnit.HOUR, force)) return false
        val collection = database.getCollection(TimerKey.LAST_EPISODES)
        val animes = fetchLastEpisodesFromJikan(pauseMillis = pauseMillis)
        parseDataToDocuments(animes, LastEpisodeEntity.serializer()).also {
            if (it.isNotEmpty()) {
                collection.deleteMany(Document())
                collection.insertMany(it)
            }
        }
        timers.update(TimerKey.LAST_EPISODES)
        return true
    }

    private suspend fun refreshRecentGallery(page: Int, force: Boolean): Boolean {
        val key = "${Collections.ANIME_PICTURES_RECENT}_$page"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.ANIME_PICTURES_RECENT)
        collection.deleteMany(Filters.eq("page", page))
        val response = RestClient.request(
            BaseUrls.ANIME_PICTURES + Endpoints.POSTS + "?page=$page",
            PostsResponse.serializer()
        ).posts.map { it.toProcessedPost(page) }
        parseDataToDocuments(response, ProcessedPost.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshArtists(page: Int, force: Boolean): Boolean {
        val key = "${TimerKey.THEMES}artists_$page"
        if (!shouldRefresh(key, 7, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.ARTISTS_INDEX)
        collection.deleteMany(Filters.eq("page_cache", page))
        val include = "songs.animethemes.anime,songs.animethemes.animethemeentries.videos"
        val artists = RestClient.request(
            "${BaseUrls.ANIME_THEMES}artist?include=$include&page[number]=$page&page[size]=25",
            ArtistSearch.serializer()
        ).artists?.map { it.toArtistEntity() }.orEmpty()
        parseDataToDocuments(artists, ArtistEntity.serializer())
            .onEach { it.append("page_cache", page) }
            .also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshSongs(page: Int, force: Boolean): Boolean {
        val key = "${TimerKey.THEMES}songs_$page"
        if (!shouldRefresh(key, 7, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.SONGS_INDEX)
        collection.deleteMany(Filters.eq("page_cache", page))
        val include = "artists,animethemes.anime"
        val songs = RestClient.request(
            "${BaseUrls.ANIME_THEMES}song?include=$include&page[number]=$page&page[size]=25",
            SongSearch.serializer()
        ).songs?.map { it.toSongEntity() }.orEmpty()
        parseDataToDocuments(songs, SongEntity.serializer())
            .onEach { it.append("page_cache", page) }
            .also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private fun refreshAnimeDirectoryType(type: AnimeTypes, force: Boolean): Boolean {
        val key = "${TimerKey.ANIME_TYPE}${type.name.lowercase()}"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(key)
        collection.deleteMany(Document())
        val documents = database.getCollection(Collections.ANIME_DIRECTORY)
            .find(Filters.eq("type", type.name))
            .toList()
            .map { Document.parse(Json.encodeToString(documentToAnimeDirectoryEntity(it))) }
        if (documents.isNotEmpty()) collection.insertMany(documents)
        timers.update(key)
        return true
    }

    private suspend fun refreshAnimeRanking(type: String, filter: String, page: Int, force: Boolean): Boolean {
        val key = "${Collections.ANIME_RANKING}_${type}_${filter}_${page}"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.ANIME_RANKING)
        collection.deleteMany(Filters.and(Filters.eq("page", page), Filters.eq("type", type), Filters.eq("subtype", filter)))
        val response = RestClient.request(
            BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?type=$type&page=$page&filter=$filter",
            AnimeSearch.serializer()
        ).data?.map { it.toAnimeTopEntity(page, "anime", type, filter) }
        parseDataToDocuments(response, AnimeTopEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshAnimeTopTenRanking(type: String, filter: String, force: Boolean): Boolean {
        val key = "${Collections.ANIME_RANKING}_${Collections.TOP_TEN}_${type}_${filter}"
        if (!shouldRefresh(key, 7, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.ANIME_RANKING_TOP_TEN)
        collection.deleteMany(Filters.and(Filters.eq("type", type), Filters.eq("subtype", filter)))
        val response = RestClient.request(
            BaseUrls.JIKAN + Endpoints.TOP_ANIME + "?type=$type&filter=$filter",
            AnimeSearch.serializer()
        ).data?.map { it.toAnimeTopEntity(0, "anime", type, filter) }.orEmpty().take(11).distinctBy { it.malId }
        parseDataToDocuments(response, AnimeTopEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshMangaRanking(type: String, filter: String, page: Int, force: Boolean): Boolean {
        val key = "${Collections.MANGA_RANKING}_${type}_${filter}_${page}"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.MANGA_RANKING)
        collection.deleteMany(Filters.and(Filters.eq("page", page), Filters.eq("type", type), Filters.eq("subtype", filter)))
        val response = RestClient.request(
            BaseUrls.JIKAN + Endpoints.TOP_MANGA + "?type=$type&page=$page&filter=$filter",
            MangaSearch.serializer()
        ).data?.map { it.toMangaTopEntity(page, "manga", type, filter) }
        parseDataToDocuments(response, MangaTopEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshPeopleRanking(page: Int, force: Boolean): Boolean {
        val key = "${Collections.PEOPLE_RANKING}_${page}"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.PEOPLE_RANKING)
        collection.deleteMany(Filters.eq("page", page))
        val response = RestClient.request(BaseUrls.JIKAN + Endpoints.TOP_PEOPLE + "?page=$page", PeopleSearch.serializer())
            .data?.map { it.toPeopleTopEntity(page, "people") }
        parseDataToDocuments(response, PeopleTopEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }

    private suspend fun refreshCharacterRanking(page: Int, force: Boolean): Boolean {
        val key = "${Collections.CHARACTER_RANKING}_${page}"
        if (!shouldRefresh(key, 30, TimeUnit.DAY, force)) return false
        val collection = database.getCollection(Collections.CHARACTER_RANKING)
        collection.deleteMany(Filters.eq("page", page))
        val response = RestClient.request(BaseUrls.JIKAN + Endpoints.TOP_CHARACTER + "?page=$page", CharacterSearch.serializer())
            .data?.map { it.toCharacterTopEntity(page, "character") }
        parseDataToDocuments(response, CharacterTopEntity.serializer()).also { if (it.isNotEmpty()) collection.insertMany(it) }
        timers.update(key)
        return true
    }
}
