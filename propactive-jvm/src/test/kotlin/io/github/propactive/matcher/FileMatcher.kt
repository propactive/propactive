package io.github.propactive.matcher

import io.github.propactive.file.FileModel
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.equalityMatcher
import io.kotest.matchers.should

class FileMatcher private constructor() : Matcher<FileModel> {
    private val matchers: MutableMap<Field, Matcher<String>> = mutableMapOf()

    companion object {
        fun FileModel.shouldMatch(matcher: FileMatcher.() -> Unit) =
            this should FileMatcher().apply(matcher)
    }

    internal fun withEnvironment(expected: String) = apply {
        matchers[Field.ENVIRONMENT] = equalityMatcher(expected)
    }

    internal fun withFilename(expected: String) = apply {
        matchers[Field.FILENAME] = equalityMatcher(expected)
    }

    internal fun withContent(expected: String) = apply {
        matchers[Field.CONTENT] = equalityMatcher(expected)
    }

    override fun test(value: FileModel): MatcherResult = matchers
        // All Configuration fields should be matched...
        .apply { keys.shouldContainAll(FileMatcher.Field.values().toSet()) }
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

    enum class Field(val extractor: (FileModel) -> String) {
        ENVIRONMENT(FileModel::environment),
        FILENAME(FileModel::filename),
        CONTENT(FileModel::content),
    }
}
