package by.dismess.core.dht

import by.dismess.core.model.UserID
import java.net.InetSocketAddress

interface DHT {
    suspend fun store(key: String, data: ByteArray)
    suspend fun retrieve(key: String): ByteArray
    suspend fun find(userID: UserID): InetSocketAddress?
}
