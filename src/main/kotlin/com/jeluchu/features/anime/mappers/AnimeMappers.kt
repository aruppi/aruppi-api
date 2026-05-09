package com.jeluchu.features.anime.mappers

import com.jeluchu.core.extensions.*
import com.jeluchu.features.anime.models.anime.*
import com.jeluchu.features.anime.models.directory.AnimeTypeEntity
import com.jeluchu.features.anime.models.lastepisodes.LastEpisodeEntity
import com.jeluchu.features.rankings.models.AnimeTopEntity
import com.jeluchu.features.rankings.models.CharacterTopEntity
import com.jeluchu.features.rankings.models.MangaTopEntity
import com.jeluchu.features.rankings.models.PeopleTopEntity
import com.jeluchu.features.schedule.models.DayEntity
import com.jeluchu.features.themes.models.anime.*
import com.jeluchu.features.themes.models.artist.ArtistEntity
import com.jeluchu.features.themes.models.artist.ArtistSong
import com.jeluchu.features.themes.models.artist.ArtistSongTheme
import com.jeluchu.features.themes.models.artist.SimpleArtistEntity
import com.jeluchu.features.themes.models.song.SongArtist
import com.jeluchu.features.themes.models.song.SongEntity
import com.jeluchu.features.themes.models.song.SongTheme
import org.bson.Document

fun documentToMoreInfoEntity(doc: Document): MoreInfoEntity {
    return MoreInfoEntity(
        id = doc.getObjectId("_id").toString(),
        malId = doc.getIntSafe("malId"),
        rank = doc.getIntSafe("rank"),
        title = doc.getStringSafe("title"),
        episodes = doc.getIntSafe("episodes"),
        episodeList = doc.getListSafe<Document>("episodeList").map { documentToEpisodeInfo(it) },
        type = doc.getStringSafe("type"),
        status = doc.getStringSafe("status"),
        season = doc.getDocumentSafe("season")?.let { documentToSeasonalEntity(it) },
        poster = doc.getStringSafe("poster"),
        cover = doc.getStringSafe("cover"),
        duration = doc.getDocumentSafe("duration")?.let { documentToEpisodeDurationEntity(it) },
        score = doc.getStringSafe("score"),
        titles = doc.getDocumentSafe("titles")?.let { documentToOtherTitlesEntity(it) },
        studios = doc.getListSafe<Document>("studios").map { documentToCompanies(it) },
        producers = doc.getListSafe<Document>("producers").map { documentToCompanies(it) },
        licensors = doc.getListSafe<Document>("licensors").map { documentToCompanies(it) },
        relations = doc.getListSafe<Document>("relations").map { documentToRelated(it) },
        promo = doc.getDocumentSafe("promo")?.let { documentToVideoPromo(it) },
        tags = doc.getDocumentSafe("genres")?.let { documentToMultipleLanguageLists(it) },
        synopsis = doc.getDocumentSafe("synopsis")?.let { documentToMultipleLanguage(it) },
        staff = doc.getListSafe<Document>("staff").map { documentToStaff(it) },
        characters = doc.getListSafe<Document>("characters").map { documentToCharacter(it) },
        streaming = doc.getListSafe<Document>("streaming").map { documentToExternalLinks(it) },
        urls = doc.getListSafe<String>("urls").takeIf { it.isNotEmpty() } ?: listOf(doc.getStringSafe("url")).filter { it.isNotEmpty() },
        broadcast = doc.getDocumentSafe("broadcast")?.let { documentToAnimeBroadcast(it) },
        external = doc.getListSafe<Document>("external").map { documentToExternalLinks(it) },
        stats = doc.getDocumentSafe("stats")?.let { documentToStatistics(it) },
        nsfw = doc.getBooleanSafe("nsfw", false),
        ageRating = doc.getStringSafe("ageRating"),
        aired = doc.getDocumentSafe("aired")?.let { documentToAiringTime(it) },
        themes = doc.getDocumentSafe("theme")?.let { documentToThemes(it) }
    )
}

