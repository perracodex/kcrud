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

rootProject.name = "Krud"

include("krud-system:core")
include("krud-system:database")
include("krud-system:access")
include("krud-domain:employee")
include("krud-domain:employment")
include("krud-server")

// includeBuild("../Kopapi") {
//    dependencySubstitution {
//        substitute(module("io.github.perracodex:kopapi")).using(project(":"))
//    }
// }
//
// includeBuild("../ExposedPagination") {
//    dependencySubstitution {
//        substitute(module("io.github.perracodex:exposed-pagination")).using(project(":"))
//    }
// }
//
// includeBuild("../KtorConfig") {
//    dependencySubstitution {
//        substitute(module("io.github.perracodex:ktor-config")).using(project(":"))
//    }
// }
