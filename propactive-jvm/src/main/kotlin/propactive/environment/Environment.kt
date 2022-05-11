package propactive.environment

import propactive.config.DEFAULT_ENVIRONMENT_FILENAME
import propactive.config.KEY_VALUE_DELIMITER
import propactive.config.UNSPECIFIED_ENVIRONMENT
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

/**
 * TODO
 */
@Target(CLASS)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Environment(
    /**
     * TODO
     */
    val value: Array<String> = ["$UNSPECIFIED_ENVIRONMENT${KEY_VALUE_DELIMITER}$DEFAULT_ENVIRONMENT_FILENAME"],
)
