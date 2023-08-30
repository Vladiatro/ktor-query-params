dependencies {
    implementation(project(":ktor-query-params"))

    implementation(libs.ktorServerCore)
    implementation(libs.swaggerCore)

    testImplementation(libs.ktorServerCoreJvm)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.ktorServerContentNegotiation)
    testImplementation(libs.ktorClientContentNegotiation)
    testImplementation(libs.ktorSerializationJson)
    testImplementation(libs.jsonUnit)
}
