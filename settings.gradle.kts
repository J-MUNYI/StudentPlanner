plugins {
    // Lets Gradle auto-download JDK 17 if your machine doesn't already have it,
    // instead of failing with "Cannot find a Java installation".
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "student-planner"
