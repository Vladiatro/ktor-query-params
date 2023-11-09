package net.falsetrue.ktor.queryparams

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

interface ParamAction

data class TypeAction(
    val type: Class<*>
) : ParamAction

object ManyAction : ParamAction
object RequiredAction : ParamAction

data class Param<T>(
    val name: String,
    val context: RouteContext,
    val actions: List<ParamAction>,
    val receiver: (ApplicationCall, Parameters) -> T
) {
    fun <R> replaceBy(p: Param<R>) {
        this.context.plugin.replaceParam(this, p)
    }

    fun withAction(action: ParamAction): Param<T> {
        val newParam: Param<T> = Param(
            name,
            context,
            actions + action,
            receiver
        )
        replaceBy(newParam)
        return newParam
    }
}

fun Route.stringParam(name: String): Param<String?> {
    return paramsPlugin().stringParam(this, name)
}

fun Route.intParam(name: String) = stringParam(name)
    .convert { it?.toInt() }

fun Route.boolParam(name: String) = stringParam(name)
    .convert { it?.toBoolean() }

fun Route.localDateParam(name: String) = stringParam(name)
    .convert {
        try {
            it?.let(LocalDate::parse)
        } catch (e: DateTimeParseException) {
            throw ArgumentParseException(name, it, "Invalid date format for $name: $it", e)
        }
    }

inline fun <reified T : Enum<T>> Route.enumParam(name: String) = stringParam(name)
    .convert { input ->
        input?.let {
            T::class.java.enumConstants.find { input.lowercase() == it.name.lowercase() }
                ?: throw EnumParseException(name, T::class.java, input)
        }
    }

inline fun <T, reified R> Param<T>.convert(crossinline converter: RouteCallContext.(T) -> R): Param<R> {
    val newParam: Param<R> = Param(
        name,
        context,
        actions + TypeAction(R::class.java)
    ) { call, params ->
        try {
            converter(RouteCallContext(call), receiver(call, params))
        } catch (e: ArgumentParseException) {
            throw e
        } catch (e: Exception) {
            throw ArgumentParseException(name, receiver(call, params)?.toString(), cause = e)
        }
    }
    replaceBy(newParam)
    return newParam
}

fun <T> Param<T?>.many(): Param<List<T>> {
    val newParam: Param<List<T>> = Param(
        name,
        context,
        actions + ManyAction
    ) { call, params ->
        params.getAll(name)?.map {
            receiver(call, parametersOf(name, it))!!
        } ?: emptyList()
    }
    replaceBy(newParam)
    return newParam
}

fun <T> Param<T?>.required(): Param<T> {
    val newParam: Param<T> = Param(
        name,
        context,
        actions + RequiredAction
    ) { call, params ->
        receiver(call, params) ?: throw RequiredParameterNotSpecifiedException(name)
    }
    replaceBy(newParam)
    return newParam
}

inline fun <reified T> Param<T>.onMissing(crossinline handler: RouteCallContext.(paramName: String) -> Unit): Param<T> {
    return catch<RequiredParameterNotSpecifiedException, T> { e -> handler(this, e.name) }
}

inline fun <reified E : Exception, reified T> Param<T>.catch(
    crossinline exceptionHandler: RouteCallContext.(exception: E) -> Unit
) : Param<T> {
    val newParam: Param<T> = Param(name, context, actions) { call, params ->
        try {
            receiver(call, params)
        } catch (e: Exception) {
            val context = RouteCallContext(call)
            if (e is E) {
                exceptionHandler(context, e)
            }
            if (e.cause != null && e.cause is E) {
                exceptionHandler(context, e.cause as E)
            }
            throw e
        }
    }
    replaceBy(newParam)
    return newParam
}

inline fun <reified T> Param<T>.onParseError(
    crossinline handler: RouteCallContext.(paramName: String, paramValue: String?) -> Unit
): Param<T> {
    return catch<ArgumentParseException, T> { e -> handler(this, name, e.value) }
}

fun <T> ApplicationCall.get(param: Param<T>): T {
    return try {
        param.receiver(this, parameters)
    } catch (e: ArgumentParseException) {
        throw e
    }
}

open class ArgumentParseException(
    val name: String,
    val value: String?,
    message: String = "Invalid format for parameter $name",
    cause: Exception? = null
) : IllegalArgumentException(message, cause)

class EnumParseException(name: String, val enum: Class<*>, val notFoundValue: String) :
    ArgumentParseException(name, notFoundValue, message = "$notFoundValue is not valid for ${enum.simpleName}")

class RequiredParameterNotSpecifiedException(name: String) :
    ArgumentParseException(name, null, message = "Parameter $name is not specified")
