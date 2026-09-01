package com.jeluchu.features.schedule.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleData(
    /**
     * All current season entries scheduled for Monday.
     */
    @SerialName("monday")
    val monday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Tuesday.
     */
    @SerialName("tuesday")
    val tuesday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Wednesday.
     */
    @SerialName("wednesday")
    val wednesday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Thursday.
     */
    @SerialName("thursday")
    val thursday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Friday.
     */
    @SerialName("friday")
    val friday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Saturday.
     */
    @SerialName("saturday")
    val saturday: List<DayEntity> = emptyList(),

    /**
     * All current season entries scheduled for Sunday.
     */
    @SerialName("sunday")
    val sunday: List<DayEntity> = emptyList()
)
