package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import java.math.BigInteger
import java.net.InetSocketAddress

const val BUCKET_SIZE = 8

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {
    private var table = mutableListOf<Bucket>()
    private val ownerId: UserID = TODO()

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private fun getBucket(key: String): Bucket {
        val idFromKey: BigInteger = TODO("Some hash of key")
        return table.first { it.border.contains(idFromKey) }
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
