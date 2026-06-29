package com.jeluchu.features.gallery.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProcessedPost(
    @SerialName("id")
    val id: Int,

    @SerialName("large_image")
    val largeImage: String,

    @SerialName("thumbnail")
    val thumbnail: String,

    @SerialName("provider")
    val provider: String = "anime-pictures",

    @SerialName("source_url")
    val sourceUrl: String = "",

    @SerialName("original_url")
    val originalUrl: String = "",

    @SerialName("width")
    val width: Int,

    @SerialName("height")
    val height: Int,

    @SerialName("pubtime")
    val pubtime: String,

    @SerialName("size")
    val size: Int,

    @SerialName("erotics")
    val erotics: Boolean,

    @SerialName("spoiler")
    val spoiler: Boolean,

    @SerialName("have_alpha")
    val haveAlpha: Boolean,

    @SerialName("rating")
    val rating: String = "",

    @SerialName("score")
    val score: Int = 0,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("page")
    val page: Int = 0,

    @SerialName("totalPages")
    val totalPages: Int = 0,

    @SerialName("totalItems")
    val totalItems: Int = 0,

    @SerialName("query")
    val query: String = "0"
)
