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
    private var usersTable = mutableListOf<Bucket>()
    private val ownerId: UserID = TODO()

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private fun saveUser(userId: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(userId.rawID) }
        bucket.idToIP[userId.rawID] = address
        if (ownerId.rawID inBucket bucket && bucket.idToIP.size > BUCKET_SIZE) {
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
        }
    }

    private fun getBucket(userId: UserID): Bucket {
        return usersTable.first { userId.rawID inBucket it }
    }

    private fun findNearestNodes(
        target: UserID,
        count: Int,
        maxDistance: BigInteger
    ): MutableMap<UserID, InetSocketAddress> {
        var listOfNodes = getBucket(target).idToIP.entries.toMutableList()

        do {
            for (node in listOfNodes) {
                networkService.sendMessage(node.value, TODO("Tag for find request"), TODO("Args"))
                saveUser(TODO("UserID from response"), TODO("User address from response"))
                listOfNodes.addAll(TODO("Response"))
            }
            listOfNodes = listOfNodes.filter { it.key xor target.rawID <= maxDistance } as
                    MutableList<MutableMap.MutableEntry<BigInteger, InetSocketAddress>>
        } while (listOfNodes.size < count)

        listOfNodes = listOfNodes.take(count) as MutableList<MutableMap.MutableEntry<BigInteger, InetSocketAddress>>
        var foundedNodes = mutableMapOf<UserID, InetSocketAddress>()
        listOfNodes.map { foundedNodes[TODO("Cast it.key, that has BitInteger type, to UserID")] = it.value }
        return foundedNodes
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }

    override fun find(userId: UserID): InetSocketAddress {
        TODO("Not yet implemented")
    }
}
