package io.github.propactive.plugin

import org.gradle.api.tasks.Input

/**
 * Propactive configurations.
 *
 * @param environments Comma separated list of environments to generate the properties (i.e. test,stage,prod)
 * @param implementationClass Sets the location of your properties object (i.e. com.package.path.to.your.ApplicationProperties)
 * @param destination Sets the location of your generated properties file within the build directory.
 * @param filenameOverride Allows overriding given filename for an environment. (This should only be used when generating application properties for a singular environment)
 * @param classCompileDependency The task Propactive will depend on to load/compile your properties class/object you created.
 */
open class Configuration(
    @get:Input var environments: String = DEFAULT_ENVIRONMENTS,
    @get:Input var implementationClass: String = DEFAULT_IMPLEMENTATION_CLASS,
    @get:Input var destination: String = DEFAULT_BUILD_DESTINATION,
    @get:Input var filenameOverride: String = DEFAULT_FILENAME_OVERRIDE,
    @get:Input var classCompileDependency: String = DEFAULT_CLASS_COMPILE_DEPENDENCY,
) {
    companion object {
        internal const val DEFAULT_ENVIRONMENTS = "*"
        internal const val DEFAULT_IMPLEMENTATION_CLASS = "ApplicationProperties"
        internal const val DEFAULT_BUILD_DESTINATION = "properties"
        // NOTE: Gradle plugin extension values cannot be null if you want to register them as property values
        internal const val DEFAULT_FILENAME_OVERRIDE = ""
        internal const val DEFAULT_CLASS_COMPILE_DEPENDENCY = "jar"
    }
}
