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
    signing
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

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
        withJavadocJar()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "net.falsetrue"
                from(components["java"])

                pom {
                    scm {
                        connection = "git@github.com:Vladiatro/ktor-query-params.git"
                        developerConnection = "git@github.com:Vladiatro/ktor-query-params.git"
                        url = "https://github.com/Vladiatro/ktor-query-params"
                    }
                    licenses {
                        license {
                            name = "Apache License, Version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0"
                            distribution = "repo"
                        }
                    }
                    developers {
                        developer {
                            id = "Vladiatro"
                            name = "Vladislav Miachikov"
                            email = "vladiator1024@gmail.com"
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Sonatype"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")

                credentials {
                    username = project.findProperty("ossrhUsername")?.toString()
                    password = project.findProperty("ossrhPassword")?.toString()
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
        useInMemoryPgpKeys(
            project.findProperty("gpg.key.id")?.toString() ?: "",
            project.findProperty("gpg.key.passphrase")?.toString() ?: ""
        )
    }
}
