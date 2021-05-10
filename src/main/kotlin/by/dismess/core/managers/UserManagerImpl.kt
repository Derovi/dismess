package by.dismess.core.managers

import by.dismess.core.dht.DHT
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.network.NetworkMessage
import by.dismess.core.services.NetworkService

class UserManagerImpl(
    val dht: DHT,
    val networkService: NetworkService,
    val dataManager: DataManager
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
        userID: UserID,
        message: NetworkMessage,
        userStatusChanged: ((UserStatus) -> Unit)?
    ): Boolean {
        TODO("Not implemented on this branch")
//        dataManager.getLastIP(userID) ?.also { savedIP ->
//            if (networkService.sendPost(savedIP, message)) {
//                return true
//            }
//        }
//        userStatusChanged?.invoke(UserStatus.RETRIEVING_IP)
//        // online of successfully responsed
//        val newAddress = dht.find(userID) // try to find new address
//        dataManager.saveLastIP(userID, newAddress)
//        return networkService.sendPost(newAddress, message)
    }

    override suspend fun isOnline(userId: UserID): Boolean =
        TODO("Not implemented on this branch")
    // sendNetworkMessage(userId, NetworkMessage("null", ""))

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUserNoAvatar(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
