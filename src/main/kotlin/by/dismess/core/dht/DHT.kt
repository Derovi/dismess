package by.dismess.core.dht

import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

interface DHT {
    fun initSelf(ownerID: UniqID, ownerIP: InetSocketAddress)
    suspend fun store(key: String, data: ByteArray): Boolean
    suspend fun retrieve(key: String): ByteArray?
    suspend fun connectTo(userID: UniqID, address: InetSocketAddress)
    suspend fun find(userID: UniqID): InetSocketAddress?
    suspend fun isValidLogin(address: InetSocketAddress, login: String): Boolean
}
