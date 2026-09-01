package com.jeluchu.core.connection

import com.jeluchu.core.utils.BaseUrls
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

object RestClient {
    private const val MAX_ATTEMPTS = 3
    private const val TENRAI_MIN_INTERVAL_MILLIS = 300L
    private const val RETRY_BASE_DELAY_MILLIS = 1_000L
    private const val MAX_RETRY_DELAY_MILLIS = 10_000L

    private val client = HttpClient(CIO) {
        expectSuccess = false
    }
    private val json = Json { ignoreUnknownKeys = true }
    private val tenraiRateLimitMutex = Mutex()
    private var nextTenraiRequestAt = 0L

    suspend fun <T> request(
        url: String,
        deserializer: DeserializationStrategy<T>,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val body = requestBody(url, builder)
        return json.decodeFromString(deserializer, body)
    }

    // Fetch raw response body as string (useful for debugging)
    suspend fun fetchRaw(url: String): String = requestBody(url)

    suspend fun <T> requestWithDelay(
        url: String,
        delay: Long = 1000,
        deserializer: DeserializationStrategy<T>
    ): T {
        val response = request(url, deserializer)
        kotlinx.coroutines.delay(duration = delay.milliseconds)
        return response
    }

    private suspend fun requestBody(
        url: String,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): String {
        var lastFailure: Throwable? = null

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                awaitTenraiRateLimit(url)

                val response = client.get(url) {
                    headers { append(HttpHeaders.Accept, ContentType.Application.Json.toString()) }
                    builder()
                }
                val body = response.bodyAsText()
                val statusCode = response.status.value

                if (statusCode in 200..299) return body

                val retryable = statusCode == HttpStatusCode.TooManyRequests.value || statusCode >= 500
                if (!retryable || attempt == MAX_ATTEMPTS - 1) {
                    throw UpstreamHttpException(statusCode, response.status.description, body)
                }

                delay(retryDelayMillis(response.headers[HttpHeaders.RetryAfter], attempt))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: UpstreamHttpException) {
                lastFailure = exception
                if (attempt == MAX_ATTEMPTS - 1 || exception.statusCode < 500 && exception.statusCode != HttpStatusCode.TooManyRequests.value) {
                    throw exception
                }
                delay(retryDelayMillis(null, attempt))
            } catch (exception: Exception) {
                lastFailure = exception
                if (attempt == MAX_ATTEMPTS - 1) throw exception
                delay(retryDelayMillis(null, attempt))
            }
        }

        throw lastFailure ?: IllegalStateException("Request failed without a response: $url")
    }

    private suspend fun awaitTenraiRateLimit(url: String) {
        if (!url.startsWith(BaseUrls.TENRAI)) return

        val waitMillis = tenraiRateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val scheduledAt = maxOf(now, nextTenraiRequestAt)
            nextTenraiRequestAt = scheduledAt + TENRAI_MIN_INTERVAL_MILLIS
            scheduledAt - now
        }

        if (waitMillis > 0) delay(waitMillis)
    }

    private fun retryDelayMillis(retryAfter: String?, attempt: Int): Long {
        val retryAfterMillis = retryAfter
            ?.toLongOrNull()
            ?.times(1_000L)

        return retryAfterMillis?.coerceIn(250L, MAX_RETRY_DELAY_MILLIS)
            ?: min(RETRY_BASE_DELAY_MILLIS * (attempt + 1), MAX_RETRY_DELAY_MILLIS)
    }
}

class UpstreamHttpException(
    val statusCode: Int,
    statusDescription: String,
    responseBody: String
) : IllegalStateException(
    "Upstream request failed with HTTP $statusCode ($statusDescription): ${responseBody.take(500)}"
)
