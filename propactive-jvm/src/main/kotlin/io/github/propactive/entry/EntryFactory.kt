package io.github.propactive.entry

import io.github.propactive.config.KEY_VALUE_DELIMITER
import io.github.propactive.config.UNSPECIFIED_ENVIRONMENT
import io.github.propactive.entry.EntryFailureReason.MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES

object EntryFactory {
    fun create(entries: Array<String>) = entries
        .apply {
            if (size > 1) onEach { entry ->
                require(entry.count { it == KEY_VALUE_DELIMITER } > 0,
                    MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES(entry))
            }
        }
        .map { entry ->
            entry
                .split(KEY_VALUE_DELIMITER, limit = 2)
                .takeUnless { it.size == 1 }
                ?.let { (key, value) -> EntryModel(key, value) }
                ?: EntryModel(UNSPECIFIED_ENVIRONMENT, entry)
        }
}