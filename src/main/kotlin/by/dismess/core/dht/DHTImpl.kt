package by.dismess.core.dht

import by.dismess.core.model.User
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

    override fun saveUser(userId: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(userId.rawID) }

        if (ownerId.rawID inBucket bucket) {
            bucket.idToIP[userId.rawID] = address
            if (bucket.idToIP.size > BUCKET_SIZE) {
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
        } else {
            TODO("Ping all to find the dead")
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
        class NodesComparator : Comparator<Map.Entry<BigInteger, InetSocketAddress>> {
            override fun compare(
                p0: Map.Entry<BigInteger, InetSocketAddress>?,
                p1: Map.Entry<BigInteger, InetSocketAddress>?
            ): Int {
                val firstDist = p0?.key!! xor target.rawID
                val secondDist = p1?.key!! xor target.rawID
                return when {
                    firstDist < secondDist -> 1
                    firstDist > secondDist -> -1
                    else -> 0
                }
            }
        }

        var listOfNodes = getBucket(target).idToIP.entries.toMutableList()

        while (listOfNodes.filter { it.key xor target.rawID <= maxDistance }.count() != count) {
            var tmpList = mutableListOf<MutableMap.MutableEntry<BigInteger, InetSocketAddress>>()
            for (node in listOfNodes) {
                networkService.sendMessage(node.value, TODO("Tag for find request"), TODO("Args"))
                tmpList.addAll(TODO("Response"))
            }
            listOfNodes.addAll(tmpList)
            listOfNodes.sortWith(NodesComparator())
            listOfNodes = listOfNodes.take(count) as MutableList<MutableMap.MutableEntry<BigInteger, InetSocketAddress>>
        }

        var foundedNodes = mutableMapOf<UserID, InetSocketAddress>()
        listOfNodes.map { foundedNodes[TODO("Cast it.key, that has BitInteger type, to UserID")] = it.value }
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }

    override fun find(userId: UserID): InetSocketAddress {
        TODO("Not yet implemented")
    }
}
