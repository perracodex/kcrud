pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // https://github.com/gradle/foojay-toolchains
    // https://github.com/gradle/foojay-toolchains/tags
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Kcrud"

include("kcrud-core")
include("kcrud-access")
include("kcrud-scheduler")
include("kcrud-employee")
include("kcrud-employment")
include("kcrud-server")

includeBuild("../Kopapi") {
    dependencySubstitution {
        substitute(module("io.github.perracodex:kopapi")).using(project(":"))
    }
}
