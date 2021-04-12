package by.dismess.core.managers

import by.dismess.core.model.attachments.ImageAttachment
import java.net.Inet4Address

interface DataManager {
    object Keys {
        const val LOGIN = "info.login"
        const val DISPLAY_NAME = "info.display_name"
        const val AVATAR = "info.avatar"
        const val LAST_IP = "info.last_ip"
    }

    suspend fun saveLogin(login: String)
    suspend fun getLogin(): String?

    suspend fun saveDisplayName(displayName: String)
    suspend fun getDisplayName(): String?

    suspend fun saveAvatar(avatar: ImageAttachment)
    suspend fun getAvatar(): ImageAttachment?

    suspend fun saveLastIP(ip: Inet4Address)
    suspend fun getLastIP(): Inet4Address
}
