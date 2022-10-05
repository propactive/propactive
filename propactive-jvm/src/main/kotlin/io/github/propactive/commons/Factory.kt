package io.github.propactive.commons

interface Factory<I,O> {
    fun create(input: I): O
}
