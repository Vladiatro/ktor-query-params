package net.falsetrue.ktor.queryparams.swagger

import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.*
import net.falsetrue.ktor.queryparams.*
import java.time.LocalDate

data class ParamSwaggerAction(
    val action: (Property) -> Unit
) : ParamAction

fun <T> Param<T>.openApi(action: Property.() -> Unit): Param<T> {
    val newParam: Param<T> = Param(
        name,
        context,
        actions + ParamSwaggerAction(action),
        receiver
    )
    replaceBy(newParam)
    return newParam
}

fun <T> Param<T>.description(description: String): Param<T> = openApi {
    this.description = description
}

internal fun getParameter(param: Param<*>): QueryParameter {
    return QueryParameter().also { swaggerParam ->
        swaggerParam.name = param.name
        var property: Property? = null
        param.actions.forEach { action ->
            property = applyChange(property, action)
        }
        property?.let {
            swaggerParam.type = it.type
            swaggerParam.description = it.description
            swaggerParam.example(it.example?.toString())
            swaggerParam.format = it.format
            swaggerParam.required = it.required
            if (it is StringProperty) {
                swaggerParam.enum = it.enum
            }
            if (it is ArrayProperty) {
                swaggerParam.items = it.items
            }
        }
    }
}

private fun applyChange(property: Property?, action: ParamAction): Property? {
    return when (action) {
        is ManyAction -> ArrayProperty(property)
        is RequiredAction -> property?.apply {
            required = true
        }
        is TypeAction -> {
            if (action.type.isEnum) {
                enumProperty(action)
            } else {
                when (action.type) {
                    String::class.java -> StringProperty()
                    Int::class.java, java.lang.Integer::class.java -> IntegerProperty()
                    Boolean::class.java, java.lang.Boolean::class.java -> BooleanProperty()
                    LocalDate::class.java -> StringProperty().apply {
                        format = "date"
                    }
                    else -> StringProperty()
                }
            }
        }
        is ParamSwaggerAction -> property?.apply(action.action)
        else -> property
    }
}

private fun enumProperty(action: TypeAction): StringProperty {
    return StringProperty().apply {
        enum = action.type.enumConstants.map { it.toString() }
    }
}
