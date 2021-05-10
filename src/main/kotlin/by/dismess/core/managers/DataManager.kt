package by.dismess.core.managers

import by.dismess.core.model.UserID
import by.dismess.core.model.attachments.ImageAttachment
import java.net.InetSocketAddress

interface DataManager {
    object Keys {
        const val LOGIN = "info.login"
        const val DISPLAY_NAME = "info.display_name"
        const val AVATAR = "info.avatar"
        const val MY_IP = "info.my_ip"
        const val LAST_IP_PREF = "last_ip."
    }

    suspend fun saveLogin(login: String)
    suspend fun getLogin(): String?

    suspend fun saveDisplayName(displayName: String)
    suspend fun getDisplayName(): String?

    suspend fun saveAvatar(avatar: ImageAttachment)
    suspend fun getAvatar(): ImageAttachment?

    suspend fun saveMyIP(ip: InetSocketAddress)
    suspend fun getMyIP(): InetSocketAddress?

    suspend fun saveLastIP(userID: UserID, ip: InetSocketAddress)
    suspend fun getLastIP(userID: UserID): InetSocketAddress?
}
