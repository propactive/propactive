package io.github.propactive.support.utils

import kotlin.random.Random.Default

fun Default.alphaNumeric(length: Int = 7) =
    (1..length).joinToString("") { "${(('A'..'Z') + ('a'..'z') + ('0'..'9')).random()}" }
