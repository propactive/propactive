package io.github.propactive.type

internal class CLASSTest : TypeTestRunner() {
    override val underTest = CLASS

    override fun validValues(): Array<Any> = arrayOf(
        "io.github.propactive.type.Class", // fully qualified class name
        "com.example.My_Class", // underscores in names
        "com.example.MyClass\$InnerClass", // inner class
        "a.b.c.D", // single-letter class names
        "_MyClass", // leading underscore
        "\$MyClass", // leading dollar sign
    )

    override fun invalidValues(): Array<Any> = arrayOf(
        "", // empty string
        "1", // starts with a number
        "io.github.propactive.type.CLASSTest.", // ends with a dot
        "io.github.propactive.type.CLASSTest..TypeTestRunner", // double dots
        "%io.github.propactive.type.CLASSTest", // starts with a special character
        "com.example.My Class", // spaces in names
        "com.example.МойКласс", // non-ASCII characters
        ".com.example.MyClass", // leading dot
        "com.example.MyClass.", // trailing dot
        "com..example.MyClass", // consecutive dots
        "com.example.-MyClass", // hyphen in name
        "com.example.*", // asterisk in name
        "com/example/MyClass", // slash instead of dot
    )
}
