package com.jeluchu.features.news.services

import com.jeluchu.core.extensions.isUpdate
import com.jeluchu.core.extensions.toJson
import com.jeluchu.core.extensions.update
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.NewsSources
import com.jeluchu.core.utils.SeasonCalendar
import com.jeluchu.core.utils.Timers
import com.jeluchu.core.utils.parseDataToDocuments
import com.jeluchu.features.news.mappers.documentToNewsEntity
import com.jeluchu.features.news.mappers.toNews
import com.jeluchu.features.news.models.NewEntity
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.prof18.rssparser.RssParser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.Document
import kotlin.text.toInt

class NewsService(
    val database: MongoDatabase
) {
    suspend fun getSpanishNews(
        call: RoutingCall
    ) = getNews(
        call = call,
        timer = Timers.NEWS_ES,
        sources = listOf(
            NewsSources.MANGALATAM,
            NewsSources.SOMOSKUDASAI,
            NewsSources.CRUNCHYROLL,
            NewsSources.RAMENPARADOS,
            NewsSources.ANMOSUGOI
        ),
        db = database.getCollection(Collections.NEWS_ES)
    )

    suspend fun getEnglishNews(
        call: RoutingCall
    ) = getNews(
        call = call,
        timer = Timers.NEWS_EN,
        sources = listOf(
            NewsSources.OTAKUMODE,
            NewsSources.MYANIMELIST,
            NewsSources.HONEYSANIME,
            NewsSources.ANIMEHUNCH
        ),
        db = database.getCollection(Collections.NEWS_EN)
    )

    private suspend fun getNews(
        timer: Timers,
        call: RoutingCall,
        sources: List<NewsSources>,
        db: MongoCollection<Document>
    ) {
        val timersDb = database.getCollection(Collections.TIMERS)
        if (database.isUpdate(timer = timer)) {
            db.deleteMany(Document())

            parseDataToDocuments(
                serializer = NewEntity.serializer(),
                data = mutableListOf<NewEntity>().apply {
                    with(receiver = RssParser()) {
                        sources.forEach {
                            getRssChannel(url = it.link)
                                .toNews(
                                    source = it.source,
                                    list = this@apply
                                )
                        }
                    }
                }.shuffled(),
            ).apply {
                if (isNotEmpty()) db.insertMany(this)
                timersDb.update(key = timer.key)
                call.respond(
                    status = HttpStatusCode.OK,
                    message = map { documentToNewsEntity(it) }.toJson()
                )
            }
        } else call.respond(
            status = HttpStatusCode.OK,
            message = db
                .find()
                .toList()
                .map { documentToNewsEntity(it) }
                .toJson()
        )
    }
}