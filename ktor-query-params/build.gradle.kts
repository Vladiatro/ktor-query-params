plugins {
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
}

dependencies {
    implementation(libs.ktorServerCore)

    testImplementation(libs.ktorServerCoreJvm)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorServerContentNegotiation)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.ktorSerializationJson)
    testImplementation(libs.jackson)
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                name = "Ktor Query Params Library"
                description = "Introduces a convenient way to define and manage query parameters and responses in Ktor applications"
                url = "https://github.com/Vladiatro/ktor-query-params/ktor-query-params"
            }
        }
    }
}
