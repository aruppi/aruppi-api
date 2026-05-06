package com.jeluchu.core.enums

import kotlinx.serialization.Serializable

@Serializable
enum class MangaStatusTypes {
    PUBLISHING,
    COMPLETE,
    HIATUS,
    DISCONTINUED,
    UPCOMING
}

val mangaStatusTypesErrorList = MangaStatusTypes.entries.joinToString(", ") { it.name.lowercase() }
fun parseMangaStatusType(status: String) = MangaStatusTypes.entries.firstOrNull { it.name.equals(status, ignoreCase = true) }