fun documentToEpisodeInfo(doc: Document): EpisodeInfo {
    return EpisodeInfo(
        number = doc.getIntSafe("number"),
        seasonNumber = doc.getIntSafe("season_number"),
        relativeNumber = doc.getIntSafe("relative_number"),
        airdate = doc.getStringSafe("airdate"),
        duration = doc.getIntSafe("duration"),
        thumbnail = doc.getStringSafe("thumbnail"),
        synopsis = doc.getDocumentSafe("synopsis")?.let { documentToMultipleLanguage(it) },
        titles = doc.getDocumentSafe("titles")?.let { documentToMultipleLanguageTitles(it) },
        score = doc.getDoubleSafe("score"),
        filler = doc.getBooleanSafe("filler", false),
        recap = doc.getBooleanSafe("recap", false)
    )
}

fun documentToMultipleLanguage(doc: Document): MultipleLanguage {
    return MultipleLanguage(
        es = doc.getStringSafe("es"),
        en = doc.getStringSafe("en")
    )
}

fun documentToMultipleLanguageTitles(doc: Document): MultipleLanguageTitles {
    return MultipleLanguageTitles(
        es = doc.getStringSafe("es"),
        en = doc.getStringSafe("en"),
        jp = doc.getStringSafe("jp"),
        romaji_jp = doc.getStringSafe("romaji_jp")
    )
}

fun documentToSeasonalEntity(doc: Document): SeasonalEntity {
    return SeasonalEntity(
        station = doc.getStringSafe("station"),
        year = doc.getIntSafe("year")
    )
}

fun documentToEpisodeDurationEntity(doc: Document): EpisodeDurationEntity {
    return EpisodeDurationEntity(
        unit = doc.getStringSafe("unit"),
        value = doc.getIntSafe("value")
    )
}

fun documentToOtherTitlesEntity(doc: Document): OtherTitlesEntity {
    return OtherTitlesEntity(
        synonyms = doc.getListSafe<String>("synonyms"),
        abbreviatedTitles = doc.getListSafe<String>("abbreviated_titles")
    )
}

fun documentToCompanies(doc: Document): Companies {
    return Companies(
        malId = doc.getIntSafe("malId", 0),
        name = doc.getStringSafe("name"),
        type = doc.getStringSafe("type"),
        url = doc.getStringSafe("url")
    )
}

fun documentToRelated(doc: Document): Related {
    return Related(
        entry = doc.getListSafe<Document>("entry").map { documentToCompanies(it) },
        relation = doc.getStringSafe("relation")
    )
}

fun documentToVideoPromo(doc: Document): VideoPromo {
    return VideoPromo(
        embedUrl = doc.getStringSafe("embedUrl").toYouTubeWatchUrl().orEmpty(),
        url = doc.getStringSafe("url"),
        youtubeId = doc.getStringSafe("youtubeId"),
        images = doc.getDocumentSafe("images")?.let { documentToImages(it) } ?: Images()
    )
}

fun documentToImages(doc: Document): Images {
    return Images(
        generic = doc.getStringSafe("generic"),
        small = doc.getStringSafe("small"),
        medium = doc.getStringSafe("medium"),
        large = doc.getStringSafe("large"),
        maximum = doc.getStringSafe("maximum")
    )
}

fun documentToMultipleLanguageLists(doc: Document): MultipleLanguageLists {
    return MultipleLanguageLists(
        es = doc.getListSafe<String>("es"),
        en = doc.getListSafe<String>("en")
    )
}

fun documentToStaff(doc: Document): Staff {
    return Staff(
        person = doc.getDocumentSafe("person")?.let { documentToIndividual(it) } ?: Individual(),
        positions = doc.getListSafe<String>("positions")
    )
}

fun documentToCharacter(doc: Document): Character {
    return Character(
        malId = doc.getIntSafe("mal_id", 0),
        url = doc.getStringSafe("url"),
        name = doc.getStringSafe("name"),
        images = doc.getStringSafe("images"),
        role = doc.getStringSafe("role"),
        voiceActor = doc.getListSafe<Document>("voice_actor").map { documentToActor(it) }
    )
}

fun documentToActor(doc: Document): Actor {
    return Actor(
        person = doc.getDocumentSafe("person")?.let { documentToIndividual(it) } ?: Individual(),
        language = doc.getStringSafe("language")
    )
}

fun documentToIndividual(doc: Document): Individual {
    return Individual(
        malId = doc.getIntSafe("malId", 0),
        url = doc.getStringSafe("url"),
        name = doc.getStringSafe("name"),
        images = doc.getStringSafe("images")
    )
}

fun documentToExternalLinks(doc: Document): ExternalLinks {
    return ExternalLinks(
        url = doc.getStringSafe("url"),
        name = doc.getStringSafe("name")
    )
}

