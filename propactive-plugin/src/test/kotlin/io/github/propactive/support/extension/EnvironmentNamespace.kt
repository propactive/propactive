package io.github.propactive.support.extension

import io.github.propactive.support.utils.alphaNumeric
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.random.Random

internal class EnvironmentNamespace(
    id: String = Random.alphaNumeric("environment-namespace")
) {
    private val namespace = ExtensionContext.Namespace.create(id)

    internal fun get(context: ExtensionContext) =
        context.getStore(namespace)!!

    internal inline fun <reified T> get(context: ExtensionContext, component: Component): T? =
        this.get(context).get(component.name, T::class.java)

    internal inline fun <reified T> remove(context: ExtensionContext, component: Component): T? =
        this.get(context).remove(component.name, T::class.java)

    internal fun <T> put(context: ExtensionContext, component: Component, value: T): T =
        value.apply { get(context).put(component.name, value) }

    internal enum class Component {
        ProjectDirectory,
        MainSourceSet,
        ResourcesSourceSet,
        BuildScript,
        BuildOutput,
    }
}
