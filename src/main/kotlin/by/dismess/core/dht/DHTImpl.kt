package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

class DHTImpl : DHT {
    override suspend fun store(key: String, data: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun retrieve(key: String): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun find(userId: UserID): InetSocketAddress? {
        TODO("Not yet implemented")
    }

    override suspend fun remember(users: List<Map.Entry<UniqID, InetSocketAddress>>) {
        TODO("Not yet implemented")
    }
}
