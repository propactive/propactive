package io.github.propactive.matcher

import io.github.propactive.environment.EnvironmentModel
import io.github.propactive.property.PropertyModel
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.invokeMatcher

class EnvironmentMatcher private constructor() : Matcher<EnvironmentModel> {
    private val matchers: MutableMap<Field, Matcher<Any>> = mutableMapOf()

    internal fun withName(expected: String) = apply {
        matchers[Field.NAME] = equalityMatcher(expected)
    }

    internal fun withFilename(expected: String) = apply {
        matchers[Field.FILENAME] = equalityMatcher(expected)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun withProperties(vararg expected: PropertyMatcher) = apply {
        matchers[Field.PROPERTIES] = allPropertiesAssertedMatcher(*expected) as Matcher<Any>
    }

    @Suppress("UNCHECKED_CAST")
    internal fun withoutProperties() = apply {
        matchers[Field.PROPERTIES] = beEmpty<PropertyModel>() as Matcher<Any>
    }

    override fun test(value: EnvironmentModel): MatcherResult = matchers
        // All Configuration fields should be matched...
        .apply { keys.shouldContainAll(Field.values().toSet()) }
        .map { (field, matcher) -> matcher.test(field.extractor(value)) }
        .toFinalResult()

    private enum class Field(val extractor: (EnvironmentModel) -> Any) {
        NAME(EnvironmentModel::name),
        FILENAME(EnvironmentModel::filename),
        PROPERTIES(EnvironmentModel::properties),
    }

    companion object {
        fun EnvironmentModel.shouldMatch(matcher: EnvironmentMatcher.() -> Unit) =
            invokeMatcher(this, EnvironmentMatcher().apply(matcher))

        private fun List<MatcherResult>.toFinalResult(): MatcherResult {
            val failedResults = this.filterNot { it.passed() }
            val passedResults = this.filter { it.passed() }

            return MatcherResult(
                failedResults.isEmpty(),
                { failedResults.joinToString("\n") { it.failureMessage() } },
                { passedResults.joinToString(" or \n") { it.negatedFailureMessage() } },
            )
        }

        /**
         * Our properties are not in a predictable order, so we will need to iterate our matchers across all the
         * actual properties to assert if a matcher matches one of the given properties. We then need to assert
         * the number of properties is equal to the number of matchers to ensure all properties were asserted on.
         *
         * This function will produce such a matcher.
         */
        private fun allPropertiesAssertedMatcher(vararg propertyMatchers: PropertyMatcher) =
            object : Matcher<Set<PropertyModel>> {
                override fun test(value: Set<PropertyModel>) = value
                    .takeUnless { it.isEmpty() }
                    ?.let { properties ->
                        // Used to track which properties have passing matchers
                        data class PropertyToPassingMatcherResult(
                            val property: PropertyModel,
                            val matcher: PropertyMatcher,
                        )

                        val passedResults = propertyMatchers.mapNotNull { matcher ->
                            properties
                                .find { property -> matcher.test(property).passed() }
                                ?.let { property -> PropertyToPassingMatcherResult(property, matcher) }
                        }

                        val failedResults = passedResults
                            .fold(propertyMatchers.toSet()) { matchers, passed -> matchers.minus(passed.matcher) }
                            .map { matcher ->
                                MatcherResult(
                                    false,
                                    { "Given $matcher\ndidn't find any matching property in:\n$value\n" },
                                    { "Given matcher:\n$matcher\nfound a matching property in:\n$value\n" },
                                )
                            }

                        val givenPropertiesShouldPassMatcherTests = MatcherResult(
                            failedResults.isEmpty(),
                            {
                                failedResults
                                    .joinToString("\n", transform = MatcherResult::failureMessage)
                            },
                            {
                                passedResults
                                    .map { it.matcher.test(it.property) }
                                    .joinToString(" or \n", transform = MatcherResult::negatedFailureMessage)
                            },
                        )

                        val givenMatchersShouldBeTheSameSizeAsProperties =
                            with(propertyMatchers.size to properties.size) {
                                MatcherResult(
                                    first == second,
                                    { "Was expecting to find $second property matcher(s) only for full assertion coverage, but found $first property matcher(s)..." },
                                    { "unused" },
                                )
                            }

                        listOf(
                            givenPropertiesShouldPassMatcherTests,
                            givenMatchersShouldBeTheSameSizeAsProperties,
                        ).toFinalResult()
                    }
                    ?: MatcherResult(
                        false,
                        { "No properties were found..." },
                        { "Properties were found..." },
                    )
            }
    }
}
