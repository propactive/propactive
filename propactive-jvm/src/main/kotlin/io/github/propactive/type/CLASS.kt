package io.github.propactive.type

import io.github.propactive.type.Type.Companion.INVALID
import io.github.propactive.type.Type.Companion.VALID

/**
 * CLASS type defined as a syntactically valid format as
 * per [Java Language Specification (JLS) 3.8. Identifiers](https://docs.oracle.com/javase/specs/jls/se21/html/jls-3.html#jls-3.8)
 */
object CLASS : Type {
    private val VALID_CLASS_NAME_REGEX = """^([a-zA-Z_$][a-zA-Z\d_$]*\.)*[a-zA-Z_$][a-zA-Z\d_$]*$""".toRegex()

    override fun validate(value: Any) = value
        .toString()
        .matches(VALID_CLASS_NAME_REGEX)
        .let { match -> if (match) VALID else INVALID }
}
