dependencies {
    implementation(project(":ktor-query-params"))

    implementation(libs.ktorServerCore)

    api(libs.swaggerCore)

    testImplementation(libs.ktorServerCoreJvm)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorServerContentNegotiation)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.ktorSerializationJson)
    testImplementation(libs.jsonUnit)
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {
            pom {
                name = "Ktor Query Params Library"
                description = "Facilitates the automatic generation of Swagger definitions for Ktor endpoints that utilize query parameters"
                url = "https://github.com/Vladiatro/ktor-query-params/ktor-query-params-openapi-generator"
            }
        }
    }
}
