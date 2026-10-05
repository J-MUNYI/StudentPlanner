package com.studentplanner.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.studentplanner.models.Assignment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class AssignmentRepository(database: MongoDatabase) {

    private val collection = database.getCollection<Assignment>("assignments")

    suspend fun create(assignment: Assignment): Assignment {
        collection.insertOne(assignment)
        return assignment
    }

    suspend fun findAll(): List<Assignment> =
        collection.find().toList()

    suspend fun findByCourse(courseId: String): List<Assignment> =
        collection.find(eq("courseId", courseId)).toList()

    suspend fun findById(id: String): Assignment? =
        collection.find(eq("id", id)).firstOrNull()

    suspend fun update(id: String, updated: Assignment): Boolean {
        val result = collection.replaceOne(eq("id", id), updated.copy(id = id))
        return result.modifiedCount > 0
    }

    suspend fun delete(id: String): Boolean {
        val result = collection.deleteOne(eq("id", id))
        return result.deletedCount > 0
    }

    suspend fun deleteByCourse(courseId: String) {
        collection.deleteMany(eq("courseId", courseId))
    }
}
