package net.falsetrue.ktor.queryparams.swagger

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.converter.ModelConverters
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import net.falsetrue.ktor.queryparams.Param
import net.falsetrue.ktor.queryparams.ParamsPlugin
import net.falsetrue.ktor.queryparams.Response
import net.falsetrue.ktor.queryparams.RouteContext
import kotlin.reflect.javaType

fun Application.getOpenApi(): Swagger {
    return pluginOrNull(KtorOpenapiPlugin)?.let { plugin ->
        if (!plugin.isComplete) {
            pluginOrNull(Routing)?.let { root ->
                fillSwagger(root, plugin)
            }
            plugin.isComplete = true
        }
        plugin.swagger
    } ?: throw IllegalStateException("Plugin KtorOpenapiPlugin is not installed")
}

fun Route.swaggerJsonRoute(path: String = "/swagger.json", method: HttpMethod = HttpMethod.Get) = route(path) {
    method(method) {
        val objectMapper = ObjectMapper().apply {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
        excludeFromApi()
        handle {
            call.respond(objectMapper.writeValueAsString(application.getOpenApi()))
        }
    }
}

private fun fillSwagger(root: Route, plugin: KtorOpenapiPluginContext) {
    root.getAllRoutes().forEach { route ->
        addRoute(route, plugin)
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun addRoute(childRoute: Route, plugin: KtorOpenapiPluginContext) {
    val path = getPath(childRoute.parent)
    val swagger = plugin.swagger
    val swaggerPath = swagger.getPath(path) ?: Path()
    val selector = childRoute.selector

    childRoute.getResponses().forEach {
        ModelConverters.getInstance().readAll(it.type.javaType).forEach { ref, model ->
            swagger.addDefinition(ref, model)
        }
    }

    val operation = getOperation(childRoute)
    if (selector is HttpMethodRouteSelector) {
        when (selector.method) {
            HttpMethod.Get -> swaggerPath.get(operation)
            HttpMethod.Put -> swaggerPath.put(operation)
            HttpMethod.Post -> swaggerPath.post(operation)
            HttpMethod.Head -> swaggerPath.head(operation)
            HttpMethod.Delete -> swaggerPath.delete(operation)
            HttpMethod.Patch -> swaggerPath.patch(operation)
        }
    }
    val shouldBeIncluded = applyActions(childRoute, swaggerPath, plugin)
    if (shouldBeIncluded) {
        swagger.path(path, swaggerPath)
    }
}

private fun getPath(route: Route?): String {
    if (route == null) {
        return ""
    }
    val selector = route.selector
    return when (selector) {
        is RootRouteSelector -> ""
        is TrailingSlashRouteSelector -> ""
        is PathSegmentConstantRouteSelector -> getPath(route.parent) + "/" + selector.value.removePrefix("/")
        else -> getPath(route.parent)
    }
}

/**
 * @return false if should be exluded
 */
private fun applyActions(route: Route?, swaggerPath: Path, plugin: KtorOpenapiPluginContext): Boolean {
    if (route != null) {
        if (!applyActions(route.parent, swaggerPath, plugin)) {
            return false
        }
    }
    plugin.additionalRouteActions[route]?.invoke(swaggerPath)
    return !plugin.excludedRoutes.contains(route)
}

private fun getOperation(childRoute: Route): Operation {
    return Operation().apply {
        childRoute.getParams().forEach { param ->
            getParameter(param)?.let(::addParameter)
        }
        childRoute.getResponses().forEach { response ->
            produces = (produces ?: emptyList()) + response.contentType.let { it.contentType + "/" + it.contentSubtype }
            produces = produces.distinct()
            addResponse(response.code.value.toString(), getResponse(response))
        }
    }
}

private fun Route.getParams(): List<Param<*>> {
    val result = arrayListOf<Param<*>>()
    pluginOrNull(ParamsPlugin)?.let { plugin ->
        plugin.params[RouteContext(plugin, this)]?.forEach(result::add)
    }
    return parent?.let { result + parent!!.getParams() } ?: result
}

private fun Route.getResponses(): List<Response<*>> {
    val result = arrayListOf<Response<*>>()
    pluginOrNull(ParamsPlugin)?.let { plugin ->
        plugin.responses[RouteContext(plugin, this)]?.forEach(result::add)
    }
    return parent?.let { result + parent!!.getResponses() } ?: result
}
