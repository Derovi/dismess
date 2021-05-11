package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID
import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

const val BUCKET_SIZE = 8

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {
    private var usersTable = mutableListOf<Bucket>()
    private val ownerID: UserID = TODO()

    init {
        networkService.registerHandler("DHT/Find") {}
    }

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private fun trySaveUser(userId: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(userId.rawID) }
        bucket.idToIP[userId.rawID] = address
        if (ownerID.rawID inBucket bucket && bucket.idToIP.size > BUCKET_SIZE) {
            val bucketBorderMid = (bucket.border.left + bucket.border.right) / BigInteger.TWO
            val leftHalf = Bucket(BucketBorder(bucket.border.left, bucketBorderMid))
            val rightHalf = Bucket(BucketBorder(leftHalf.border.right, bucket.border.right))
            bucket.idToIP.map {
                when {
                    it.key inBucket leftHalf -> leftHalf.idToIP.put(it.key, it.value)
                    it.key inBucket rightHalf -> rightHalf.idToIP.put(it.key, it.value)
                    else -> Unit
                }
            }
            val bucketInd = usersTable.indexOf(bucket)
            usersTable.add(bucketInd, rightHalf)
            usersTable.add(bucketInd, leftHalf)
            usersTable.remove(bucket)
        } else if (!ownerID.rawID inBucket bucket && bucket.idToIP.size > BUCKET_SIZE) {
            for (user in bucket.idToIP) {
                TODO("Ping and remove dead nods")
            }
        }
    }

    private fun getBucketWithUser(userId: UserID): Bucket {
        return usersTable.first { userId.rawID inBucket it }
    }

    private fun findNearestNodes(
        target: UserID,
        count: Int,
        maxDistance: BigInteger
    ): MutableMap<UserID, InetSocketAddress> {
        TODO()
    }

    fun findUser(target: UserID) {
        var potentialBucket = getBucketWithUser(target)
    }

    override suspend fun find(userId: UserID): InetSocketAddress? {
        TODO("Not yet implemented")
    }

    override suspend fun remember(users: List<Map.Entry<UniqID, InetSocketAddress>>) {
        TODO("Not yet implemented")
    }
}
