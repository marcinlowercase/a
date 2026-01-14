package marcinlowercase.a.core.enum_class

import kotlinx.serialization.Serializable

@Serializable // Marks this class as serializable
enum class TabState {
    ACTIVE,      // The tab currently visible to the user
    BACKGROUND,  // A tab that is loaded but not visible
    FROZEN       // A tab that needs to be reloaded when opened
}

