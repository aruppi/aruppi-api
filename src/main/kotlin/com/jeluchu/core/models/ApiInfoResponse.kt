package com.jeluchu.core.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiInfoResponse(
    val version: String,
    val documentation: DocumentationLinks
)
