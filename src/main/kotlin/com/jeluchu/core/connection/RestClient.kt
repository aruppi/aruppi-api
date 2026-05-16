package com.jeluchu.core.connection

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

object RestClient {
    private val client = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> request(
        url: String,
        deserializer: DeserializationStrategy<T>
    ): T {
        return runCatching {
            val response = client.get(url) {
                headers { append(HttpHeaders.Accept, ContentType.Application.Json.toString()) }
            }

            json.decodeFromString(deserializer, response.bodyAsText())
        }.getOrElse { throwable -> throw throwable }
    }

    // Fetch raw response body as string (useful for debugging)
    suspend fun fetchRaw(url: String): String {
        val response = client.get(url) {
            headers { append(HttpHeaders.Accept, ContentType.Application.Json.toString()) }
        }

        return response.bodyAsText()
    }

    suspend fun <T> requestWithDelay(
        url: String,
        delay: Long = 1000,
        deserializer: DeserializationStrategy<T>
    ): T {
        return runCatching {
            val response = client.get(url) {
                headers { append(HttpHeaders.Accept, ContentType.Application.Json.toString()) }
            }

            delay(duration = delay.milliseconds)
            json.decodeFromString(deserializer, response.bodyAsText())
        }.getOrElse { throwable -> throw throwable }
    }
}