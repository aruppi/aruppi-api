package com.jeluchu.core.configuration

import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.ErrorResponse
import com.jeluchu.core.models.DocumentationLinks
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun Application.initInstallers() {
    install(plugin = StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    error = ErrorMessages.InvalidRequest.message,
                    status = HttpStatusCode.BadRequest.value,
                    path = call.request.path(),
                    version = ApiMetadata.version,
                    documentation = documentationLinks()
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ErrorResponse(
                    error = cause.message ?: ErrorMessages.InvalidRequest.message,
                    status = HttpStatusCode.BadRequest.value,
                    path = call.request.path(),
                    version = ApiMetadata.version,
                    documentation = documentationLinks()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error while processing ${call.request.path()}", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse(
                    error = ErrorMessages.InternalServerError.message,
                    status = HttpStatusCode.InternalServerError.value,
                    path = call.request.path(),
                    version = ApiMetadata.version,
                    documentation = documentationLinks()
                )
            )
        }
    }

    install(plugin = ContentNegotiation) {
        json()
    }
}

private fun documentationLinks(): DocumentationLinks {
    return DocumentationLinks(
        redoc = ApiMetadata.docsPath,
        swagger = ApiMetadata.swaggerPath,
        openapi = ApiMetadata.openApiPath
    )
}
