buildscript {
    repositories {
        mavenLocal()
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
    apply(plugin = "application")

    group = "net.falsetrue"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
}
