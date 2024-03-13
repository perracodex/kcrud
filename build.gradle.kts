/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.expedia)
}

group = "kcrud"
version = "1.0.0"

// Ktor plugin configuration block.
ktor {
    fatJar {
        // Set the name of the generated fat JAR file.
        archiveFileName.set("$group-$version-all.jar")
    }
}

application {
    // Specify the fully qualified name of the main class for the application.
    // This setting is used to define the entry point for the executable JAR generated
    // by Gradle, which is essential for running the application with 'java -jar' command.
    mainClass.set("$group.server.ApplicationKt")

    // Determine if the 'development' flag is present in project properties.
    // This flag is used to set the application's operating mode.
    // If 'development' is true, additional debug information and settings
    // suitable for development may be enabled.
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

// Configuration block for all projects in this multi-project build.
allprojects {

    // Define repositories where dependencies are fetched from.
    repositories {
        // Use Maven Central as the primary repository for fetching dependencies.
        mavenCentral()

        // Add the Kotlin JS Wrappers repository from JetBrains Space,
        // required for projects that depend on Kotlin/JS libraries or components.
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    }
}

// Configuration block applied to all subprojects within the multi-project setup.
subprojects {

    // Apply common plugins necessary for Kotlin development to all subprojects.
    // This includes the Kotlin JVM and Kotlin Serialization plugins, which are
    // retrieved dynamically from the version catalog defined in the root project.
    apply {
        plugin(rootProject.libs.plugins.kotlin.jvm.get().pluginId)
        plugin(rootProject.libs.plugins.kotlin.serialization.get().pluginId)
    }

    // Configure the Kotlin JVM toolchain for all subprojects to use JDK version 17.
    // This ensures that all Kotlin compilations in subprojects use the specified JDK version.
    kotlin {
        jvmToolchain(jdkVersion = 17)
    }
}

dependencies {
    implementation(project(":kcrud-base"))
    implementation(project(":kcrud-employee"))
    implementation(project(":kcrud-employment"))
    implementation(project(":kcrud-server"))
}

// Part of the fat JAR workflow: Task to copy the SSL keystore file for secure deployment.
val copyKeystoreTask by tasks.registering(Copy::class) {
    from("keystore.p12")
    into("build/libs")
    doFirst {
        println("Copying keystore from ${project.projectDir}/keystore.p12 to ${project.buildDir}/libs.")
    }
}

// Part of the fat JAR workflow: Ensures SSL keystore file is placed in the build output after the fat JAR creation.
tasks.named("buildFatJar") {
    finalizedBy(copyKeystoreTask)
    doLast {
        println("Finished building fat JAR.")
    }
}

// Part of the fat JAR workflow: Ensures the keystore copying task is completed before running the fat JAR.
tasks.named("startShadowScripts") {
    dependsOn(copyKeystoreTask)
}
