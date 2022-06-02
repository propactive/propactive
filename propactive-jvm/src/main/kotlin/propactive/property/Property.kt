package propactive.property

import propactive.type.STRING
import propactive.type.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass

/**
 * Marks the field as an application property.
 * All fields annotated with @Property will be part of the final application properties file.
 *
 * Example:
 * ```
 * @Property(["test: value"])
 * const val property = "app.resource.key"
 * ```
 * will generate the following line in an application properties file:
 * ```
 * app.resource.key=value
 * ```
 */
@Target(PROPERTY, FIELD)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Property(
    /**
     * Property value for each given environment delimited with a colon (:)
     * You can reuse a value for multiple environments using the forward slash (/) delimiter, example:
     * ```
     * @Property(["test/stage/prod: value"])
     * ```
     * Will generate an application property for 3 environments (test, stage, and prod)
     */
    val value: Array<String> = [""],
    /**
     * The type of the property value. (used on runtime for validation)
     * Current natively supported types:
     * - BASE64
     * - BOOLEAN
     * - DECIMAL
     * - INTEGER
     * - JSON
     * - STRING
     * - URI
     * - URL
     * - UUID
     *
     * You can use the interface [Type] to create your own type for custom validation.
     */
    val type: KClass<out Type> = STRING::class,
    /**
     * Marks a property as mandatory.
     * Any property marked as mandatory cannot have blank values within any environment.
     */
    val mandatory: Boolean = true,
)
