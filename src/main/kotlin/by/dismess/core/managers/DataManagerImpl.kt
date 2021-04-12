package by.dismess.core.managers

import by.dismess.core.model.attachments.ImageAttachment
import by.dismess.core.services.StorageService
import java.net.Inet4Address

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

    override suspend fun saveLastIP(ip: Inet4Address): Unit =
        storageService.save(DataManager.Keys.LAST_IP, ip)

    override suspend fun getLastIP(): Inet4Address =
        storageService.load(DataManager.Keys.LAST_IP)
}
