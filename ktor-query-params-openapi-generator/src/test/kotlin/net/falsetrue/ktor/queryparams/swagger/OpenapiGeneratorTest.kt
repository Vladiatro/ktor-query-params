package net.falsetrue.ktor.queryparams.swagger

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.swagger.annotations.ApiModelProperty
import io.swagger.models.Info
import io.swagger.models.properties.EmailProperty
import net.falsetrue.ktor.queryparams.*
import net.javacrumbs.jsonunit.JsonAssert
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OpenapiGeneratorTest {
    @Test
    fun testOpenapiGeneration() = testApplication {
        simpleApplication()
        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val response = client.get("/swagger.json")
        JsonAssert.assertJsonEquals(
            javaClass.getResourceAsStream("/net/falsetrue/ktor/queryparams/swagger/swagger.json")?.readAllBytes()?.decodeToString(),
            response.body<String>()
        )
    }
}

fun TestApplicationBuilder.simpleApplication() = application {
    install(KtorOpenapiPlugin) {
        swagger.info = Info().apply {
            version = "1.0.0"
            title = "Test API"
        }
    }
    routing {
        route("/test") {
            val stringParam = stringParam("string")
                .description("A string param")
            val intParam = intParam("int")
                .description("An int param")
            val boolParam = boolParam("bool")
                .description("A bool param")
            val requiredParam = stringParam("required").required()
                .description("A required string param")
            val manyParam = stringParam("manyParam").many()
                .description("An array param")
            val enumParam = enumParam<TestEnum>("enum")
                .description("An enum param")
            val localDateParam = localDateParam("localDate")
                .description("A local date param")
            val hiddenParam = stringParam("hidden")
                .hidden()
            val customApiProperty = stringParam("custom")
                .openApi(EmailProperty())

            val okResult = responds<Result>()
                .description("Successful operation")
            val badRequest = responds<Error>(HttpStatusCode.BadRequest)
                .description("Bad request")
            openApi {
                get.tags = listOf("tag1", "tag2")
            }
            doGet {
                hiddenParam.get()
                customApiProperty.get()
                try {
                    okResult.send(
                        Result(
                            stringParam.get(),
                            intParam.get(),
                            boolParam.get(),
                            requiredParam.get(),
                            manyParam.get(),
                            enumParam.get(),
                            localDateParam.get(),
                        )
                    )
                } catch (e: Exception) {
                    badRequest.send(Error("An error occurred"))
                }
            }
        }
        route("/exlude") {
            excludeFromApi()
            get {

            }
        }
        swaggerJsonRoute()
    }
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        jackson()
    }
}

private data class Result(
    val string: String?,
    val int: Int?,
    val bool: Boolean?,
    val required: String,
    val many: List<String>,
    val enum: TestEnum?,
    val localDate: LocalDate?,
    @ApiModelProperty(hidden = true)
    val hiddenProperty: String = "hidden",
)

private data class Error(
    val message: String
)

@Suppress("unused")
private enum class TestEnum {
    ENUM_1, ENUM_2
}
