package propactive.environment

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import propactive.environment.EnvironmentBuilder.Companion.environmentBuilder
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_FILENAME_IS_NOT_SET
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_INVALID_FILENAME
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_NAME_IS_NOT_SET
import propactive.environment.EnvironmentFailureReason.ENVIRONMENT_PROPERTIES_IS_NOT_SET
import propactive.support.utils.alphaNumeric
import java.util.stream.Stream
import kotlin.random.Random

@TestInstance(PER_CLASS)
class EnvironmentBuilderTest {
    companion object {
        private val GIVEN_FILENAME = Random.alphaNumeric()
        private val GIVEN_ENVIRONMENT = Random.alphaNumeric()
    }

    @ParameterizedTest
    @MethodSource("missingExpectedSettersArgs")
    fun `should throw IllegalStateException when property expected values are not set`(
        environmentBuilder: EnvironmentBuilder,
        errorMessage: String
    ) {
        assertThrows<IllegalStateException> { environmentBuilder.build() }
            .message shouldBe errorMessage
    }

    @ParameterizedTest
    @MethodSource("invalidStateOfSettersArgs")
    fun `should throw IllegalArgumentException when property setters has invalid setters`(
        environmentBuilder: EnvironmentBuilder,
        errorMessage: String
    ) {
        assertThrows<IllegalArgumentException> { environmentBuilder.build() }
            .message shouldBe errorMessage
    }

    private fun missingExpectedSettersArgs() = Stream.of(
        arguments(
            environmentBuilder(),
            ENVIRONMENT_NAME_IS_NOT_SET()
        ),
        arguments(
            environmentBuilder()
                .withName(GIVEN_ENVIRONMENT),
            ENVIRONMENT_FILENAME_IS_NOT_SET(GIVEN_ENVIRONMENT)()
        ),
        arguments(
            environmentBuilder()
                .withName(GIVEN_ENVIRONMENT)
                .withFilename(GIVEN_FILENAME),
            ENVIRONMENT_PROPERTIES_IS_NOT_SET(GIVEN_ENVIRONMENT)()
        ),
    )

    private fun invalidStateOfSettersArgs() = Stream.of(
        arguments(
            environmentBuilder()
                .withName(GIVEN_ENVIRONMENT)
                .withFilename("@@invalid@@")
                .withProperties(emptyList()),
            ENVIRONMENT_INVALID_FILENAME(GIVEN_ENVIRONMENT, "@@invalid@@")()
        ),
    )
}
