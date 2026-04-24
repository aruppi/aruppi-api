package com.jeluchu.core.messages

import com.jeluchu.core.enums.*

sealed class ErrorMessages(val message: String) {
    data object NotFound : ErrorMessages("Nyaaaaaaaan! This request has not been found by our alpaca-neko")
    data object NotFoundContent : ErrorMessages("No related content found")
    data object AnimeNotFound : ErrorMessages("This malId is not in our database")
    data object InvalidMalId : ErrorMessages("The provided id of malId is invalid")
    data object InvalidDay : ErrorMessages("Invalid 'day' parameter. Valid values are: ${Day.entries.joinToString(", ") { it.name.lowercase() }}")
    data object InvalidAnimeType : ErrorMessages("Invalid 'type' parameter. Valid values are: ${AnimeTypes.entries.joinToString(", ") { it.name.lowercase() }}")
    data object InvalidMangaType : ErrorMessages("Invalid 'type' parameter. Valid values are: ${MangaTypes.entries.joinToString(", ") { it.name.lowercase() }}")
    data object InvalidSizeAndPage : ErrorMessages("Invalid page and size parameters")
    data object InvalidTopAnimeType : ErrorMessages("Invalid 'type' parameter. Valid values are: $animeTypesErrorList")
    data object InvalidAnimeStatusType : ErrorMessages("Invalid 'status' parameter. Valid values are: $animeStatusTypesErrorList")
    data object InvalidTopAnimeFilterType : ErrorMessages("Invalid 'type' parameter. Valid values are: $animeFilterTypesErrorList")
    data object InvalidTopMangaType : ErrorMessages("Invalid 'type' parameter. Valid values are: $mangaTypesErrorList")
    data object InvalidTopMangaFilterType : ErrorMessages("Invalid 'type' parameter. Valid values are: $mangaFilterTypesErrorList")
    data object InvalidInput : ErrorMessages("Invalid input provided")
    data object InvalidValueTopPage : ErrorMessages("Value 26 is higher than the configured '25' max value")
    data object UnauthorizedMongo : ErrorMessages("Check the MongoDb Connection String to be able to correctly access this request.")
    data object ArtistNotFound : ErrorMessages("No artist found with that slug")
    data object MissingArtistSlug : ErrorMessages("Missing 'slug' path parameter")
    data object SongNotFound : ErrorMessages("No songs found matching that query")
}