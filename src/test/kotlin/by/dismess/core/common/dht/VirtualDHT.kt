package by.dismess.core.common.dht

import by.dismess.core.dht.DHT
import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

class VirtualDHT(val common: VirtualDHTCommon) : DHT {
    override fun initSelf(ownerID: UniqID, ownerIP: InetSocketAddress) {
        common.users[ownerID] = ownerIP
    }

    override suspend fun store(key: String, data: ByteArray): Boolean {
        common.storage[key] = data
        return true
    }

    override suspend fun retrieve(key: String): ByteArray? = common.storage[key]
    override suspend fun connectTo(userID: UniqID, address: InetSocketAddress) {}

    override suspend fun find(userID: UniqID): InetSocketAddress? {
        println("find: $userID")
        println(common.users)
        return common.users[userID]
    }

    override suspend fun isValidLogin(address: InetSocketAddress, login: String): Boolean {
        return true
    }
}
