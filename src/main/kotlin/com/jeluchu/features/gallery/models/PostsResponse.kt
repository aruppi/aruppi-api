package com.jeluchu.features.gallery.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PostsResponse(
    @SerialName("exclusive_tag")
    val exclusiveTag: JsonElement? = null,

    @SerialName("posts_per_page")
    val postsPerPage: Int = 0,

    @SerialName("response_posts_count")
    val responsePostsCount: Int = 0,

    @SerialName("page_number")
    val pageNumber: Int = 0,

    @SerialName("posts")
    val posts: List<Post> = emptyList(),

    @SerialName("posts_count")
    val postsCount: Int = 0,

    @SerialName("max_pages")
    val maxPages: Int = 0
) {
    @Serializable
    data class Post(
        @SerialName("id")
        val id: Int = 0,

        @SerialName("md5")
        val md5: String = "",

        @SerialName("md5_pixels")
        val md5Pixels: String = "",

        @SerialName("width")
        val width: Int = 0,

        @SerialName("height")
        val height: Int = 0,

        @SerialName("pubtime")
        val pubtime: String = "",

        @SerialName("datetime")
        val datetime: String = "",

        @SerialName("score")
        val score: Double = 0.0,

        @SerialName("score_number")
        val scoreNumber: Int = 0,

        @SerialName("size")
        val size: Int = 0,

        @SerialName("download_count")
        val downloadCount: Int = 0,

        @SerialName("erotics")
        val erotics: Int = 0,

        @SerialName("color")
        val color: List<Int> = emptyList(),

        @SerialName("ext")
        val ext: String = "",

        @SerialName("status")
        val status: Int = 0,

        @SerialName("status_type")
        val statusType: Int = 0,

        @SerialName("redirect_id")
        val redirectId: String? = null,

        @SerialName("spoiler")
        val spoiler: Boolean = false,

        @SerialName("have_alpha")
        val haveAlpha: Boolean = false,

        @SerialName("tags_count")
        val tagsCount: Int = 0,

        @SerialName("artefacts_degree")
        val artefactsDegree: Double = 0.0,

        @SerialName("smooth_degree")
        val smoothDegree: Double = 0.0
    )
}
