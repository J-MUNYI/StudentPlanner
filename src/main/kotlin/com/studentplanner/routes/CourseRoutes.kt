package com.studentplanner.routes

import com.studentplanner.models.Course
import com.studentplanner.repository.AssignmentRepository
import com.studentplanner.repository.CourseRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.courseRoutes(courseRepo: CourseRepository, assignmentRepo: AssignmentRepository) {

    route("/courses") {

        // CREATE  ->  POST /courses
        post {
            val course = call.receive<Course>()
            val created = courseRepo.create(course)
            call.respond(HttpStatusCode.Created, created)
        }

        // READ ALL  ->  GET /courses
        get {
            call.respond(courseRepo.findAll())
        }

        // READ ONE  ->  GET /courses/{id}
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val course = courseRepo.findById(id)
            if (course == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(course)
        }

        // UPDATE  ->  PUT /courses/{id}
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val updated = call.receive<Course>()
            val success = courseRepo.update(id, updated)
            if (success) call.respond(HttpStatusCode.OK, updated.copy(id = id))
            else call.respond(HttpStatusCode.NotFound)
        }

        // DELETE  ->  DELETE /courses/{id}
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val success = courseRepo.delete(id)
            if (success) {
                // also clean up any assignments that belonged to this course
                assignmentRepo.deleteByCourse(id)
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // BONUS: GET /courses/{id}/progress
        // Shows weekly hour progress + assignment completion for one course.
        get("/{id}/progress") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val course = courseRepo.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            val assignments = assignmentRepo.findByCourse(id)
            val total = assignments.size
            val submitted = assignments.count { it.submitted }
            val onTime = assignments.count { it.submittedOnTime == true }

            call.respond(
                mapOf(
                    "course" to course.courseName,
                    "hoursPerWeek" to course.hoursPerWeek,
                    "hoursCompleted" to course.hoursCompleted,
                    "weeklyHourProgressPercent" to course.weeklyProgressPercent(),
                    "totalAssignments" to total,
                    "submitted" to submitted,
                    "submittedOnTime" to onTime,
                    "onTimeRatePercent" to if (total == 0) 0.0 else (onTime.toDouble() / total) * 100
                )
            )
        }
    }
}
