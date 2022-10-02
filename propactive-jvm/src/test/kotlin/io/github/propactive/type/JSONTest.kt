package io.github.propactive.type

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal class JSONTest : TypeTestRunner() {
    override val underTest = JSON

    override fun validValues(): Array<Any> = arrayOf(
        """{}""",
        """{ "foo": [] }""",
        """{ "foo": "bar", "baz": 1 }""",
        """{ "foo": [{ "bar": "baz" }, { "qux": "quux" }] }""",
        """{ "foo": { "bar": 1 }, "baz": { "qux": 2 } }""",
        JsonObject(mapOf()),
        JsonObject(mapOf("foo" to JsonPrimitive("bar"))),
        JsonObject(mapOf("foo" to JsonArray(listOf(JsonPrimitive(1))))),
        JsonObject(mapOf("foo" to JsonObject(mapOf("foo" to JsonPrimitive("bar"))))),
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "", // Empty strings are not accepted
        "invalid", // invalid value
        Unit, // Unit value
        // Randomly malformed Json values
        """{ foo: [] }""",
        """{ "foo": "bar" "baz": 1 }""",
        """{ "foo": [{ "bar": "baz" }, { "qux": }] }""",
        """{ "foo": { "bar": 1 }, "baz": { "qux": 2 } """,
    )
}
