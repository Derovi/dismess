package by.dismess.core.dht

import by.dismess.core.klaxon
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
    private val ownerID: UserID = TODO()

    init {
        networkService.registerGet("DHT/Find") { message ->
            val request = klaxon.parse<FindRequest>(message.data!!)
            tryToSaveUser(request!!.sender, message.sender)
            val bucket = getBucketWithUser(request.targetUser)
            val response = klaxon.toJsonString(bucket)
            result(response)
        }
        networkService.registerPost("DHT/Ping") {}
    }

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private suspend fun tryToSaveUser(userId: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(userId.rawID) }
        bucket.idToIP[userId] = address
        if (ownerID inBucket bucket && bucket.idToIP.size > BUCKET_SIZE) {
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
        } else if (!(ownerID inBucket bucket) && bucket.idToIP.size > BUCKET_SIZE) {
            for (user in bucket.idToIP) {
                if (!networkService.sendPost(user.value, "DHT/Ping")) {
                    bucket.idToIP.remove(user)
                }
            }
            if (bucket.idToIP.size > BUCKET_SIZE) {
                TODO("Remove old nodes")
            }
        }
    }

    private fun getBucketWithUser(userId: UserID): Bucket {
        return usersTable.first { userId inBucket it }
    }

    private suspend fun findNearestNodes(target: UserID): MutableMap<UserID, InetSocketAddress> {
        var distance = ownerID distanceTo target
        var usersBuffer = getBucketWithUser(target).idToIP
        var findIterations = 0

        while (findIterations < 100 || usersBuffer.size > 1) {
            val newBuffer = mutableMapOf<UserID, InetSocketAddress>()
            for (user in usersBuffer) {
                val request = FindRequest(target, ownerID)
                val response = networkService.sendGet(user.value, "DHT/Find", request) ?: continue
                val userBucket = klaxon.parse<Bucket>(response)
                newBuffer.putAll(userBucket!!.idToIP)
            }
            var newDistance = distance
            newBuffer.filter {
                tryToSaveUser(it.key, it.value)
                val userDistance = it.key distanceTo target
                newDistance = newDistance.max(userDistance)
                userDistance <= distance
            }
            distance = newDistance
            usersBuffer = newBuffer
            ++findIterations
        }

        return usersBuffer
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }

    override suspend fun find(userID: UserID): InetSocketAddress {
        TODO()
    }
}
