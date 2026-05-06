package com.jeluchu.core.configuration

import com.jeluchu.core.extensions.errorResponse
import com.jeluchu.core.messages.ErrorMessages
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
                message = call.errorResponse(
                    status = HttpStatusCode.BadRequest,
                    message = ErrorMessages.InvalidRequest.message,
                    code = "INVALID_REQUEST"
                )
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = call.errorResponse(
                    status = HttpStatusCode.BadRequest,
                    message = cause.message ?: ErrorMessages.InvalidRequest.message,
                    code = "INVALID_ARGUMENT"
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error while processing ${call.request.path()}", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = call.errorResponse(
                    status = HttpStatusCode.InternalServerError,
                    message = ErrorMessages.InternalServerError.message,
                    code = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }

    install(plugin = ContentNegotiation) {
        json()
    }
}
