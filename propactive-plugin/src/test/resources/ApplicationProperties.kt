import io.github.propactive.property.Property
import io.github.propactive.environment.Environment
import io.github.propactive.type.INTEGER

@Environment
object ApplicationProperties {
    @Property(["ABC"])
    const val stringPropertyKey = "propactive.dev.string.property.key"

    @Property(["42"], type = INTEGER::class)
    const val intPropertyKey = "propactive.dev.int.property.key"
}
