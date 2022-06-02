package propactive.environment

import propactive.config.DEFAULT_ENVIRONMENT_FILENAME
import propactive.config.KEY_VALUE_DELIMITER
import propactive.config.UNSPECIFIED_ENVIRONMENT
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Marks an object as an Environment to generate application property files.
 *
 * Example:
 * ```
 * @Environment(["test: application.properties"])
 * object WithDifferentEnvironmentValues {
 *   @Property(["test: value"])
 *   const val property = "app.resource.key"
 * }
 * ```
 * Will generate an application properties file named "application.properties" with the following lines:
 * ```
 * app.resource.key=value
 * ```
 */
@Target(CLASS)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Environment(
    /**
     * The environment to file mapping to generate delimited with a colon (:)
     * You can reuse a filename for multiple environments using the forward slash (/) delimiter,
     * and an environment wildcard (*) for expansion, example:
     * ```
     * @Environment(["test/stage/prod: *-application.properties"])
     * object WithDifferentEnvironmentValues {
     *   // ...
     * }
     * ```
     * This will generate 3 files with the following names:
     * - test-application.properties
     * - stage-application.properties
     * - prod-application.properties
     */
    val value: Array<String> = ["$UNSPECIFIED_ENVIRONMENT${KEY_VALUE_DELIMITER}$DEFAULT_ENVIRONMENT_FILENAME"],
)
