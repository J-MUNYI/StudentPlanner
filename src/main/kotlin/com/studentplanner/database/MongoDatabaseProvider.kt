package com.studentplanner.database

import com.mongodb.MongoClientSettings
import com.mongodb.ConnectionString
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.kotlinx.KotlinSerializerCodecProvider

/**
 * Centralizes the MongoDB connection so the rest of the app just asks
 * for `MongoDatabaseProvider.database` instead of reconnecting everywhere.
 *
 * Kotlin concept: `object` = a singleton. There is only ever ONE
 * MongoDatabaseProvider instance in the whole app, created lazily
 * the first time it's used.
 */
object MongoDatabaseProvider {

    // Codec registry teaches the driver how to turn our @Serializable
    // data classes (Course, Assignment) into MongoDB documents and back.
    private val codecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(KotlinSerializerCodecProvider())
    )

    private val client: MongoClient by lazy {
        MongoClient.create(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(connectionString))
                .codecRegistry(codecRegistry)
                .build()
        )
    }

    // Read from environment variable first (useful for Docker/Atlas),
    // fall back to local MongoDB for simple local development.
    private val connectionString: String =
        System.getenv("MONGO_URI") ?: "mongodb://localhost:27017"

    private val databaseName: String =
        System.getenv("MONGO_DB_NAME") ?: "student_planner"

    val database: MongoDatabase by lazy {
        client.getDatabase(databaseName)
    }
}
