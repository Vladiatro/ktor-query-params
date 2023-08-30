package net.falsetrue.ktor.queryparams.swagger

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.swagger.models.Path

fun Route.openApi(action: Path.() -> Unit): Route {
    application.pluginOrNull(KtorOpenapiPlugin)?.let {
        it.additionalRouteActions[this] = action
    }
    return this
}

fun Route.excludeFromApi() {
    application.pluginOrNull(KtorOpenapiPlugin)?.let {
        it.excludedRoutes[this] = Unit
    }
}
