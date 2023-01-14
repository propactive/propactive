package io.github.propactive.matcher

import io.github.propactive.property.PropertyModel
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.should

class PropertyMatcherBuilder private constructor(): Matcher<PropertyModel> {
    private val matchers: MutableMap<Field, Matcher<String>> = mutableMapOf()

    companion object {
        fun PropertyModel.shouldMatchProperty(matcher: PropertyMatcherBuilder.() -> Unit) =
            this should PropertyMatcherBuilder().apply(matcher)
    }

    internal fun withName(expected: String) = apply {
        matchers[Field.NAME] = equalityMatcher(expected)
    }

    internal fun withEnvironment(expected: String) = apply {
        matchers[Field.ENVIRONMENT] = equalityMatcher(expected)
    }

    internal fun withValue(expected: String) = apply {
        matchers[Field.VALUE] = equalityMatcher(expected)
    }

    override fun test(value: PropertyModel): MatcherResult = matchers
        // All Configuration fields should be matched...
        .apply { keys.shouldContainAll(Field.values().toSet()) }
        .map { (field, matcher) -> matcher.test(field.extractor(value)) }
        .toFinalResult()

    private fun List<MatcherResult>.toFinalResult(): MatcherResult {
        val failedResults = this.filterNot { it.passed() }
        val passedResults = this.filter { it.passed() }

        return MatcherResult(
            failedResults.isEmpty(),
            { failedResults.joinToString("\n") { it.failureMessage() } },
            { passedResults.joinToString(" or \n") { it.negatedFailureMessage() } },
        )
    }

    private enum class Field(val extractor: (PropertyModel) -> String) {
        NAME(PropertyModel::name),
        ENVIRONMENT(PropertyModel::environment),
        VALUE(PropertyModel::value),
    }
}
