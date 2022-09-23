package io.github.propactive.environment

import io.github.propactive.entry.EntryModel
import io.github.propactive.environment.EnvironmentBuilder.Companion.GLOBALLY_VALID_FILENAME

object EnvironmentFailureReason {
    // BUILDER FAILURES

    internal val ENVIRONMENT_NAME_IS_NOT_SET =
        { "Environment does not have name set." }

    internal val ENVIRONMENT_FILENAME_IS_NOT_SET = { environmentName: String ->
        { describe(environmentName) + "does not have filename set." }
    }

    internal val ENVIRONMENT_PROPERTIES_IS_NOT_SET = { environmentName: String ->
        { describe(environmentName) + "does not have properties set." }
    }

    internal val ENVIRONMENT_INVALID_FILENAME = { environmentName: String, filename: String ->
        { describe(environmentName) + "has a file named: $filename that does not adhere to the following regular language: ${GLOBALLY_VALID_FILENAME.pattern}" }
    }

    // FACTORY FAILURES

    internal val ENVIRONMENT_MISSING_ANNOTATION = {
        "Target class is not annotated with @${Environment::class.simpleName}"
    }

    internal val ENVIRONMENT_INVALID_KEY_EXPANSION = { entry: EntryModel ->
        { describe(entry.key) + "is concatenated multi-environment entry, but mapped filename: \"${entry.value.trim()}\" doesn't contain a wildcard (\"*\"), and hence filename conflict will occur for: \"$entry\"" }
    }

    private fun describe(environment: String) =
        "Environment named: $environment "
}