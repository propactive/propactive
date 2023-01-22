package io.github.propactive.environment

import io.github.propactive.config.EXPANSION_WILDCARD
import io.github.propactive.config.MULTIPLE_ENVIRONMENT_DELIMITER
import io.github.propactive.entry.EntryFactory
import io.github.propactive.entry.EntryModel
import io.github.propactive.environment.EnvironmentBuilder.Companion.environmentBuilder
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_KEY_EXPANSION
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_MISSING_ANNOTATION
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.info
import io.github.propactive.property.PropertyFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object EnvironmentFactory {
    @JvmStatic
    fun create(clazz: KClass<out Any>): Set<EnvironmentModel> = clazz
        .info { "Constructing Environments..." }
        .debug { "With annotations: $annotations" }
        .run { requireNotNull(findAnnotation<Environment>(), ENVIRONMENT_MISSING_ANNOTATION) }
        .value
        .run(EntryFactory::create)
        .expandIfMultipleKeysPerEntry()
        .let { entries ->
            with(PropertyFactory.create(clazz.members)) {
                entries.map { (environment, filename) ->
                    environmentBuilder()
                        .withName(environment)
                        .withFilename(filename)
                        .withProperties(this)
                        .build()
                }
            }
        }
        .toSet()
        .info { "Done - Number of Environments: $size" }

    private fun List<EntryModel>.expandIfMultipleKeysPerEntry() = this.flatMap { entry ->
        entry.key
            .split(MULTIPLE_ENVIRONMENT_DELIMITER)
            .apply { require((size == 1 || entry.value.contains(EXPANSION_WILDCARD)), ENVIRONMENT_INVALID_KEY_EXPANSION(entry)) }
            .map { EntryModel(it, entry.value) }
    }
}
