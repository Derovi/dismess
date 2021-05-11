package by.dismess.core.dht

import by.dismess.core.model.UserID
import java.net.InetSocketAddress

interface DHT {
    fun initSelf(ownerID: UserID, ownerIP: InetSocketAddress)
    suspend fun store(key: String, data: ByteArray): Boolean
    suspend fun retrieve(key: String): ByteArray?
    suspend fun connectTo(userID: UserID, address: InetSocketAddress)
    suspend fun find(userID: UserID): InetSocketAddress?
    suspend fun isValidLogin(address: InetSocketAddress, login: String): Boolean
}
