package net.falsetrue.ktor.queryparams

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlin.reflect.typeOf

object ParamsPlugin : BaseRouteScopedPlugin<Unit, ParamsPluginContext> {
    override val key: AttributeKey<ParamsPluginContext> = AttributeKey("Query params plugin")

    override fun install(
        pipeline: ApplicationCallPipeline,
        configure: Unit.() -> Unit
    ): ParamsPluginContext {
        return ParamsPluginContext()
    }
}

class ParamsPluginContext {
    val params = mutableMapOf<RouteContext, MutableList<Param<*>>>()
    val responses = mutableMapOf<RouteContext, MutableList<Response<*>>>()

    fun stringParam(route: Route, name: String): Param<String?> {
        return Param(name, RouteContext(this, route), listOf(TypeAction(String::class.java))) { _, params -> params[name] }
            .also { param ->
                params.computeIfAbsent(param.context) { ArrayList() }.add(param)
            }
    }

    inline fun <reified T> responds(
        route: Route,
        code: HttpStatusCode = HttpStatusCode.OK,
        contentType: ContentType = ContentType.Application.Json
    ): Response<T> {
        return Response<T>(code, typeOf<T>(), contentType, RouteContext(this, route), listOf())
            .also { response ->
                responses.computeIfAbsent(response.context) { ArrayList() }.add(response)
            }
    }

    fun replaceParam(old: Param<*>, new: Param<*>) {
        params[old.context]?.let { list ->
            list.indexOf(old).takeIf { it >= 0 }?.let {
                list[it] = new
            }
        }
    }

    fun replaceResponse(old: Response<*>, new: Response<*>) {
        responses[old.context]?.let { list ->
            list.indexOf(old).takeIf { it >= 0 }?.let {
                list[it] = new
            }
        }
    }
}

data class RouteContext(
    val plugin: ParamsPluginContext,
    val route: Route,
)

class RouteCallContext(
    val call: ApplicationCall
) {
    fun <T> Param<T>.get(): T = call.get(this)

    suspend inline fun <reified T : Any> Response<T>.send(body: T) = call.send(this, body)
}

fun Route.doGet(body: suspend RouteCallContext.() -> Unit) {
    get {
        val context = RouteCallContext(this.context)
        body(context)
    }
}

fun Route.doPost(body: suspend RouteCallContext.() -> Unit) {
    post {
        val context = RouteCallContext(this.context)
        body(context)
    }
}

fun Route.paramsPlugin() = pluginOrNull(ParamsPlugin) ?: install(ParamsPlugin)
