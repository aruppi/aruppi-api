package com.jeluchu.core.extensions

import com.jeluchu.core.enums.TimeUnit
import com.jeluchu.core.utils.Collections
import com.jeluchu.core.utils.TimerKey
import com.jeluchu.core.utils.Timers
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import java.time.Duration
import java.time.Instant
import java.util.*

fun MongoDatabase.isUpdate(
    timer: Timers
) = getCollection(Collections.TIMERS).needsUpdate(
    key = timer.key,
    unit = timer.unit,
    amount = timer.amount
)

fun MongoDatabase.isUpdate(
    page: Int,
    timer: Timers
) = getCollection(Collections.TIMERS).needsUpdate(
    unit = timer.unit,
    amount = timer.amount,
    key = "${timer.key}_page_$page"
)

fun MongoDatabase.isUpdate(
    slug: String,
    timer: Timers
) = getCollection(Collections.TIMERS).needsUpdate(
    unit = timer.unit,
    amount = timer.amount,
    key = "${timer.key}_slug_$slug"
)

fun MongoCollection<Document>.needsUpdate(
    key: String,
    amount: Long = 5,
    unit: TimeUnit = TimeUnit.HOUR
): Boolean {
    val currentTime = Instant.now()
    val timestampEntry = find(eq(TimerKey.KEY, key)).firstOrNull()

    return if (timestampEntry == null) true else {
        val lastUpdatedDate = timestampEntry.getDate(TimerKey.LAST_UPDATED)
        val lastUpdated = lastUpdatedDate.toInstant()
        val duration = Duration.between(lastUpdated, currentTime)

        when (unit) {
            TimeUnit.DAY -> duration.toDays() >= amount
            TimeUnit.HOUR -> duration.toHours() >= amount
            TimeUnit.MINUTE -> duration.toMinutes() >= amount
            TimeUnit.SECOND -> duration.toSeconds() >= amount
        }
    }
}

fun MongoCollection<Document>.update(key: String) {
    val currentTime = Instant.now()
    val newTimestampDocument = Document(TimerKey.KEY, key)
        .append(TimerKey.LAST_UPDATED, Date.from(currentTime))

    replaceOne(
        eq(TimerKey.KEY, key),
        newTimestampDocument,
        ReplaceOptions().upsert(true)
    )
}