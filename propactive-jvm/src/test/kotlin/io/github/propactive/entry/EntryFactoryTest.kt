package io.github.propactive.entry

import io.github.propactive.config.UNSPECIFIED_ENVIRONMENT
import io.github.propactive.entry.EntryFailureReason.MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES
import io.github.propactive.matcher.EntryMatcher.Companion.shouldMatch
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class EntryFactoryTest {
    @Nested
    @TestInstance(PER_CLASS)
    inner class HappyPath {
        @ParameterizedTest
        @CsvSource("value", ":value")
        fun shouldSupportNoKeyEntriesWhenThereIsASingleEntryOnly(entry: String) {
            EntryFactory.create(arrayOf(entry))
                .first()
                .shouldMatch {
                    withKey(UNSPECIFIED_ENVIRONMENT)
                    withValue("value")
                }
        }

        @ParameterizedTest
        @CsvSource(
            "key:value    ",
            "key : value  ",
            "  key : value",
        )
        fun shouldSplitKeyValueForValidEntriesByDelimiter(entry: String) {
            EntryFactory.create(arrayOf(entry))
                .first()
                .let { it.copy(it.key.trim(), it.value.trim()) }
                .shouldMatch {
                    withKey("key")
                    withValue("value")
                }
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    inner class SadPath {
        @Test
        fun shouldThrowIllegalArgumentExceptionWhenNoDelimiterIsProvidedForMultipleEntries() {
            shouldThrow<IllegalArgumentException> { EntryFactory.create(arrayOf("env:entry1", "entry2")) }
                .message shouldBe MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES("entry2")()
        }
    }
}
