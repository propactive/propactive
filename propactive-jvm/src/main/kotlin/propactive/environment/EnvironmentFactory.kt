package propactive.environment

import propactive.config.EXPANSION_WILDCARD
import propactive.config.MULTIPLE_ENVIRONMENT_DELIMITER
import propactive.entry.EntryFactory
import propactive.entry.EntryModel
import propactive.environment.EnvironmentBuilder.Companion.environmentBuilder
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_KEY_EXPANSION
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_MISSING_ANNOTATION
import propactive.property.PropertyFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

object EnvironmentFactory {
    fun create(clazz: KClass<out Any>): Set<EnvironmentModel> =
        requireNotNull(clazz.findAnnotation<Environment>(), ENVIRONMENT_MISSING_ANNOTATION)
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

    private fun List<EntryModel>.expandIfMultipleKeysPerEntry() = this.flatMap { entry ->
        entry.key
            .split(MULTIPLE_ENVIRONMENT_DELIMITER)
            .apply { require((size == 1 || entry.value.contains(EXPANSION_WILDCARD)), ENVIRONMENT_INVALID_KEY_EXPANSION(entry)) }
            .map { EntryModel(it, entry.value) }
    }
}
