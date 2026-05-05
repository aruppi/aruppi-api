package com.jeluchu.core.models

import kotlinx.serialization.Serializable

@Serializable
data class PreloadResponse(
    val refreshed: Int,
    val skipped: Int,
    val failed: Int,
    val results: List<PreloadTaskResult>
)

@Serializable
data class PreloadTaskResult(
    val key: String,
    val status: String,
    val message: String? = null
)
