package io.github.propactive.matcher

import io.github.propactive.entry.EntryModel
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.should

class EntryMatcher private constructor() : Matcher<EntryModel> {
    private val matchers: MutableMap<Field, Matcher<String>> = mutableMapOf()

    companion object {
        fun EntryModel.shouldMatch(matcher: EntryMatcher.() -> Unit) =
            this should EntryMatcher().apply(matcher)
    }

    internal fun withKey(expected: String) = apply {
        matchers[Field.KEY] = equalityMatcher(expected)
    }

    internal fun withValue(expected: String) = apply {
        matchers[Field.VALUE] = equalityMatcher(expected)
    }

    override fun test(value: EntryModel): MatcherResult = matchers
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

    private enum class Field(val extractor: (EntryModel) -> String) {
        KEY(EntryModel::key),
        VALUE(EntryModel::value),
    }
}
