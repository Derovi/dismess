package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import java.net.InetSocketAddress
import java.util.TreeMap

const val BUCKET_SIZE = 8
const val PING_TIMER = 10 * 60000
const val MAX_FIND_ITERATIONS = 100

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {
    private var usersTable = mutableListOf<Bucket>()
    private val ownerID: UserID = TODO()

    init {
        networkService.registerGet("DHT/Find") { message ->
            val data = message.data ?: return@registerGet
            val request = klaxon.parse<FindRequest>(data) ?: return@registerGet
            val bucket = getUserBucket(request.targetUser)
            val response = klaxon.toJsonString(bucket)
            trySaveUser(request.sender, message.sender)
            result(response)
        }
        networkService.registerPost("DHT/Ping") {}
    }

    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    private suspend fun pingBucket(bucket: Bucket) {
        for (user in bucket.idToIP) {
            if (!networkService.sendPost(user.value, "DHT/Ping")) {
                bucket.idToIP.remove(user)
            }
        }
        bucket.lastPingTime = System.currentTimeMillis()
    }

    private suspend fun trySaveUser(userId: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(userId.rawID) }

        if (bucket.idToIP.containsKey(userId)) {
            return
        }

        if (System.currentTimeMillis() - bucket.lastPingTime > PING_TIMER) {
            pingBucket(bucket)
        }

        if (ownerID inBucket bucket) {
            bucket.idToIP[userId] = address
            if (bucket.idToIP.size > BUCKET_SIZE) {
                val splitedBuckets = bucket.split()
                val bucketInd = usersTable.indexOf(bucket)
                usersTable.add(bucketInd, splitedBuckets.second)
                usersTable.add(bucketInd, splitedBuckets.first)
                usersTable.remove(bucket)
            }
        } else if (bucket.idToIP.size < BUCKET_SIZE) {
            bucket.idToIP[userId] = address
        }
    }

    private fun getUserBucket(userId: UserID): Bucket = usersTable.first { userId inBucket it }

    private suspend fun findNearestNodes(target: UserID, count: Int): MutableMap<UserID, InetSocketAddress> {
        val nearestUsers = getUserBucket(target).idToIP
        var findIterations = 0

        val mapComparator = kotlin.Comparator { firstUser: UserID, secondUser: UserID ->
            when {
                firstUser distanceTo target < secondUser distanceTo target -> return@Comparator -1
                firstUser distanceTo target > secondUser distanceTo target -> return@Comparator 1
                else -> return@Comparator 0
            }
        }

        while (findIterations < MAX_FIND_ITERATIONS) {
            val buffer = TreeMap<UserID, InetSocketAddress>(mapComparator)
            for (user in nearestUsers) {
                val request = FindRequest(target, ownerID)
                val response = networkService.sendGet(user.value, "DHT/Find", request) ?: continue
                val responseBucket = klaxon.parse<Bucket>(response) ?: continue
                buffer.putAll(responseBucket.idToIP)
            }
            nearestUsers.clear()
            var counter = 0
            buffer.forEach {
                trySaveUser(it.key, it.value)
                if (counter < count) {
                    nearestUsers[it.key] = it.value
                    ++counter
                }
            }
            ++findIterations
        }

        return nearestUsers
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not implemented yet")
    }

    override suspend fun find(userID: UserID): InetSocketAddress {
        TODO()
    }
}
