package io.github.propactive.entry

import io.github.propactive.config.KEY_VALUE_DELIMITER

object EntryFailureReason {
    // FACTORY FAILURES

    internal val MISSING_KEY_VALUE_DELIMITER_FOR_A_MULTI_ENTRIES = { entry: String ->
        {
            "Expected a delimiter ($KEY_VALUE_DELIMITER) between the key and value entry, " +
                "example: \"key: value\" as multiple entries were provided. Instead received entry was: \"$entry\""
        }
    }
}
