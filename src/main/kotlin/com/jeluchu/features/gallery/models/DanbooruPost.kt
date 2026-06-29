package com.jeluchu.features.gallery.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DanbooruPost(
    @SerialName("id")
    val id: Int = 0,

    @SerialName("image_width")
    val imageWidth: Int = 0,

    @SerialName("image_height")
    val imageHeight: Int = 0,

    @SerialName("created_at")
    val createdAt: String = "",

    @SerialName("file_size")
    val fileSize: Int = 0,

    @SerialName("rating")
    val rating: String = "",

    @SerialName("score")
    val score: Int = 0,

    @SerialName("tag_string")
    val tagString: String = "",

    @SerialName("file_url")
    val fileUrl: String? = null,

    @SerialName("large_file_url")
    val largeFileUrl: String? = null,

    @SerialName("preview_file_url")
    val previewFileUrl: String? = null,
)
