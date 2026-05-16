package com.jeluchu.core.utils

import com.jeluchu.core.enums.TimeUnit
import kotlinx.serialization.Serializable

@Serializable
enum class Timers(
    val key: String,
    val amount: Long = 1,
    val unit: TimeUnit = TimeUnit.HOUR
) {
    /** NEWS TIMERS **/
    NEWS_ES(key = Collections.NEWS_ES, amount = 1, unit = TimeUnit.DAY),
    NEWS_EN(key = Collections.NEWS_EN, amount = 1, unit = TimeUnit.DAY),

    /** THEMES TIMERS **/
    ANIME_THEMES(key = Collections.ANIME_THEMES, amount = 7, unit = TimeUnit.DAY),
}