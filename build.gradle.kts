buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.kotlinGradlePlugin)
    }
}

plugins {
    application
    `maven-publish`
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "net.falsetrue"
    version = "0.1.0"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    java {
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "net.falsetrue"
                from(components["java"])
            }
        }
    }
}
