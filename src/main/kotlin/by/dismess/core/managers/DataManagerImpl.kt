package by.dismess.core.managers

import by.dismess.core.model.UserID
import by.dismess.core.model.attachments.ImageAttachment
import by.dismess.core.services.StorageService
import java.net.InetSocketAddress

class DataManagerImpl(
    val storageService: StorageService
) : DataManager {

    override suspend fun saveLogin(login: String): Unit =
        storageService.save(DataManager.Keys.LOGIN, login)

    override suspend fun getLogin(): String? =
        storageService.load(DataManager.Keys.LOGIN)

    override suspend fun saveDisplayName(displayName: String): Unit =
        storageService.save(DataManager.Keys.DISPLAY_NAME, displayName)

    override suspend fun getDisplayName(): String? =
        storageService.load(DataManager.Keys.DISPLAY_NAME)

    override suspend fun saveAvatar(avatar: ImageAttachment): Unit =
        storageService.save(DataManager.Keys.AVATAR, avatar)

    override suspend fun getAvatar(): ImageAttachment? =
        storageService.load(DataManager.Keys.AVATAR)

    override suspend fun saveMyIP(ip: InetSocketAddress): Unit =
        storageService.save(DataManager.Keys.MY_IP, ip)

    override suspend fun getMyIP(): InetSocketAddress? =
        storageService.load(DataManager.Keys.MY_IP)

    override suspend fun saveLastIP(userID: UserID, ip: InetSocketAddress): Unit =
        storageService.save(DataManager.Keys.LAST_IP_PREF + userID, ip)

    override suspend fun getLastIP(userID: UserID): InetSocketAddress? =
        storageService.load(DataManager.Keys.LAST_IP_PREF + userID)
}
