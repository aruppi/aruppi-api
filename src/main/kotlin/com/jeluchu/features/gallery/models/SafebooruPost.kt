package com.jeluchu.features.gallery.models

data class SafebooruPost(
    val id: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val createdAt: String = "",
    val fileSize: Int = 0,
    val rating: String = "s",
    val score: Int = 0,
    val tags: String = "",
    val fileUrl: String = "",
    val sampleUrl: String = "",
    val previewUrl: String = "",
    val source: String = ""
)
