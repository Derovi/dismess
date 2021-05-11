package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

interface DHT {
    suspend fun store(key: String, data: ByteArray): Boolean
    suspend fun retrieve(key: String): ByteArray?
    suspend fun find(userId: UserID): InetSocketAddress?
    suspend fun remember(users: List<Map.Entry<UniqID, InetSocketAddress>>)
}
