/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

group = "kcrud.base"
version = "1.0.0"

dependencies {

    implementation(libs.database.h2)

    implementation(libs.docs.swagger)
    implementation(libs.docs.swagger.generators)
    implementation(libs.docs.openapi)

    implementation(libs.kotlinx.atomicfu)
    implementation(libs.kotlinx.datetime)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.autoHeadResponse)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.rateLimit)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.tests)

    implementation(libs.exposed.core)
    implementation(libs.exposed.crypt)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.flyway.core)

    implementation(libs.google.phonenumber)

    implementation(libs.graphql.expedia.generator)
    implementation(libs.graphql.expedia.server)
    implementation(libs.graphql.kgraphql)
    implementation(libs.graphql.kgraphql.ktor)

    implementation(libs.hikariCP)

    implementation(libs.logback.classic)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.koin.test)

    implementation(libs.micrometer.metrics)
    implementation(libs.micrometer.metrics.prometheus)

    implementation(libs.quartz.scheduler)

    implementation(libs.shared.commons.codec)
    implementation(libs.shared.gson)

    implementation(libs.typesafe.config)

    testImplementation(libs.test.kotlin.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.mockito.kotlin)
}
