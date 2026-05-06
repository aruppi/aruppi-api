package com.jeluchu.core.models

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val code: String? = null,
    val status: Int? = null,
    val path: String? = null,
    val requestId: String? = null,
    val version: String? = null,
    val documentation: DocumentationLinks? = null
)
