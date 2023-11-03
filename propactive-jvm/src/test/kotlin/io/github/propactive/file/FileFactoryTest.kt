package io.github.propactive.file

import io.github.propactive.environment.EnvironmentBuilder.Companion.environmentBuilder
import io.github.propactive.matcher.FileMatcher.Companion.shouldMatch
import io.github.propactive.property.PropertyModel
import io.github.propactive.support.utils.alphaNumeric
import org.junit.jupiter.api.Test
import kotlin.random.Random

class FileFactoryTest {
    companion object {
        private val GIVEN_ENVIRONMENT_NAME = Random.alphaNumeric("test")
        private val GIVEN_ENVIRONMENT_FILENAME = Random.alphaNumeric("application-test", ".properties")

        private val GIVEN_PROPERTY_NAME = Random.alphaNumeric("property-key")
        private val GIVEN_PROPERTY_VALUE = Random.alphaNumeric("property-value")
        private val GIVEN_PROPERTY_MODEL = PropertyModel(GIVEN_PROPERTY_NAME, GIVEN_ENVIRONMENT_NAME, GIVEN_PROPERTY_VALUE)

        private val GIVEN_ENVIRONMENT_BUILDER = {
            environmentBuilder()
                .withName(GIVEN_ENVIRONMENT_NAME)
                .withFilename(GIVEN_ENVIRONMENT_FILENAME)
                .withProperties(listOf(GIVEN_PROPERTY_MODEL))
        }
    }

    @Test
    fun `should create the correct file model for a given environment model`() {
        GIVEN_ENVIRONMENT_BUILDER()
            .build()
            .let(::setOf)
            .let(FileFactory::create)
            .single()
            .shouldMatch {
                withEnvironment(GIVEN_ENVIRONMENT_NAME)
                withFilename(GIVEN_ENVIRONMENT_FILENAME)
                withContent("$GIVEN_PROPERTY_NAME=$GIVEN_PROPERTY_VALUE")
            }
    }

    @Test
    fun `should separate written properties by newline`() {
        val anotherPropertyName = Random.alphaNumeric("property-key")
        val anotherPropertyValue = Random.alphaNumeric("property-value")

        GIVEN_ENVIRONMENT_BUILDER()
            .withProperties(
                listOf(
                    GIVEN_PROPERTY_MODEL,
                    PropertyModel(anotherPropertyName, GIVEN_ENVIRONMENT_NAME, anotherPropertyValue),
                ),
            )
            .build()
            .let(::setOf)
            .let(FileFactory::create)
            .single()
            .shouldMatch {
                withEnvironment(GIVEN_ENVIRONMENT_NAME)
                withFilename(GIVEN_ENVIRONMENT_FILENAME)
                withContent("$GIVEN_PROPERTY_NAME=$GIVEN_PROPERTY_VALUE\n$anotherPropertyName=$anotherPropertyValue")
            }
    }
}
