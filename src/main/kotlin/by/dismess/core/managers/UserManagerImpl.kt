package by.dismess.core.managers

import by.dismess.core.dht.DHT
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.network.NetworkMessage
import by.dismess.core.services.NetworkService
import java.net.InetSocketAddress

class UserManagerImpl(
    val dht: DHT,
    val networkService: NetworkService
) : UserManager {
    companion object {
        const val TIMEOUT = 1000
    }

    /**
     * Send message to user
     * 1) Returns true if successfully, false if not
     * 2) userStatusChanged callback allows to catch request to dht
     */
    override suspend fun sendNetworkMessage(
            userId: UserID,
            message: NetworkMessage, userStatusChanged: ((UserStatus) -> Unit)?
    ): Boolean {
        val savedAddress: InetSocketAddress = InetSocketAddress(33)// TODO get saved address
        if (networkService.sendMessage(savedAddress, message)) {
            return true
        }
        userStatusChanged?.invoke(UserStatus.RETRIEVING_IP)
        // online of successfully responsed
        val newAddress = dht.find(userId) // try to find new address
        // TODO store new address
        return networkService.sendMessage(newAddress, message)
    }

    override suspend fun isOnline(userId: UserID): Boolean =
            sendNetworkMessage(userId, NetworkMessage("null", ""))

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUserNoAvatar(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
