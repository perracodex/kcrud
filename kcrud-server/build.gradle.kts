/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.expedia)
}

group = "kcrud.server"
version = "1.0.0"

ktor {
    fatJar {
        archiveFileName.set("kcrud.jar")
    }
}

kotlin {
    jvmToolchain(jdkVersion = 17)
}

application {
    mainClass.set("$group.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

dependencies {

    implementation(project(":kcrud-base"))
    implementation(project(":kcrud-employee"))
    implementation(project(":kcrud-employment"))

    implementation(libs.kotlinx.datetime)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.rateLimit)
    implementation(libs.ktor.server.tests)

    implementation(libs.graphql.expedia.server)
    implementation(libs.graphql.kgraphql.ktor)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.test)

    implementation(libs.shared.commons.codec)
    implementation(libs.shared.gson)

    testImplementation(libs.test.kotlin.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.mockito.kotlin)
    testImplementation(libs.exposed.core)
    testImplementation(libs.exposed.kotlin.datetime)
}
