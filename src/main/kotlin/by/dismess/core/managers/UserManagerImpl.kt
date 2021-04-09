package by.dismess.core.managers

import by.dismess.core.dht.DHT
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.network.NetworkMessage
import by.dismess.core.services.NetworkService

class UserManagerImpl(
    val dht: DHT,
    val networkService: NetworkService
) : UserManager {
    companion object {
        const val TIMEOUT = 1000
    }

    override suspend fun sendNetworkMessage(networkMessage: NetworkMessage, userStatusChanged: ((UserStatus) -> Unit)?) {

    }

    override suspend fun isOnline(userId: UserID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUserNoAvatar(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
