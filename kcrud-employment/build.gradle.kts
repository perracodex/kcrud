/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

group = "kcrud.employment"
version = "1.0.0"

dependencies {

    implementation(project(":kcrud-core"))
    implementation(project(":kcrud-access"))
    implementation(project(":kcrud-employee"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)

    implementation(libs.ktor.config)

    implementation(libs.exposed.core)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.pagination)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.test)

    implementation(libs.shared.commons.codec)

    implementation(libs.test.kotlin.junit)
    implementation(libs.test.mockk)
    implementation(libs.test.mockito.kotlin)
}
