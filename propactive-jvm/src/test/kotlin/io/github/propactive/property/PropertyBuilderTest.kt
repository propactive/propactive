package io.github.propactive.property

import io.github.propactive.property.PropertyBuilder.Companion.propertyBuilder
import io.github.propactive.property.PropertyFailureReason.PROPERTY_ENVIRONMENT_IS_NOT_SET
import io.github.propactive.property.PropertyFailureReason.PROPERTY_FIELD_HAS_INVALID_NAME
import io.github.propactive.property.PropertyFailureReason.PROPERTY_NAME_IS_NOT_SET
import io.github.propactive.property.PropertyFailureReason.PROPERTY_SET_MANDATORY_IS_BLANK
import io.github.propactive.property.PropertyFailureReason.PROPERTY_VALUE_HAS_INVALID_TYPE
import io.github.propactive.property.PropertyFailureReason.PROPERTY_VALUE_IS_NOT_SET
import io.github.propactive.support.utils.alphaNumeric
import io.github.propactive.type.INTEGER
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random

@TestInstance(PER_CLASS)
class PropertyBuilderTest {
    companion object {
        private val GIVEN_NAME = Random.alphaNumeric()
        private val GIVEN_ENVIRONMENT = Random.alphaNumeric()
        private val GIVEN_STRING_VALUE = Random.alphaNumeric()
        private const val IS_MANDATORY = true
    }

    @ParameterizedTest
    @MethodSource("missingExpectedSettersArgs")
    fun `should throw IllegalStateException when property expected values are not set`(
        propertyBuilder: PropertyBuilder,
        errorMessage: String,
    ) {
        assertThrows<IllegalStateException> { propertyBuilder.build() }
            .message shouldBe errorMessage
    }

    @ParameterizedTest
    @MethodSource("invalidStateOfSettersArgs")
    fun `should throw IllegalArgumentException when property setters has invalid setters`(
        propertyBuilder: PropertyBuilder,
        errorMessage: String,
    ) {
        assertThrows<IllegalArgumentException> { propertyBuilder.build() }
            .message shouldBe errorMessage
    }

    private fun missingExpectedSettersArgs() = Stream.of(
        arguments(
            propertyBuilder(),
            PROPERTY_NAME_IS_NOT_SET(),
        ),
        arguments(
            propertyBuilder().withName(GIVEN_NAME),
            PROPERTY_ENVIRONMENT_IS_NOT_SET(GIVEN_NAME)(),
        ),
        arguments(
            propertyBuilder().withName(GIVEN_NAME).withEnvironment(GIVEN_ENVIRONMENT),
            PROPERTY_VALUE_IS_NOT_SET(GIVEN_NAME, GIVEN_ENVIRONMENT)(),
        ),
    )

    private fun invalidStateOfSettersArgs() = Stream.of(
        arguments(
            propertyBuilder()
                .withName("@@invalid@@")
                .withEnvironment(GIVEN_ENVIRONMENT)
                .withValue(GIVEN_STRING_VALUE),
            PROPERTY_FIELD_HAS_INVALID_NAME("@@invalid@@", GIVEN_ENVIRONMENT)(),
        ),
        arguments(
            propertyBuilder(IS_MANDATORY)
                .withName(GIVEN_NAME)
                .withEnvironment(GIVEN_ENVIRONMENT)
                .withValue(""),
            PROPERTY_SET_MANDATORY_IS_BLANK(GIVEN_NAME, GIVEN_ENVIRONMENT)(),
        ),
        arguments(
            propertyBuilder()
                .withName(GIVEN_NAME)
                .withEnvironment(GIVEN_ENVIRONMENT)
                .withType(INTEGER)
                .withValue("invalid"),
            PROPERTY_VALUE_HAS_INVALID_TYPE(GIVEN_NAME, GIVEN_ENVIRONMENT, "invalid", INTEGER)(),
        ),
    )
}
