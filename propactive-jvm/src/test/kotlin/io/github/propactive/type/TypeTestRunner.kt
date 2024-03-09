package io.github.propactive.type

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(PER_CLASS)
abstract class TypeTestRunner {
    internal abstract val underTest: Type
    abstract fun validValues(): Array<Any>
    abstract fun invalidValues(): Array<Any>

    @ParameterizedTest
    @MethodSource("validValues")
    fun `Given a valid type as string, when validated, then it should NOT error`(validValue: Any) {
        underTest.validate(validValue).shouldBeTrue()
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    fun `Given an invalid type as string, when validated, then it should error`(invalidValue: Any) {
        underTest.validate(invalidValue).shouldBeFalse()
    }
}
