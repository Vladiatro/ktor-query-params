package net.falsetrue.ktor.queryparams.swagger

import io.swagger.converter.ModelConverters
import io.swagger.models.Response
import net.falsetrue.ktor.queryparams.ResponseAction
import kotlin.reflect.javaType

data class ResponseSwaggerAction(
    val action: (io.swagger.models.Response) -> Unit
) : ResponseAction

fun <T> net.falsetrue.ktor.queryparams.Response<T>.openApi(action: io.swagger.models.Response.() -> Unit): net.falsetrue.ktor.queryparams.Response<T> {
    val newParam: net.falsetrue.ktor.queryparams.Response<T> = net.falsetrue.ktor.queryparams.Response(
        code, type, contentType, context,
        actions + ResponseSwaggerAction(action),
    )
    replaceBy(newParam)
    return newParam
}

fun <T> net.falsetrue.ktor.queryparams.Response<T>.description(description: String): net.falsetrue.ktor.queryparams.Response<T> = openApi {
    this.description = description
}

@OptIn(ExperimentalStdlibApi::class)
internal fun getResponse(response: net.falsetrue.ktor.queryparams.Response<*>): Response {
    return Response().apply {
        schema = ModelConverters.getInstance().readAsProperty(response.type.javaType)
        response.actions.forEach { action ->
            applyChange(this, action)
        }
    }
}

private fun applyChange(swaggerResponse: Response, action: ResponseAction) {
    when (action) {
        is ResponseSwaggerAction -> action.action(swaggerResponse)
    }
}
