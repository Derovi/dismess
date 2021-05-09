package by.dismess.core.managers.impl

import by.dismess.core.dht.DHT
import by.dismess.core.klaxon
import by.dismess.core.managers.DataManager
import by.dismess.core.managers.UserManager
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.chating.attachments.ImageAttachment
import by.dismess.core.network.MessageType
import by.dismess.core.network.NetworkMessage
import by.dismess.core.services.NetworkService

class UserManagerImpl(
    val dht: DHT,
    val networkService: NetworkService,
    val dataManager: DataManager
) : UserManager {

    init {
        networkService.registerPost("user/ping") {}
        networkService.registerGet("user/retrieve") {
            result(
                User(
                    dataManager.getLogin()!!,
                    dataManager.getDisplayName(),
                    dataManager.getAvatar()
                )
            )
        }
    }

    override suspend fun sendPost(target: UserID, tag: String, data: Any, timeout: Long): Boolean =
        sendPost(target, tag, klaxon.toJsonString(data), timeout)

    override suspend fun sendPost(target: UserID, tag: String, timeout: Long): Boolean =
        sendRequest(target, NetworkMessage(MessageType.POST, tag), timeout) != null

    override suspend fun sendPost(target: UserID, tag: String, data: String, timeout: Long): Boolean =
        sendRequest(target, NetworkMessage(MessageType.POST, tag, data), timeout) != null

    override suspend fun sendGet(target: UserID, tag: String, data: Any, timeout: Long): String? =
        sendGet(target, tag, klaxon.toJsonString(data), timeout)

    override suspend fun sendGet(target: UserID, tag: String, timeout: Long): String? =
        sendRequest(target, NetworkMessage(MessageType.GET, tag), timeout)?.data

    override suspend fun sendGet(target: UserID, tag: String, data: String, timeout: Long): String? =
        sendRequest(target, NetworkMessage(MessageType.GET, tag, data), timeout)?.data

    private suspend fun sendRequest(
        target: UserID,
        message: NetworkMessage,
        timeout: Long = 1000
    ): NetworkMessage? {
        val savedIP = dataManager.getLastIP(target)
        if (savedIP != null) {
            val result = networkService.sendRequest(savedIP, message, timeout)
            if (result != null) {
                return result
            }
        }
        val newAddress = dht.find(target) // try to find new address
        if (newAddress == savedIP) {
            return null
        }
        dataManager.saveLastIP(target, newAddress)
        return networkService.sendRequest(newAddress, message, timeout)
    }

    override suspend fun isOnline(userId: UserID): Boolean =
        sendPost(userId, "user/ping", userId)

    override suspend fun retrieveUser(userId: UserID): User? {
        val user = sendGet(userId, "user/retrieve")?.run { klaxon.parse<User>(this) }
        user?.lastIP = dataManager.getLastIP(userId)
        return user
    }

    override suspend fun retrieveAvatar(userId: UserID): ImageAttachment? =
        sendGet(userId, "user/avatar")?.run { klaxon.parse<ImageAttachment>(this) }
}