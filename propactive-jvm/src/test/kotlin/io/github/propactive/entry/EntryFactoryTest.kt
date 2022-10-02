package io.github.propactive.entry

import io.github.propactive.config.UNSPECIFIED_ENVIRONMENT
import io.github.propactive.entry.EntryFailureReason.MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES
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
                .first().apply {
                    key shouldBe UNSPECIFIED_ENVIRONMENT
                    value shouldBe "value"
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
                .first().apply {
                    key.trim() shouldBe "key"
                    value.trim() shouldBe "value"
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
