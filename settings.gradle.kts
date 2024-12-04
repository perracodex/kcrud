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

include("kcrud-system:core")
include("kcrud-system:database")
include("kcrud-system:access")
include("kcrud-system:scheduler")
include("kcrud-domain:employee")
include("kcrud-domain:employment")
include("kcrud-server")

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