fun documentToStatistics(doc: Document): Statistics {
    return Statistics(
        completed = doc.getIntSafe("completed"),
        dropped = doc.getIntSafe("dropped"),
        onHold = doc.getIntSafe("onHold"),
        planToWatch = doc.getIntSafe("planToWatch"),
        scores = doc.getListSafe<Document>("scores").map { documentToScore(it) },
        total = doc.getIntSafe("total"),
        watching = doc.getIntSafe("watching")
    )
}

fun documentToAiringTime(doc: Document): AiringTime {
    return AiringTime(
        from = doc.getStringSafe("from"),
        to = doc.getStringSafe("to")
    )
}

fun documentToThemes(doc: Document): Themes {
    return Themes(
        openings = doc.getListSafe<String>("openings"),
        endings = doc.getListSafe<String>("endings")
    )
}

fun documentToAnimeBroadcast(doc: Document): AnimeBroadcast {
    return AnimeBroadcast(
        day = doc.getStringSafe("day"),
        time = doc.getStringSafe("time"),
        timezone = doc.getStringSafe("timezone")
    )
}

fun documentToScore(doc: Document): Score {
    return Score(
        percentage = when (val value = doc["percentage"]) {
            is Double -> value
            is Int -> value.toDouble()
            else -> 0.0
        },
        score = doc.getIntSafe("score", 0),
        votes = doc.getIntSafe("votes", 0)
    )
}

fun documentToScheduleDayEntity(doc: Document) = DayEntity(
    day = doc.getStringSafe("day"),
    malId = doc.getIntSafe("malId"),
    image = doc.getStringSafe("image"),
    title = doc.getStringSafe("title")
)

