package com.jeluchu.features.gallery.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProcessedPost(
    @SerialName("id")
    val id: Int,

    @SerialName("image")
    val image: String,

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

    @SerialName("page")
    val page: Int = 0,

    @SerialName("query")
    val query: String = "0"
)
