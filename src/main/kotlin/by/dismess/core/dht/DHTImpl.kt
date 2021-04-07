package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import java.math.BigInteger
import java.util.LinkedList

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {

    private val bucketSize: Int = 8
    private var table: LinkedList<Bucket> = LinkedList()
    private val ownerId: UserID = TODO()

    override fun store(key: String, data: ByteArray) {
        val idFromKey: BigInteger = TODO("Some hash of key")

        val bucket = table.filter { it.border.contains(idFromKey) }[0]
        if (bucket.border.contains(ownerId.rawID)) {
            bucket.data.add(Pair(idFromKey, data))
            if (bucket.data.size > bucketSize) {
                val middle = (bucket.border.left + bucket.border.right) / BigInteger.TWO
                val firstBucket = Bucket(BucketBorder(bucket.border.left, middle))
                val secondBucket = Bucket(BucketBorder(middle, bucket.border.right))
                bucket.data.filter { firstBucket.border.contains(TODO("Hash from key")) }
                    .map { firstBucket.data.add(it) }
                bucket.data.filter { secondBucket.border.contains(TODO("Hash from key")) }
                    .map { secondBucket.data.add(it) }
            }
        } else {
            if (bucket.data.size < bucketSize) {
                bucket.data.add(Pair(idFromKey, data))
            } else {
                TODO("Ping all users from bucket to find the dead")
            }
        }
    }

    override fun getKeyBucket(key: String): Bucket {
        val idFromKey: BigInteger = TODO("Some hash of key")
        return table.filter { it.border.contains(idFromKey) }[0]
    }

    override fun findNodes(target: String, count: Int, maxDistance: BigInteger): Bucket {
        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }
}
