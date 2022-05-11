package propactive.environment

import propactive.property.PropertyModel

data class EnvironmentModel(
    val name: String,
    val filename: String,
    val properties: Set<PropertyModel>
) {
    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?) = when {
        this === other                -> true
        javaClass != other?.javaClass -> false
        else                          -> (name == (other as EnvironmentModel).name)
    }
}
