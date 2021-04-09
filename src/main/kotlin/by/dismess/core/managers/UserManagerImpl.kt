package by.dismess.core.managers

import by.dismess.core.dht.DHT
import by.dismess.core.model.User
import by.dismess.core.model.UserID
import by.dismess.core.network.NetworkMessage
import by.dismess.core.services.NetworkService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class UserManagerImpl(
    val dht: DHT,
    val networkService: NetworkService
) : UserManager {
    override suspend fun isOnline(userId: UserID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun sendNetworkMessage(address: InetSocketAddress, data: ByteArray) {
        val tag = NetworkService.randomTag()
        networkService.sendMessage(address, tag, data)
        GlobalScope
    }

    override suspend fun retrieveUser(userId: UserID): User {
        TODO("Not yet implemented")
    }

    override suspend fun retrieveUserNoAvatar(userId: UserID): User {
        TODO("Not yet implemented")
    }
}
