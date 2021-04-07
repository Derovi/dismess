package by.dismess.core.model

import by.dismess.core.model.attachments.ImageAttachment
import java.net.Inet4Address

data class User(
    val login: String,
    var displayName: String? = null,
    var avatar: ImageAttachment? = null,
    var lastIP: Inet4Address? = null
)
