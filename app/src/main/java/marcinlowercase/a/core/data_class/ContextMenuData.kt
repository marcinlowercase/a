package marcinlowercase.a.core.data_class

import marcinlowercase.a.core.enum_class.ContextMenuType

//data class ContextMenuData(
//    val url: String,
//    val type: Int
//)

data class ContextMenuData(
    val type: ContextMenuType,
    val linkUrl: String? = null, // The target (e.g. google.com)
    val srcUrl: String? = null   // The media file (e.g. image.png, video.mp4)
)