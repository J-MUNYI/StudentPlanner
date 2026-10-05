package com.studentplanner.routes

import com.studentplanner.models.Assignment
import com.studentplanner.repository.AssignmentRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.assignmentRoutes(assignmentRepo: AssignmentRepository) {

    route("/assignments") {

        // CREATE  ->  POST /assignments
        post {
            val assignment = call.receive<Assignment>()
            val created = assignmentRepo.create(assignment)
            call.respond(HttpStatusCode.Created, created)
        }

        // READ ALL  ->  GET /assignments
        get {
            call.respond(assignmentRepo.findAll())
        }

        // READ ONE  ->  GET /assignments/{id}
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val assignment = assignmentRepo.findById(id)
            if (assignment == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(assignment)
        }

        // READ ALL FOR ONE COURSE -> GET /assignments/course/{courseId}
        get("/course/{courseId}") {
            val courseId = call.parameters["courseId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(assignmentRepo.findByCourse(courseId))
        }

        // UPDATE  ->  PUT /assignments/{id}
        // (typically used to mark an assignment as submitted, on time or not,
        //  and log hours spent on it)
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val updated = call.receive<Assignment>()
            val success = assignmentRepo.update(id, updated)
            if (success) call.respond(HttpStatusCode.OK, updated.copy(id = id))
            else call.respond(HttpStatusCode.NotFound)
        }

        // DELETE  ->  DELETE /assignments/{id}
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val success = assignmentRepo.delete(id)
            if (success) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}
