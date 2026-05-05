package com.jeluchu.core.messages

import com.jeluchu.core.enums.*

sealed class ErrorMessages(val message: String) {
    data object NotFound : ErrorMessages("The requested endpoint does not exist in this API version")
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
    data object MissingTags : ErrorMessages("No tags provided")
    data object InvalidRequest : ErrorMessages("The request could not be processed because one or more parameters are invalid")
    data object InvalidValueTopPage : ErrorMessages("Value 26 is higher than the configured '25' max value")
    data object UnauthorizedMongo : ErrorMessages("Check the MongoDb Connection String to be able to correctly access this request.")
    data object ArtistNotFound : ErrorMessages("No artist found with that slug")
    data object MissingArtistSlug : ErrorMessages("Missing 'slug' path parameter")
    data object SongNotFound : ErrorMessages("No songs found matching that query")
    data object InvalidAnimeThemeSlug : ErrorMessages("No anime theme found with that slug")
    data object InvalidThemesSongsEndpoint : ErrorMessages("Invalid themes songs endpoint. Valid endpoints are: /api/v5/themes/songs and /api/v5/themes/songs/random")
    data object InvalidThemesAnimeEndpoint : ErrorMessages("Invalid anime themes endpoint. Valid endpoints are: /api/v5/themes/anime, /api/v5/themes/anime/{slug}, and /api/v5/themes/anime/{slug}/random")
    data object InvalidThemesArtistsEndpoint : ErrorMessages("Invalid themes artists endpoint. Valid endpoints are: /api/v5/themes/artists and /api/v5/themes/artists/{slug}")
    data object InternalServerError : ErrorMessages("An unexpected error occurred while processing the request")
}
