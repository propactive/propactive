package io.github.propactive.matcher

import io.github.propactive.plugin.Configuration
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.should

class ConfigurationMatcher private constructor() : Matcher<Configuration> {
    private val matchers: MutableMap<Field, Matcher<String>> = mutableMapOf()

    companion object {
        fun Configuration.shouldMatch(matcher: ConfigurationMatcher.() -> Unit) =
            this should ConfigurationMatcher().apply(matcher)
    }

    internal fun withImplementationClass(expected: String) = apply {
        matchers[Field.IMPLEMENTATION_CLASS] = equalityMatcher(expected)
    }

    internal fun withDestination(expected: String) = apply {
        matchers[Field.DESTINATION] = equalityMatcher(expected)
    }

    internal fun withFilenameOverride(expected: String) = apply {
        matchers[Field.FILENAME_OVERRIDE] = equalityMatcher(expected)
    }

    internal fun withEnvironments(expected: String) = apply {
        matchers[Field.ENVIRONMENTS] = equalityMatcher(expected)
    }

    internal fun withImplementationClassCompileDependency(expected: String) = apply {
        matchers[Field.IMPLEMENTATION_CLASS_COMPILE_DEPENDENCY] = equalityMatcher(expected)
    }

    override fun test(value: Configuration): MatcherResult = matchers
        // All Configuration fields should be matched...
        .apply { keys.shouldContainAll(Field.entries.toSet()) }
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

    private enum class Field(val extractor: (Configuration) -> String) {
        IMPLEMENTATION_CLASS(Configuration::implementationClass),
        DESTINATION(Configuration::destination),
        ENVIRONMENTS(Configuration::environments),
        FILENAME_OVERRIDE(Configuration::filenameOverride),
        IMPLEMENTATION_CLASS_COMPILE_DEPENDENCY(Configuration::classCompileDependency),
    }
}
