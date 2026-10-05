package com.studentplanner

import com.studentplanner.database.MongoDatabaseProvider
import com.studentplanner.repository.AssignmentRepository
import com.studentplanner.repository.CourseRepository
import com.studentplanner.routes.assignmentRoutes
import com.studentplanner.routes.courseRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.calllogging.CallLogging
import kotlinx.serialization.json.Json

// Entry point: this is what `./gradlew run` executes.
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    // Turns request/response bodies into JSON <-> Kotlin data classes automatically.
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CallLogging)

    // Catch-all error handling so a bad request doesn't crash the server.
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Unknown error")))
        }
    }

    // Wire up the database + repositories once, reuse everywhere.
    val database = MongoDatabaseProvider.database
    val courseRepository = CourseRepository(database)
    val assignmentRepository = AssignmentRepository(database)

    routing {
        // Serves everything in src/main/resources/static (index.html, style.css, app.js)
        // at the root URL. Visiting http://localhost:8080 now loads the actual UI,
        // not JSON. This is what makes the frontend and backend "one app" —
        // there's no separate frontend server or port to manage.
        staticResources("/", "static")

        get("/api/status") {
            call.respond(
                mapOf(
                    "status" to "Student Planner API is running",
                    "endpoints" to listOf(
                        "GET/POST /courses",
                        "GET/PUT/DELETE /courses/{id}",
                        "GET /courses/{id}/progress",
                        "GET/POST /assignments",
                        "GET/PUT/DELETE /assignments/{id}",
                        "GET /assignments/course/{courseId}"
                    )
                )
            )
        }
        courseRoutes(courseRepository, assignmentRepository)
        assignmentRoutes(assignmentRepository)
    }
}
