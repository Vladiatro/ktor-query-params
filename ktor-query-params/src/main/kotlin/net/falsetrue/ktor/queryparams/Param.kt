package net.falsetrue.ktor.queryparams

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.time.LocalDate

interface ParamAction

data class TypeAction (
    val type: Class<*>
) : ParamAction
object ManyAction : ParamAction
object RequiredAction : ParamAction

class Param<T>(
    val name: String,
    val context: RouteContext,
    val actions: List<ParamAction>,
    val receiver: (Parameters) -> T
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
    .convert { it?.let(LocalDate::parse) }

inline fun <reified T : Enum<T>> Route.enumParam(name: String) = stringParam(name)
    .convert { input ->
        input?.let {
            T::class.java.enumConstants.find { input.lowercase() == it.name.lowercase() }
                ?: throw EnumParseException(T::class.java, input)
        }
    }

inline fun <T, reified R> Param<T>.convert(crossinline converter: (T) -> R): Param<R> {
    val newParam: Param<R> = Param(
        name,
        context,
        actions + TypeAction(R::class.java)
    ) { converter(receiver(it)) }
    replaceBy(newParam)
    return newParam
}

fun <T> Param<T?>.many(): Param<List<T>> {
    val newParam: Param<List<T>> = Param(
        name,
        context,
        actions + ManyAction
    ) { args ->
        args.getAll(name)?.map {
            receiver(parametersOf(name, it))!!
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
    ) { receiver(it)!! }
    replaceBy(newParam)
    return newParam
}

fun <T> ApplicationCall.get(param: Param<T>): T {
    return try {
        param.receiver(parameters)
    } catch (e: Exception) {
        throw ArgumentParseException(param.name, e)
    }
}

class ArgumentParseException(val name: String, cause: Exception): IllegalArgumentException("Invalid format for parameter $name parameter", cause)

class EnumParseException(val enum: Class<*>, val notFoundValue: String): IllegalArgumentException("$notFoundValue is not valid for enum ${enum.simpleName}")
