package by.dismess.core.model

import by.dismess.core.model.attachments.ImageAttachment
import java.net.Inet4Address

class User(val userID: UserID) {
    var displayName: String? = null
    var avatar: ImageAttachment? = null
    val lastIP: Inet4Address? = null
}
