package com.studentplanner.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single weekly assignment/submission tied to a Course.
 *
 * weekNumber      -> which week of the unit this submission belongs to
 * dueDate         -> ISO date string, e.g. "2026-09-26"
 * submitted       -> has the student turned it in
 * submittedOnTime -> null until graded/checked, then true/false
 * hoursSpent      -> hours the student logged working on this specific assignment
 */
@Serializable
data class Assignment(
    val id: String = UUID.randomUUID().toString(),
    val courseId: String,
    val title: String,
    val weekNumber: Int,
    val dueDate: String,
    val submitted: Boolean = false,
    val submittedOnTime: Boolean? = null,
    val hoursSpent: Double = 0.0
)
