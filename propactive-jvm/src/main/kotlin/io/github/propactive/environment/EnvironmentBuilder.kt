package io.github.propactive.environment

import io.github.propactive.commons.Builder
import io.github.propactive.config.EXPANSION_WILDCARD
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_FILENAME_IS_NOT_SET
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_FILENAME
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_NAME_IS_NOT_SET
import io.github.propactive.environment.EnvironmentFailureReason.ENVIRONMENT_PROPERTIES_IS_NOT_SET
import io.github.propactive.property.PropertyModel
import kotlin.text.RegexOption.MULTILINE

class EnvironmentBuilder private constructor() : Builder<EnvironmentModel> {
    private lateinit var name: String
    private lateinit var filename: String
    private lateinit var properties: List<PropertyModel>

    fun withName(name: String) = apply { this.name = name.trim() }
    fun withFilename(filename: String) = apply { this.filename = filename.trim() }
    fun withProperties(properties: List<PropertyModel>) = apply { this.properties = properties }

    companion object {
        internal val GLOBALLY_VALID_FILENAME = Regex("[A-Za-z0-9|._-]{1,255}", MULTILINE)

        fun environmentBuilder() = EnvironmentBuilder()
    }

    override fun build(): EnvironmentModel = apply {
        check(::name.isInitialized, ENVIRONMENT_NAME_IS_NOT_SET)
        check(::filename.isInitialized, ENVIRONMENT_FILENAME_IS_NOT_SET(name))
        check(::properties.isInitialized, ENVIRONMENT_PROPERTIES_IS_NOT_SET(name))
    }.run {
        EnvironmentModel(
            name,
            filename
                .replace(EXPANSION_WILDCARD, name)
                .apply { require(matches(GLOBALLY_VALID_FILENAME), ENVIRONMENT_INVALID_FILENAME(name, this)) },
            properties
                .filter { name == it.environment }
                .toSet(),
        )
    }
}
