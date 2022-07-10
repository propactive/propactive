package propactive.property

data class PropertyModel(
    val name: String,
    val environment: String,
    val value: String,
) {
    override fun hashCode() = name.hashCode() + environment.hashCode()
    override fun equals(other: Any?) = when {
        this === other                -> true
        javaClass != other?.javaClass -> false
        else                          -> (name == (other as PropertyModel).name) && (environment == other.environment)
    }
}