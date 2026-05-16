package com.jeluchu.core.utils

import kotlinx.serialization.Serializable

@Serializable
enum class NewsSources(
    val link: String,
    val source: String
) {
    SOMOSKUDASAI(link = RssUrls.SOMOSKUDASAI, source = RssSources.SOMOSKUDASAI),
    MANGALATAM(link = RssUrls.MANGALATAM, source = RssSources.MANGALATAM),
    CRUNCHYROLL(link = RssUrls.CRUNCHYROLL, source = RssSources.CRUNCHYROLL),
    RAMENPARADOS(link = RssUrls.RAMENPARADOS, source = RssSources.RAMENPARADOS),
    ANMOSUGOI(link = RssUrls.ANMOSUGOI, source = RssSources.ANMOSUGOI),
    OTAKUMODE(link = RssUrls.OTAKUMODE, source = RssSources.OTAKUMODE),
    MYANIMELIST(link = RssUrls.MYANIMELIST, source = RssSources.MYANIMELIST),
    HONEYSANIME(link = RssUrls.HONEYSANIME, source = RssSources.HONEYSANIME),
    ANIMEHUNCH(link = RssUrls.ANIMEHUNCH, source = RssSources.ANIMEHUNCH)
}