package com.jeluchu.features.search.routes

import com.jeluchu.core.extensions.getToJson
import com.jeluchu.core.utils.Routes
import com.jeluchu.features.search.services.SearchService
import io.ktor.server.routing.*

fun Route.searchEndpoints(
    service: SearchService = SearchService()
) = route(Routes.SEARCH) {
    getToJson { service.search(call) }
}
