package propactive.property

import propactive.type.STRING
import propactive.type.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.reflect.KClass

/**
 * TODO
 */
@Target(PROPERTY, FIELD)
@Retention(RUNTIME)
@MustBeDocumented
annotation class Property(
    /**
     * TODO
     */
    val value: Array<String> = [""],
    /**
     * TODO
     */
    val type: KClass<out Type> = STRING::class,
    /**
     * TODO
     */
    val mandatory: Boolean = true,
)
