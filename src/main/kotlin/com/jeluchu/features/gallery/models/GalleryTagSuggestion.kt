package com.jeluchu.features.gallery.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalleryTagSuggestion(
    @SerialName("name")
    val name: String,
    @SerialName("label")
    val label: String = name.replace('_', ' '),
    @SerialName("post_count")
    val postCount: Int = 0,
    @SerialName("category")
    val category: Int = 0
)

@Serializable
data class DanbooruTag(
    @SerialName("name")
    val name: String = "",
    @SerialName("post_count")
    val postCount: Int = 0,
    @SerialName("category")
    val category: Int = 0
)
