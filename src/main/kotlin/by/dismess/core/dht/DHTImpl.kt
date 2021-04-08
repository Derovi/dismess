package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import java.math.BigInteger
import java.net.InetSocketAddress
import java.util.LinkedList

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {

    private val bucketSize: Int = 8
    private var table: LinkedList<Bucket> = LinkedList()
    private val ownerId: UserID = TODO()

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private fun getBucket(key: String): Bucket {
        val idFromKey: BigInteger = TODO("Some hash of key")
        return table.filter { it.border.contains(idFromKey) }[0]
    }

    private fun findNearestNodes(
        target: BigInteger,
        count: Int,
        maxDistance: BigInteger
    ): Map<UserID, InetSocketAddress> {
        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }

    override fun find(userId: UserID): InetSocketAddress {
        TODO("Not yet implemented")
    }
}
