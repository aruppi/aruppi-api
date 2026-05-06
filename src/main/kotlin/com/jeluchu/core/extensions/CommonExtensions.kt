package com.jeluchu.core.extensions

import com.jeluchu.core.configuration.ApiMetadata
import com.jeluchu.core.models.DocumentationLinks
import com.jeluchu.core.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.Document
import java.text.SimpleDateFormat
import java.util.*

fun RoutingCall.getIntSafeQueryParam(
    key: String,
    defaultValue: Int = 0
) = request.queryParameters[key]?.toIntOrNull() ?: defaultValue

fun RoutingCall.getStringSafeQueryParam(
    key: String,
    defaultValue: String = ""
) = request.queryParameters[key] ?: defaultValue

fun RoutingCall.getStringSafeParam(
    key: String,
    defaultValue: String = ""
) = parameters[key] ?: defaultValue

fun RoutingCall.getIntSafeParam(
    key: String,
    defaultValue: Int = 0
) = parameters[key]?.toIntOrNull() ?: defaultValue

suspend fun RoutingCall.respondError(
    status: HttpStatusCode,
    message: String,
    code: String = status.defaultErrorCode()
) = respond(
    status,
    errorResponse(status = status, message = message, code = code)
)

suspend fun RoutingCall.badRequestError(message: String) = respondError(HttpStatusCode.BadRequest, message)

fun ApplicationCall.errorResponse(
    status: HttpStatusCode,
    message: String,
    code: String = status.defaultErrorCode()
) = ErrorResponse(
    error = message,
    code = code,
    status = status.value,
    path = request.path(),
    requestId = request.headers["X-Request-ID"] ?: UUID.randomUUID().toString(),
    version = ApiMetadata.version,
    documentation = DocumentationLinks(
        redoc = ApiMetadata.docsPath,
        swagger = ApiMetadata.swaggerPath,
        openapi = ApiMetadata.openApiPath
    )
)

fun HttpStatusCode.defaultErrorCode(): String = when (value) {
    HttpStatusCode.BadRequest.value -> "BAD_REQUEST"
    HttpStatusCode.Unauthorized.value -> "UNAUTHORIZED"
    HttpStatusCode.Forbidden.value -> "FORBIDDEN"
    HttpStatusCode.NotFound.value -> "NOT_FOUND"
    HttpStatusCode.TooManyRequests.value -> "RATE_LIMITED"
    in 500..599 -> "INTERNAL_SERVER_ERROR"
    else -> "REQUEST_FAILED"
}

fun Document.getStringSafe(key: String, defaultValue: String = ""): String {
    return try {
        when (val value = this[key]) {
            is String -> value
            is Number, is Boolean -> value.toString()
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getIntSafe(key: String, defaultValue: Int = 0): Int {
    return try {
        when (val value = this[key]) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getDoubleSafe(key: String, defaultValue: Double = 0.0): Double {
    return try {
        when (val value = this[key]) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: defaultValue
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getFloatSafe(key: String, defaultValue: Float = 0.0f): Float {
    return try {
        when (val value = this[key]) {
            is Float -> value
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: defaultValue
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getLongSafe(key: String, defaultValue: Long = 0L): Long {
    return try {
        val value = this.get(key)
        when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultValue
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getBooleanSafe(key: String, defaultValue: Boolean = false): Boolean {
    return try {
        when (val value = this[key]) {
            is Boolean -> value
            is String -> value.toBoolean()
            is Number -> value.toInt() != 0
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getDateSafe(key: String, defaultValue: Date = Date(0)): Date {
    return try {
        when (val value = this[key]) {
            is Date -> value
            is String -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(value) ?: defaultValue
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

inline fun <reified T> Document.getListSafe(key: String, defaultValue: List<T> = emptyList()): List<T> {
    return try {
        when (val value = this[key]) {
            is List<*> -> value.filterIsInstance<T>()
            else -> defaultValue
        }
    } catch (e: Exception) {
        defaultValue
    }
}

fun Document.getDocumentSafe(key: String): Document? {
    return try {
        val value = this[key]
        if (value is Document) value else null
    } catch (e: Exception) {
        null
    }
}

fun String.parseRssDate(format: String = "dd/MM/yyyy"): String {
    val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).parse(this)
    val outputDateFormat = SimpleDateFormat(format, Locale.US)
    return date?.let { outputDateFormat.format(it) }.orEmpty()
}
