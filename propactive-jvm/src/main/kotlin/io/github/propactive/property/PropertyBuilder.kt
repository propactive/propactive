package io.github.propactive.property

import io.github.propactive.commons.Builder
import io.github.propactive.logging.PropactiveLogger.debug
import io.github.propactive.logging.PropactiveLogger.trace
import io.github.propactive.property.PropertyFailureReason.PROPERTY_ENVIRONMENT_IS_NOT_SET
import io.github.propactive.property.PropertyFailureReason.PROPERTY_FIELD_HAS_INVALID_NAME
import io.github.propactive.property.PropertyFailureReason.PROPERTY_NAME_IS_NOT_SET
import io.github.propactive.property.PropertyFailureReason.PROPERTY_SET_MANDATORY_IS_BLANK
import io.github.propactive.property.PropertyFailureReason.PROPERTY_VALUE_HAS_INVALID_TYPE
import io.github.propactive.property.PropertyFailureReason.PROPERTY_VALUE_IS_NOT_SET
import io.github.propactive.type.STRING
import io.github.propactive.type.Type
import kotlin.text.RegexOption.MULTILINE

class PropertyBuilder private constructor(
    private val mandatory: Boolean = false,
) : Builder<PropertyModel> {
    private lateinit var name: String
    private lateinit var environment: String
    private lateinit var value: String
    private var type: Type = STRING

    fun withName(name: String) = apply { this.name = name.trim() }
    fun withEnvironment(environment: String) = apply { this.environment = environment.trim() }
    fun withValue(value: String) = apply { this.value = value.trim() }
    fun withType(type: Type) = apply { this.type = type }

    companion object {
        internal val GLOBALLY_VALID_NAME = Regex("[A-Za-z0-9._-]{1,255}", MULTILINE)

        @JvmStatic
        internal fun propertyBuilder(mandatory: Boolean = false) = PropertyBuilder(mandatory)
    }

    override fun build(): PropertyModel = this
        .apply {
            check(::name.isInitialized, PROPERTY_NAME_IS_NOT_SET)
            check(::environment.isInitialized, PROPERTY_ENVIRONMENT_IS_NOT_SET(name))
            check(::value.isInitialized, PROPERTY_VALUE_IS_NOT_SET(name, environment))
            require(name.matches(GLOBALLY_VALID_NAME), PROPERTY_FIELD_HAS_INVALID_NAME(name, environment))
            require(!(mandatory && value.isBlank()), PROPERTY_SET_MANDATORY_IS_BLANK(name, environment))
            require(type.validate(value), PROPERTY_VALUE_HAS_INVALID_TYPE(name, environment, value, type))
        }
        .debug { "Building property: $name ($environment)" }
        .run { PropertyModel(name, environment, value) }
        .trace { "Built property: $this" }
}
