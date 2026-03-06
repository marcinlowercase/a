package marcinlowercase.a.core.data_class

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val name: String,
)
