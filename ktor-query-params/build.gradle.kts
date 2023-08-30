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
