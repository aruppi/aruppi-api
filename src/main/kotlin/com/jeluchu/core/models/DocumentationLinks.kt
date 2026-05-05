package com.jeluchu.core.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentationLinks(
    val redoc: String,
    val swagger: String,
    val openapi: String
)
