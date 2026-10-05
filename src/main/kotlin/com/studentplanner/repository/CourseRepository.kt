package com.studentplanner.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studentplanner.models.Course
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

/**
 * This class is the ONLY place that talks to MongoDB about Courses.
 * Everything else (routes) talks to this repository, not to Mongo directly.
 * This separation is standard practice and makes the app easy to test.
 */
class CourseRepository(database: MongoDatabase) {

    private val collection = database.getCollection<Course>("courses")

    // CREATE
    suspend fun create(course: Course): Course {
        collection.insertOne(course)
        return course
    }

    // READ (all)
    suspend fun findAll(): List<Course> =
        collection.find().toList()

    // READ (one)
    suspend fun findById(id: String): Course? =
        collection.find(eq("id", id)).firstOrNull()

    // UPDATE
    suspend fun update(id: String, updated: Course): Boolean {
        val result = collection.replaceOne(eq("id", id), updated.copy(id = id))
        return result.modifiedCount > 0
    }

    // DELETE
    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(eq("id", id))
        return result.deletedCount > 0
    }
}
