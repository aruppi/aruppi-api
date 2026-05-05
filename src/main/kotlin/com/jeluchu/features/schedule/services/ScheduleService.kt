package com.jeluchu.features.schedule.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.Day
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseDay
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
import com.jeluchu.core.models.ErrorResponse
import com.jeluchu.core.models.jikan.anime.AnimeData.Companion.toDayEntity
import com.jeluchu.core.utils.*
import com.jeluchu.features.anime.mappers.documentToScheduleDayEntity
import com.jeluchu.features.schedule.models.DayEntity
import com.jeluchu.features.schedule.models.ScheduleData
import com.jeluchu.features.schedule.models.ScheduleEntity
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document

class ScheduleService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val schedules = database.getCollection(Collections.SCHEDULES)

    suspend fun getSchedule(call: RoutingCall) {
        val needsUpdate = timers.needsUpdate(
            amount = 7,
            unit = TimeUnit.DAY,
            key = TimerKey.SCHEDULE
        )

        if (needsUpdate) {
            schedules.deleteMany(Document())
            val documents = mutableListOf<Document>()

            Day.entries.forEach { day ->
                val animes = getSchedule(day).data?.map { it.toDayEntity(day) }.orEmpty()
                val documentsToInsert = parseDataToDocuments(animes, DayEntity.serializer())
                if (documentsToInsert.isNotEmpty()) {
                    documents.addAll(documentsToInsert)
                    schedules.insertMany(documentsToInsert)
                }
            }

            timers.update(TimerKey.SCHEDULE)

            call.respond(HttpStatusCode.OK, documents.documentWeekMapper())
        } else {
            val elements = schedules.find().toList()
            call.respond(HttpStatusCode.OK, elements.documentWeekMapper())
        }
    }

    suspend fun getScheduleByDay(call: RoutingCall) {
        val param = call.parameters["day"] ?: throw IllegalArgumentException(ErrorMessages.InvalidMalId.message)
        if (parseDay(param) == null) return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidDay.message)

        val elements = schedules.find(Filters.eq("day", param.lowercase())).toList()
        val directory = elements.map { documentToScheduleDayEntity(it) }
        val json = Json.encodeToString(directory)
        call.respond(HttpStatusCode.OK, json)
    }

    private suspend fun getSchedule(day: Day) = RestClient.requestWithDelay(
        url = BaseUrls.JIKAN + Endpoints.SCHEDULES + "/" + day,
        deserializer = ScheduleEntity.serializer()
    )

    private fun List<Document>.documentWeekMapper(): String {
        val elements = map { documentToScheduleDayEntity(it) }

        return Json.encodeToString(ScheduleData(
            monday = elements.filter { it.day == Day.MONDAY.name.lowercase() }.distinctBy { it.malId },
            tuesday = elements.filter { it.day == Day.TUESDAY.name.lowercase() }.distinctBy { it.malId },
            wednesday = elements.filter { it.day == Day.WEDNESDAY.name.lowercase() }.distinctBy { it.malId },
            thursday = elements.filter { it.day == Day.THURSDAY.name.lowercase() }.distinctBy { it.malId },
            friday = elements.filter { it.day == Day.FRIDAY.name.lowercase() }.distinctBy { it.malId },
            saturday = elements.filter { it.day == Day.SATURDAY.name.lowercase() }.distinctBy { it.malId },
            sunday = elements.filter { it.day == Day.SUNDAY.name.lowercase() }.distinctBy { it.malId }
        ))
    }
}
