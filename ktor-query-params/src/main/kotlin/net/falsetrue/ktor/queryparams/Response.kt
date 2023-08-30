package net.falsetrue.ktor.queryparams

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KType

interface ResponseAction

class Response<T>(
    val code: HttpStatusCode,
    val type: KType,
    val contentType: ContentType,
    val context: RouteContext,
    val actions: List<ResponseAction>
) {
    fun <R> replaceBy(p: Response<R>) {
        this.context.plugin.replaceResponse(this, p)
    }
}

inline fun <reified T> Route.responds(
    code: HttpStatusCode = HttpStatusCode.OK,
    contentType: ContentType = ContentType.Application.Json
): Response<T> {
    return paramsPlugin().responds(this, code, contentType)
}

suspend inline fun <reified T : Any> ApplicationCall.send(r: Response<T>, entity: T) {
    response.status(r.code)
    respond(entity)
}
