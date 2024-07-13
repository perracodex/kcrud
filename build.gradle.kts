/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization) apply false
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
    mainClass.set("io.ktor.server.netty.EngineMain")

    // Configure detailed coroutine debug logging.
    val enhanceCoroutinesDebugging: Boolean = project.findProperty("enhanceCoroutinesDebugging")?.toString()?.toBoolean() ?: false
    if (enhanceCoroutinesDebugging) {
        applicationDefaultJvmArgs = listOf("-Dkotlinx.coroutines.debug=on")
    }
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

    // Defined in 'gradle.properties' file.
    val disableOptimizations: Boolean = project.findProperty("disableOptimizations")?.toString()?.toBoolean() ?: false

    // Targets 'KotlinCompile' tasks in each subproject to apply task-specific compiler options.
    tasks.withType<KotlinCompile>().configureEach {
        if (disableOptimizations) {
            compilerOptions {
                // Add '-Xdebug' flag to disable local variable optimizations when debugging.
                // WARNING: Never add this flag in production as it can cause memory leaks.
                freeCompilerArgs.add("-Xdebug")
            }
        }
    }
}

dependencies {
    implementation(project(":kcrud-base"))
    implementation(project(":kcrud-employee"))
    implementation(project(":kcrud-employment"))
    implementation(project(":kcrud-server"))
}

/** Part of the fat JAR workflow: Task to copy the SSL keystore file for secure deployment. */
val copyKeystoreTask: TaskProvider<Copy> by tasks.registering(Copy::class) {
    from("keystore.p12")
    into("build/libs")
    doFirst {
        println("Copying keystore from ${project.projectDir}/keystore.p12 to ${project.buildDir}/libs.")
    }
}

/** Part of the fat JAR workflow: Ensures SSL keystore file is placed in the build output after the fat JAR creation. */
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
