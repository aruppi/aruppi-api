package com.jeluchu.core.utils

object BaseUrls {
    const val JIKAN = "https://api.jikan.moe/v4/"
    const val ANIME_THEMES = "https://api.animethemes.moe/"
    const val ANIME_PICTURES = "https://api.anime-pictures.net/api/v3/"
    const val DANBOORU = "https://danbooru.donmai.us"
    const val SAFEBOORU = "https://safebooru.org"
}

object RssUrls {
    // Anitakume Podcast
    const val ANITAKUME = "https://www.ivoox.com/feed_fg_f1660716_filtro_1.xml"

    // Spanish news
    const val SOMOSKUDASAI = "https://somoskudasai.com/noticias/feed/"
    const val MANGALATAM = "https://www.mangalatam.com/feeds/posts/default?alt=rss"
    const val CRUNCHYROLL = "https://cr-news-api-service.prd.crunchyrollsvc.com/v1/es-ES/rss"
    const val RAMENPARADOS = "https://ramenparados.com/feed/"
    const val ANMOSUGOI = "https://www.anmosugoi.com/feed/"

    // English news
    const val OTAKUMODE = "https://otakumode.com/news/feed"
    const val MYANIMELIST = "https://myanimelist.net/rss/news.xml"
    const val HONEYSANIME = "https://honeysanime.com/feed/"
    const val ANIMEHUNCH = "https://animehunch.com/feed/"
}

object RssSources {
    const val SOMOSKUDASAI = "Somos Kudasai"
    const val MANGALATAM = "MangaLatam"
    const val CRUNCHYROLL = "Crunchyroll"
    const val RAMENPARADOS = "Ramen para dos"
    const val ANMOSUGOI = "Anmo Sugoi"
    const val OTAKUMODE = "Tokyo Otaku Mode News"
    const val MYANIMELIST = "MyAnimeList"
    const val HONEYSANIME = "Honey's anime"
    const val ANIMEHUNCH = "My Anime For Life"
}

object Endpoints {
    const val POSTS = "posts"
    const val ANIME = "anime"
    const val EPISODES = "episodes"
    const val SCHEDULES = "schedules"
    const val TOP_ANIME = "top/anime"
    const val TOP_MANGA = "top/manga"
    const val TOP_PEOPLE = "top/people"
    const val TOP_CHARACTER = "top/characters"
    const val LAST_EPISODES = "list/latest-episodes"
}

object Routes {
    const val ES = "/es"
    const val EN = "/en"
    const val TOP = "/top"
    const val TAGS = "/tags"
    const val NEWS = "/news"
    const val ANIME = "/anime"
    const val MANGA = "/manga"
    const val PEOPLE = "/people"
    const val SEARCH = "/search"
    const val TOP_TEN = "/topTen"
    const val GALLERY = "/gallery"
    const val SCHEDULE = "/schedule"
    const val RADIO_STATIONS = "/radio"
    const val LAST_POST = "/lastPosts"
    const val DIRECTORY = "/directory"
    const val ANITAKUME = "/anitakume"
    const val CHARACTER = "/characters"
    const val LAST_EPISODES = "/lastEpisodes"
    const val EPISODES = "/episodes"
    const val ID = "/{id}"
    const val TYPE = "/{type}"
    const val SEASON = "/season"
    const val SEASON_PARAMS = "/{year}/{season}"
    const val DAY = "/{day}"
    const val THEMES = "/themes"
    const val SUGGESTIONS = "/suggestions"
    const val UPCOMING = "/upcoming"
    const val YEAR_INDEX = "/yearIndex"
    const val RANDOM = "/random"
    const val ARTISTS = "/artists"
    const val SLUG = "/{slug}"
    const val SONGS = "/songs"
}

object Path {
    const val RANDOM = "random"
}

object TimerKey {
    const val KEY = "key"
    const val SCHEDULE = "schedule"
    const val RADIO = "radio_station"
    const val LAST_UPDATED = "lastUpdated"
    const val ANIME_TYPE = "anime_"
    const val THEMES = "themes_"
    const val EPISODES = "episodes_"
    const val LAST_EPISODES = "last_episodes"
    const val UPCOMING_SEASON = "upcoming_season"
}

object Collections {
    const val TIMERS = "timers"
    const val TOP_TEN = "top_ten"
    const val NEWS_ES = "news_es"
    const val NEWS_EN = "news_en"
    const val SCHEDULES = "schedule"
    const val ANITAKUME = "anitakume"
    const val RADIO = "radio_stations"
    const val ANIME_THEMES = "anime_themes"
    const val ARTISTS_INDEX = "artists_index"
    const val SONGS_INDEX = "songs_index"
    const val ANIME_THEMES_DETAIL = "anime_themes_detail"
    const val LAST_EPISODES = "last_episodes"
    const val UPCOMING_SEASON = "upcoming_season"
    const val ANIME_RANKING = "anime_ranking"
    const val MANGA_RANKING = "manga_ranking"
    const val MANGA_DIRECTORY = "manga_directory"
    const val MANGA_DETAIL = "manga_detail"
    const val PEOPLE_RANKING = "people_ranking"
    const val ANIME_DIRECTORY = "anime_directory"
    const val CHARACTER_RANKING = "character_ranking"
    const val ANIME_PICTURES_QUERY = "anime_pictures_query"
    const val ANIME_RANKING_TOP_TEN = "anime_ranking_top_ten"
    const val ANIME_PICTURES_RECENT = "anime_pictures_recent"
}
