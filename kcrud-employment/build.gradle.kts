/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.graphql.expedia)
}

group = "kcrud.employment"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

application {
    mainClass.set("")
}

dependencies {

    implementation(project(":kcrud-base"))
    implementation(project(":kcrud-employee"))

    implementation(libs.kotlinx.datetime)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.tests)

    implementation(libs.exposed.core)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.graphql.expedia.server)
    implementation(libs.graphql.kgraphql.ktor)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.test)

    implementation(libs.shared.commons.codec)

    implementation(libs.test.kotlin.junit)
    implementation(libs.test.mockk)
    implementation(libs.test.mockito.kotlin)
}
