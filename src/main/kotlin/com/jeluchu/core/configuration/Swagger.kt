package com.jeluchu.core.configuration

import com.jeluchu.core.models.ApiInfoResponse
import com.jeluchu.core.models.DocumentationLinks
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Route.initDocumentation() {
    val openApiFile = this::class.java.classLoader
        .getResource("openapi/documentation.yaml")
        ?.readText()
        .orEmpty()

    suspend fun RoutingContext.respondApiInfo() {
        call.respond(
            ApiInfoResponse(
                version = ApiMetadata.version,
                documentation = DocumentationLinks(
                    redoc = ApiMetadata.docsPath,
                    swagger = ApiMetadata.swaggerPath,
                    openapi = ApiMetadata.openApiPath
                )
            )
        )
    }

    get { respondApiInfo() }
    get("/") { respondApiInfo() }

    get("/openapi.yaml") {
        call.respondText(openApiFile, ContentType.parse("application/yaml"))
    }

    get("/docs") {
        call.respondText(
            """
            <!doctype html>
            <html>
              <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <title>Aruppi API Docs</title>
                <style>
                  body { margin: 0; padding: 0; }
                </style>
              </head>
              <body>
                <redoc spec-url="${ApiMetadata.openApiPath}"></redoc>
                <script src="https://cdn.redoc.ly/redoc/latest/bundles/redoc.standalone.js"></script>
              </body>
            </html>
            """.trimIndent(),
            ContentType.Text.Html
        )
    }

    swaggerUI(
        path = "/swagger",
        swaggerFile = "openapi/documentation.yaml"
    )
}
