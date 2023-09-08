package net.falsetrue.ktor.queryparams

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import kotlin.test.assertEquals

class KtorTest {
    @Test
    fun testGetParams() = testApplication {
        simpleApplication()
        val client = createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                }
            }
        }
        val response = client.get("/test") {
            parameter("string", "stringValue")
            parameter("int", "123")
            parameter("bool", "true")
            parameter("required", "requiredValue")
            parameter("manyParam", "many1")
            parameter("manyParam", "many2")
            parameter("enum", TestEnum.ENUM_1)
            parameter("localDate", "2023-08-01")
        }
        val result: Result = response.body()
        with(result) {
            assertAll({
                assertEquals("stringValue", string)
                assertEquals(123, int)
                assertEquals(true, bool)
                assertEquals("requiredValue", required)
                assertEquals(listOf("many1", "many2"), many)
                assertEquals(null, absentParam)
                assertEquals(LocalDate.parse("2023-08-01"), localDate)
            })
        }
    }

    @Test
    fun testExceptionHandling() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        application {
            routing {
                val intParam = intParam("int")
                get("/test") {
                    try {
                        call.get(intParam)
                    } catch (e: ArgumentParseException) {
                        call.respond("Exception caught in parameter ${e.name}")
                    }
                }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson()
            }
        }
        val response = client.get("/test") {
            parameter("int", "notInt")
        }
        val result: String = response.body()
        assertEquals("Exception caught in parameter int", result)
    }

    @Test
    fun testEnumExceptionHandling() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        application {
            routing {
                val enumParam = enumParam<TestEnum>("enum")
                route("/test") {
                    doGet {
                        try {
                            enumParam.get()
                        } catch (e: EnumParseException) {
                            call.respond("not found: ${e.notFoundValue}, enum: ${e.enum.simpleName}")
                        }
                    }
                }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson()
            }
        }
        val response = client.get("/test") {
            parameter("enum", "wrongEnum")
        }
        val result: String = response.body()
        assertEquals("not found: wrongEnum, enum: TestEnum", result)
    }

    @Test
    fun testRequiredParamNotFound() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        application {
            routing {
                val requiredParam = stringParam("requiredParam").required()
                route("/test") {
                    doGet {
                        try {
                            requiredParam.get()
                        } catch (e: ArgumentParseException) {
                            call.respond(e.message ?: "")
                        }
                    }
                }
            }
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson()
            }
        }
        val response = client.get("/test") { }
        val result: String = response.body()
        assertEquals("Parameter requiredParam is not specified", result)
    }

    @Test
    fun testGetParamsPost() = testApplication {
        simpleApplication()
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val response = client.post("/test") {
            parameter("required", "stringValue")
        }
        val result: String = response.body()
        assertEquals("stringValue", result)
    }

    data class Result(
        val string: String?,
        val int: Int?,
        val bool: Boolean?,
        val required: String,
        val many: List<String>,
        val absentParam: String?,
        val enum: TestEnum?,
        val localDate: LocalDate?,
    )
}

fun TestApplicationBuilder.simpleApplication() = application {
    routing {
        route("/test") {
            val stringParam = stringParam("string")
            val intParam = intParam("int")
            val boolParam = boolParam("bool")
            val requiredParam = stringParam("required").required()
            val manyParam = stringParam("manyParam").many()
            val absentParam = stringParam("absentParam")
            val enumParam = enumParam<TestEnum>("enum")
            val localDateParam = localDateParam("localDate")

            val okResult = responds<KtorTest.Result>()
            doGet {
                okResult.send(
                    KtorTest.Result(
                        stringParam.get(),
                        intParam.get(),
                        boolParam.get(),
                        requiredParam.get(),
                        manyParam.get(),
                        absentParam.get(),
                        enumParam.get(),
                        localDateParam.get()
                    )
                )
            }
        }
        route("/test") {
            val requiredParam = stringParam("required").required()
            post {
                call.respond(call.get(requiredParam))
            }
        }
    }
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
        }
    }
}

enum class TestEnum {
    ENUM_1
}
