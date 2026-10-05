package com.studentplanner.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents one "core course work" unit the student is tracking.
 *
 * hoursPerWeek   -> how many hours of school/lecture/self-study this course takes per week
 * creditUnits    -> how many units the course is worth (used for workload weighting)
 * hoursCompleted -> running total of hours the student has actually logged studying this course
 */
@Serializable
data class Course(
    val id: String = UUID.randomUUID().toString(),
    val courseName: String,
    val courseCode: String,
    val hoursPerWeek: Int,
    val creditUnits: Int,
    val hoursCompleted: Double = 0.0
) {
    /**
     * Simple progress indicator: how much of the expected weekly hours
     * has the student actually logged so far, expressed as a percentage.
     * (Demonstrates a Kotlin computed/derived value using a function.)
     */
    fun weeklyProgressPercent(): Double {
        if (hoursPerWeek == 0) return 0.0
        val pct = (hoursCompleted / hoursPerWeek) * 100
        return if (pct > 100) 100.0 else pct
    }
}