fun documentToAnimeTopEntity(position: Int, doc: Document) = AnimeTopEntity(
    rank = position + 1,
    malId = doc.getIntSafe("malId"),
    score = doc.getFloatSafe("score"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("image"),
    url = doc.getStringSafe("url"),
    promo = doc.getDocumentSafe("promo")?.getStringSafe("embedUrl").toYouTubeWatchUrl().orEmpty(),
    season = doc.getStringSafe("season"),
    year = doc.getIntSafe("year"),
    airing = doc.getBooleanSafe("airing"),
    type = doc.getStringSafe("type"),
    subtype = doc.getStringSafe("subtype"),
)

fun documentToAnimeLastEpisodeEntity(doc: Document) = LastEpisodeEntity(
    malId = doc.getIntSafe("malId"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("image"),
    day = doc.getStringSafe("day"),
    time = doc.getStringSafe("time"),
    score = doc.getStringSafe("score"),
    timezone = doc.getStringSafe("timezone")
)

fun documentToMangaTopEntity(position: Int, doc: Document) = MangaTopEntity(
    malId = doc.getIntSafe("malId"),
    rank = position + 1,
    score = doc.getDoubleSafe("score"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("image"),
    url = doc.getStringSafe("url"),
    volumes = doc.getIntSafe("volumes"),
    chapters = doc.getIntSafe("chapters"),
    status = doc.getStringSafe("status"),
    type = doc.getStringSafe("type"),
    subtype = doc.getStringSafe("subtype"),
)

fun documentToPeopleTopEntity(position: Int, doc: Document) = PeopleTopEntity(
    rank = position + 1,
    malId = doc.getIntSafe("malId"),
    name = doc.getStringSafe("name"),
    givenName = doc.getStringSafe("givenName"),
    familyName = doc.getStringSafe("familyName"),
    image = doc.getStringSafe("image"),
    birthday = doc.getStringSafe("birthday"),
    top = doc.getStringSafe("top"),
)

fun documentToCharacterTopEntity(position: Int, doc: Document) = CharacterTopEntity(
    rank = position + 1,
    malId = doc.getIntSafe("malId"),
    name = doc.getStringSafe("name"),
    nameKanji = doc.getStringSafe("nameKanji"),
    image = doc.getStringSafe("image"),
    top = doc.getStringSafe("top"),
)

fun documentToAnimeTypeEntity(doc: Document) = AnimeTypeEntity(
    year = doc.getIntSafe("year"),
    malId = doc.getIntSafe("malId"),
    type = doc.getStringSafe("type"),
    score = doc.getStringSafe("score"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("poster"),
    season = doc.getStringSafe("season")
)

fun documentToAnimeDirectoryEntity(doc: Document) = AnimeTypeEntity(
    year = doc.getIntSafe("year"),
    malId = doc.getIntSafe("malId"),
    type = doc.getStringSafe("type"),
    score = doc.getStringSafe("score"),
    title = doc.getStringSafe("title"),
    image = doc.getStringSafe("image"),
    season = doc.getStringSafe("season")
)

fun documentToAnimesThemeEntity(doc: Document) = Anime(
    year = doc.getIntSafe("year"),
    slug = doc.getStringSafe("slug"),
    name = doc.getStringSafe("name"),
    image = doc.getStringSafe("image"),
    season = doc.getStringSafe("season"),
    themes = doc.getListSafe<Document>("themes").map { documentToAnimeVideoTheme(it) }
)

fun documentToAnimeVideoTheme(doc: Document) = AnimeVideoTheme(
    id = doc.getIntSafe("id"),
    slug = doc.getStringSafe("slug"),
    type = doc.getStringSafe("type"),
    sequence = doc.getIntSafe("sequence"),
    entries = doc.getListSafe<Document>("animethemeentries").map { documentToAnimeThemeEntry(it) }
)

fun documentToAnimeThemeEntry(doc: Document) = AnimeThemeEntry(
    id = doc.getIntSafe("id"),
    nsfw = doc.getBooleanSafe("nsfw"),
    notes = doc.getStringSafe("notes"),
    spoiler = doc.getBooleanSafe("spoiler"),
    episodes = doc.getStringSafe("episodes"),
    videos = doc.getListSafe<Document>("videos").map { documentToVideo(it) }
)

fun documentToVideo(doc: Document) = Video(
    nc = doc.getBooleanSafe("nc"),
    size = doc.getIntSafe("size"),
    link = doc.getStringSafe("link"),
    uncen = doc.getBooleanSafe("uncen"),
    source = doc.getStringSafe("source"),
    subbed = doc.getBooleanSafe("subbed"),
    lyrics = doc.getBooleanSafe("lyrics"),
    overlap = doc.getStringSafe("overlap"),
    filename = doc.getStringSafe("filename"),
    resolution = doc.getIntSafe("resolution")
)

fun documentToSimpleArtistEntity(doc: Document) = SimpleArtistEntity(
    id = doc.getIntSafe("id").takeIf { it != 0 } ?: doc.getIntSafe("malId").takeIf { it != 0 },
    name = doc.getStringSafe("name"),
    slug = doc.getStringSafe("slug"),
    image = doc.getStringSafe("image").ifBlank {
        doc.getListSafe<Document>("images")
            .firstOrNull { it.getStringSafe("facet") == "Large Cover" }
            ?.getStringSafe("link")
            ?.ifBlank {
                doc.getListSafe<Document>("images")
                    .firstOrNull()
                    ?.getStringSafe("link")
                    .orEmpty()
            }
    }
)

fun documentToArtistSong(doc: Document) = ArtistSong(
    id = doc.getIntSafe("id"),
    title = doc.getStringSafe("title"),
    themes = doc.getListSafe<Document>("themes").map { documentToArtistSongTheme(it) }
)

fun documentToArtistSongTheme(doc: Document) = ArtistSongTheme(
    type = doc.getStringSafe("type"),
    slug = doc.getStringSafe("slug"),
    sequence = doc.getIntSafe("sequence"),
    animeName = doc.getStringSafe("animeName"),
    animeSlug = doc.getStringSafe("animeSlug"),
    videoLink = doc.getStringSafe("videoLink")
)


fun documentToSongEntity(doc: Document) = SongEntity(
    id = doc.getIntSafe("id"),
    title = doc.getStringSafe("title"),
    artists = doc.getListSafe<Document>("artists").map { documentToSongArtist(it) },
    themes = doc.getListSafe<Document>("themes").map { documentToSongTheme(it) }
)

fun documentToSongArtist(doc: Document) = SongArtist(
    id = doc.getIntSafe("id"),
    name = doc.getStringSafe("name"),
    slug = doc.getStringSafe("slug")
)

fun documentToSongTheme(doc: Document) = SongTheme(
    type = doc.getStringSafe("type"),
    slug = doc.getStringSafe("slug"),
    sequence = doc.getIntSafe("sequence"),
    animeName = doc.getStringSafe("animeName"),
    animeSlug = doc.getStringSafe("animeSlug"),
    videoLink = doc.getStringSafe("videoLink")
)
