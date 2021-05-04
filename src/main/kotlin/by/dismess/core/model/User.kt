package by.dismess.core.model

import by.dismess.core.model.attachments.ImageAttachment
import java.net.InetSocketAddress

data class User(
    val login: String,
    var displayName: String? = null,
    var avatar: ImageAttachment? = null,
    var lastIP: InetSocketAddress? = null
)
