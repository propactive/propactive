package io.github.propactive.support.utils

import kotlin.random.Random.Default

fun Default.alphaNumeric(prefix: String? = null, suffix: String? = null, length: Int = 7) =
    (1..length)
        .joinToString("") { "${(('A'..'Z') + ('a'..'z') + ('0'..'9')).random()}" }
        .let { random -> prefix?.let { "$it-$random" } ?: random }
        .let { string -> suffix?.let { "$string-$it" } ?: string }
