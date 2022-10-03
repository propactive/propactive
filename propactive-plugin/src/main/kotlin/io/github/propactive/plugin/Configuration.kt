package io.github.propactive.plugin

/**
 * Propactive configurations.
 *
 * @param environments Comma separated list of environments to generate the properties (i.e. test,stage,prod)
 * @param implementationClass Sets the location of your properties object (i.e. com.package.path.to.your.ApplicationProperties)
 * @param destination Sets the location of your generated properties file within the build directory.
 * @param filenameOverride Allows overriding given filename for an environment. (This should only be used when generating application properties for a singular environment)
 */
open class Configuration(
    var environments: String? = null,
    var implementationClass: String? = null,
    var destination: String? = null,
    var filenameOverride: String? = null,
)
