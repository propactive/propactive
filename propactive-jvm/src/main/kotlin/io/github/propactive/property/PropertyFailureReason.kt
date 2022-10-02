package io.github.propactive.property

import io.github.propactive.config.UNSPECIFIED_ENVIRONMENT
import io.github.propactive.property.PropertyBuilder.Companion.GLOBALLY_VALID_NAME
import io.github.propactive.type.Type

object PropertyFailureReason {
    // BUILDER FAILURES

    internal val PROPERTY_NAME_IS_NOT_SET = {
        "Property does not have name set"
    }

    internal val PROPERTY_ENVIRONMENT_IS_NOT_SET = { fieldName: String ->
        { describe(fieldName) + "does not have environment name set" }
    }

    internal val PROPERTY_VALUE_IS_NOT_SET = { fieldName: String, environmentName: String ->
        { describe(fieldName, environmentName) + "does not have value set" }
    }

    internal val PROPERTY_FIELD_HAS_INVALID_NAME = { fieldName: String, environmentName: String ->
        { describe(fieldName, environmentName) + "does not adhere to the following regular language: ${GLOBALLY_VALID_NAME.pattern}" }
    }

    internal val PROPERTY_SET_MANDATORY_IS_BLANK = { fieldName: String, environmentName: String ->
        { describe(fieldName, environmentName) + "is set mandatory but has a blank value" }
    }

    internal val PROPERTY_VALUE_HAS_INVALID_TYPE = { fieldName: String, environmentName: String, value: String, type: Type ->
        { describe(fieldName, environmentName) + "was expected to be of type: ${type::class.simpleName}, but value was: $value" }
    }

    // FACTORY FAILURES

    internal val PROPERTY_FIELD_INACCESSIBLE = { fieldName: String ->
        { describe(fieldName) + "is annotated with @${Property::class.simpleName}, so it should not be private" }
    }

    internal val PROPERTY_FIELD_HAS_INVALID_TYPE = { fieldName: String ->
        { describe(fieldName) + "is annotated with @${Property::class.simpleName}, so field type should be ${String::class.simpleName}" }
    }

    private fun describe(field: String, environment: String = UNSPECIFIED_ENVIRONMENT) =
        "Property named: $field " + if (environment.isNotBlank()) "within environment named: $environment " else " "
}
