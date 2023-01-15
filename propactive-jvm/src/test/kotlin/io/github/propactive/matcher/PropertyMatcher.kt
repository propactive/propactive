package io.github.propactive.matcher

import io.github.propactive.property.PropertyModel
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.should
import java.util.StringJoiner

class PropertyMatcher private constructor() : Matcher<PropertyModel> {
    private val matchers: MutableMap<Field, Matcher<String>> = mutableMapOf()
    private val values: MutableMap<Field, String> = mutableMapOf()

    companion object {
        fun propertyMatcher() = PropertyMatcher()

        fun PropertyModel.shouldMatchProperty(matcher: PropertyMatcher.() -> Unit) =
            this should propertyMatcher().apply(matcher)
    }

    internal fun withName(expected: String) = apply {
        values[Field.NAME] = expected
        matchers[Field.NAME] = equalityMatcher(values[Field.NAME])
    }

    internal fun withEnvironment(expected: String) = apply {
        values[Field.ENVIRONMENT] = expected
        matchers[Field.ENVIRONMENT] = equalityMatcher(values[Field.ENVIRONMENT])
    }

    internal fun withValue(expected: String) = apply {
        values[Field.VALUE] = expected
        matchers[Field.VALUE] = equalityMatcher(values[Field.VALUE])
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

    override fun toString() = StringJoiner("\n  ")
        .add("PropertyMatcher:")
        .apply { values.forEach { (field, value) -> add("${field.name} shouldBe \"$value\"") } }
        .toString()
}
