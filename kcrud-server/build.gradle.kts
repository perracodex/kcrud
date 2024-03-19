/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

group = "kcrud.server"
version = "1.0.0"

dependencies {

    implementation(project(":kcrud-base"))
    implementation(project(":kcrud-access"))
    implementation(project(":kcrud-employee"))
    implementation(project(":kcrud-employment"))

    implementation(libs.kotlinx.datetime)

    implementation(libs.dotenv)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.rateLimit)
    implementation(libs.ktor.server.tests)

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
