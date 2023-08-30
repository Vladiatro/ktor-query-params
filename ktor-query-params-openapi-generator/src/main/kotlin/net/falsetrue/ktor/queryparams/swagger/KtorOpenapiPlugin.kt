package net.falsetrue.ktor.queryparams.swagger

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.swagger.models.Path
import io.swagger.models.Swagger
import java.util.*

object KtorOpenapiPlugin : BaseApplicationPlugin<Application, KtorOpenapiPluginContext, KtorOpenapiPluginContext> {
    override val key: AttributeKey<KtorOpenapiPluginContext> = AttributeKey("Ktor OpenAPI plugin")

    override fun install(pipeline: Application, configure: KtorOpenapiPluginContext.() -> Unit): KtorOpenapiPluginContext {
        return KtorOpenapiPluginContext().apply(configure)
    }
}

class KtorOpenapiPluginContext {
    val swagger = Swagger()
    var isComplete = false
    val additionalRouteActions = IdentityHashMap<Route, Path.() -> Unit>()
    val excludedRoutes = IdentityHashMap<Route, Unit>()
}
