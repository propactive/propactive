package io.github.propactive.property

import io.github.propactive.config.BLANK_PROPERTY
import io.github.propactive.type.STRING
import io.github.propactive.type.Type
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
    val value: Array<String> = [BLANK_PROPERTY],
    /**
     * The type of the property value. (used on runtime for validation)
     * Current natively supported types:
     * - [io.github.propactive.type.BASE64]
     * - [io.github.propactive.type.BOOLEAN]
     * - [io.github.propactive.type.DECIMAL]
     * - [io.github.propactive.type.INTEGER]
     * - [io.github.propactive.type.JSON]
     * - [io.github.propactive.type.STRING]
     * - [io.github.propactive.type.URI]
     * - [io.github.propactive.type.URL]
     * - [io.github.propactive.type.UUID]
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
