pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "Kcrud"

include("kcrud-base")
include("kcrud-employee")
include("kcrud-employment")
include("kcrud-server")
