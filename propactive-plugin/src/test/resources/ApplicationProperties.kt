import io.github.propactive.property.Property
import io.github.propactive.environment.Environment
import io.github.propactive.type.INTEGER

/**
 * This is a dummy class that is used for plugin integration tests.
 *
 * @see io.github.propactive.task.ApplicationPropertiesTaskIT
 * @see io.github.propactive.support.extension.KotlinEnvironmentExtension
 */
@Environment
@Suppress("unused")
object ApplicationProperties {
    @Property(["ABC"])
    const val stringPropertyKey = "propactive.dev.string.property.key"

    @Property(["42"], type = INTEGER::class)
    const val intPropertyKey = "propactive.dev.int.property.key"
}
