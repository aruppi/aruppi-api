package com.jeluchu.core.configuration

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Route.initDocumentation() {
    val openApiFile = this::class.java.classLoader
        .getResource("openapi/documentation.yaml")
        ?.readText()
        .orEmpty()

    get("/") {
        call.respondRedirect("docs", permanent = false)
    }

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
                <redoc spec-url="/api/v5/openapi.yaml"></redoc>
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