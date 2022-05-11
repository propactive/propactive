package propactive.property

import propactive.config.MULTIPLE_ENVIRONMENT_DELIMITER
import propactive.config.UNSPECIFIED_ENVIRONMENT
import propactive.entry.EntryFactory
import propactive.property.PropertyFailureReason.PROPERTY_FIELD_HAS_INVALID_TYPE
import propactive.property.PropertyFailureReason.PROPERTY_FIELD_INACCESSIBLE
import kotlin.reflect.KCallable
import kotlin.reflect.KVisibility.PRIVATE
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf

internal object PropertyFactory {
    fun create(kCallables: Collection<KCallable<*>>): List<PropertyModel> = kCallables
        .mapNotNull { it.findAnnotation<Property>()?.run { it to this } }
        .flatMap { (k, property) ->
            check(k.visibility != PRIVATE, PROPERTY_FIELD_INACCESSIBLE(k.name))
            check(k.returnType.isSubtypeOf(String::class.createType()), PROPERTY_FIELD_HAS_INVALID_TYPE(k.name))
            property
                .value
                .run(EntryFactory::create)
                .flatMap { (environment, value) ->
                    environment
                        .expandIfMultipleKeysPerEntry()
                        .setEnvironmentToUnspecifiedIfNoKeysWerePresent()
                        .map { env ->
                            @Suppress("UNCHECKED_CAST")
                            PropertyBuilder.propertyBuilder(property.mandatory)
                                .withName((k as KCallable<String>).call())
                                .withEnvironment(env)
                                .withType((property.type.objectInstance ?: property.type.createInstance()))
                                .withValue(value)
                                .build()
                        }
                }
        }

    private fun String.expandIfMultipleKeysPerEntry(): List<String> = this
        .split(MULTIPLE_ENVIRONMENT_DELIMITER)
        .filter { it.isNotBlank() }

    private fun List<String>.setEnvironmentToUnspecifiedIfNoKeysWerePresent() =
        this.ifEmpty { plus(UNSPECIFIED_ENVIRONMENT) }
}
