package com.jeluchu.features.news.routes

import com.jeluchu.core.extensions.getToJson
import com.jeluchu.core.utils.Routes
import com.jeluchu.features.news.services.NewsService
import com.mongodb.client.MongoDatabase
import io.ktor.server.routing.*

fun Route.newsEndpoints(
    mongoDatabase: MongoDatabase,
    service: NewsService = NewsService(mongoDatabase)
) = route(path = Routes.NEWS) {
    route(path = Routes.ES) {
        getToJson { service.getSpanishNews(call) }
    }

    route(Routes.EN) {
        getToJson { service.getEnglishNews(call) }
    }
}