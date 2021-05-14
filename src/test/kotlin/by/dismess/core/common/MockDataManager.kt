package by.dismess.core.common

import by.dismess.core.chating.attachments.ImageAttachment
import by.dismess.core.managers.DataManager
import by.dismess.core.utils.UniqID
import by.dismess.core.utils.generateUserID
import java.net.InetSocketAddress
import kotlin.random.Random

class MockDataManager : DataManager {

    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private val id = generateUserID(getRandomString(Random.nextInt(10, 60)))
    private val ip = run {
        var randomIP = Random.nextInt(256).toString()
        repeat(3) {
            randomIP += "." + Random.nextInt(256)
        }
        InetSocketAddress(randomIP, Random.nextInt(1000, 10000))
    }
    override suspend fun getOwnIP(): InetSocketAddress? = ip

    override suspend fun getId(): UniqID = id

    override suspend fun saveLogin(login: String) {}

    override suspend fun getLogin(): String? {
        return null
    }

    override suspend fun saveDisplayName(displayName: String) {}

    override suspend fun getDisplayName(): String? {
        return null
    }

    override suspend fun saveAvatar(avatar: ImageAttachment) {}

    override suspend fun getAvatar(): ImageAttachment? {
        return null
    }

    override suspend fun setOwnIP(ip: InetSocketAddress) {}

    override suspend fun saveLastIP(userID: UniqID, ip: InetSocketAddress) {}

    override suspend fun getLastIP(userID: UniqID): InetSocketAddress? {
        return null
    }
}
