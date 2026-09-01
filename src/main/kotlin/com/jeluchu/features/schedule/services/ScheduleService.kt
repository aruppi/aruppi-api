package com.jeluchu.features.schedule.services

import com.jeluchu.core.connection.RestClient
import com.jeluchu.core.enums.Day
import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.enums.parseDay
import com.jeluchu.core.extensions.needsUpdate
import com.jeluchu.core.extensions.respondError
import com.jeluchu.core.extensions.update
import com.jeluchu.core.messages.ErrorMessages
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
import org.bson.Document

class ScheduleService(
    database: MongoDatabase
) {
    private val timers = database.getCollection(Collections.TIMERS)
    private val schedules = database.getCollection(Collections.SCHEDULES)

    suspend fun getSchedule(call: RoutingCall) {
        val elements = loadSchedules()
        call.respond(HttpStatusCode.OK, elements.documentWeekMapper())
    }

    suspend fun getScheduleByDay(call: RoutingCall) {
        val param = call.parameters["day"] ?: throw IllegalArgumentException(ErrorMessages.InvalidMalId.message)
        val day = parseDay(param)
            ?: return call.respondError(HttpStatusCode.BadRequest, ErrorMessages.InvalidDay.message)

        loadSchedules()

        val elements = schedules.find(Filters.eq("day", day.name.lowercase())).toList()
        val directory = elements.map { documentToScheduleDayEntity(it) }
        call.respond(HttpStatusCode.OK, directory)
    }

    private suspend fun loadSchedules(): List<Document> {
        val cached = schedules.find().toList()
        val needsUpdate = timers.needsUpdate(
            amount = 7,
            unit = TimeUnit.DAY,
            key = TimerKey.SCHEDULE
        )

        // A valid timer must not hide an empty cache, for example after an interrupted refresh.
        if (!needsUpdate && cached.isNotEmpty()) return cached

        return try {
            val schedule = mutableListOf<DayEntity>()

            Day.entries.forEach { day ->
                schedule += fetchSchedule(day).data?.map { it.toDayEntity(day) }.orEmpty()
            }

            val documents = parseDataToDocuments(schedule, DayEntity.serializer())
            if (documents.isEmpty()) {
                cached
            } else {
                // Replace the cache only after all upstream days have been loaded successfully.
                schedules.deleteMany(Document())
                schedules.insertMany(documents)
                timers.update(TimerKey.SCHEDULE)
                documents
            }
        } catch (exception: Exception) {
            // Keep serving the last known schedule when Tenrai is temporarily unavailable.
            if (cached.isNotEmpty()) cached else throw exception
        }
    }

    private suspend fun fetchSchedule(day: Day) = RestClient.requestWithDelay(
        url = BaseUrls.TENRAI + Endpoints.SCHEDULES + "?filter=" + day.name.lowercase(),
        deserializer = ScheduleEntity.serializer()
    )

    private fun List<Document>.documentWeekMapper(): ScheduleData {
        return map { documentToScheduleDayEntity(it) }.toScheduleData()
    }

    private fun List<DayEntity>.toScheduleData() = ScheduleData(
        monday = forDay(Day.MONDAY),
        tuesday = forDay(Day.TUESDAY),
        wednesday = forDay(Day.WEDNESDAY),
        thursday = forDay(Day.THURSDAY),
        friday = forDay(Day.FRIDAY),
        saturday = forDay(Day.SATURDAY),
        sunday = forDay(Day.SUNDAY)
    )

    private fun List<DayEntity>.forDay(day: Day): List<DayEntity> {
        return filter { it.day.equals(day.name, ignoreCase = true) }
            .distinctBy { it.malId }
    }
}
