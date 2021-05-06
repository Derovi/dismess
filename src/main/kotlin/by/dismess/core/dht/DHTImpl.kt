package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.generateUserID
import java.math.BigInteger
import java.net.InetSocketAddress
import java.util.TreeMap

const val BUCKET_SIZE = 8
const val PING_TIMER = 10 * 60000
const val MAX_FIND_ITERATIONS = 100
const val STORE_COPIES_COUNT = 10

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {
    private var usersTable = mutableListOf<Bucket>()
    private var ownerID: UserID
        get() {
            return ownerID
        }
        set(value) {
            ownerID = value
        }
    private var ownerIP: InetSocketAddress = TODO()

    init {
        val bucket = Bucket(BucketBorder(BigInteger.ONE, BigInteger.TWO.pow(160)))
        bucket.idToIP[ownerID] = ownerIP
        registerGetHandlers()
        registerPostHandlers()
    }

    private fun registerPostHandlers() {
        networkService.registerPost("DHT/Ping") {}

        networkService.registerPost("DHT/Store") { message ->
            val data = message.data ?: return@registerPost
            val request = klaxon.parse<StoreRequest>(data) ?: return@registerPost
            storageService.save(request.key, request.data)
        }
    }

    private fun registerGetHandlers() {
        networkService.registerGet("DHT/Find") { message ->
            val data = message.data ?: return@registerGet
            val request = klaxon.parse<FindRequest>(data) ?: return@registerGet
            val bucket = getUserBucket(request.targetUser)
            val response = klaxon.toJsonString(bucket)
            trySaveUser(request.sender, message.sender)
            result(response)
        }

        networkService.registerGet("DHT/Retrieve") { message ->
            val data = message.data ?: return@registerGet
            val key = klaxon.parse<String>(data) ?: return@registerGet
            val responseData = storageService.load<ByteArray>(key)
            val response = klaxon.toJsonString(responseData)
            result(response)
        }
    }

    private suspend fun pingBucket(bucket: Bucket) {
        for (user in bucket.idToIP) {
            if (!networkService.sendPost(user.value, "DHT/Ping")) {
                bucket.idToIP.remove(user)
            }
        }
        bucket.lastPingTime = System.currentTimeMillis()
    }

    private suspend fun trySaveUser(user: UserID, address: InetSocketAddress) {
        val bucket = usersTable.first { it.border.contains(user.rawID) }

        if (bucket.idToIP.containsKey(user)) {
            bucket.idToIP[user] = address
            return
        }

        if (System.currentTimeMillis() - bucket.lastPingTime > PING_TIMER) {
            pingBucket(bucket)
        }

        if (ownerID inBucket bucket) {
            bucket.idToIP[user] = address
            if (bucket.idToIP.size > BUCKET_SIZE) {
                val splitedBuckets = splitBucket(bucket)
                val bucketInd = usersTable.indexOf(bucket)
                usersTable.add(bucketInd, splitedBuckets.second)
                usersTable.add(bucketInd, splitedBuckets.first)
                usersTable.remove(bucket)
            }
        } else if (bucket.idToIP.size < BUCKET_SIZE) {
            bucket.idToIP[user] = address
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

        var previousIterationResult = mutableMapOf<UserID, InetSocketAddress>()
        while (findIterations < MAX_FIND_ITERATIONS && !(nearestUsers equalTo previousIterationResult)) {
            val buffer = TreeMap<UserID, InetSocketAddress>(mapComparator)
            for (user in nearestUsers) {
                if (user.key == ownerID) { continue }
                val request = FindRequest(target, ownerID)
                val response = networkService.sendGet(user.value, "DHT/Find", request) ?: continue
                val responseBucket = klaxon.parse<Bucket>(response) ?: continue
                buffer.putAll(responseBucket.idToIP)
            }

            previousIterationResult = nearestUsers
            nearestUsers.clear()
            buffer.forEach {
                trySaveUser(it.key, it.value)
                if (nearestUsers.size < count) {
                    nearestUsers[it.key] = it.value
                }
            }
            ++findIterations
        }

        return nearestUsers
    }

    override suspend fun store(key: String, data: ByteArray) {
        val dataOwner = generateUserID(key)
        val storingUsers = findNearestNodes(dataOwner, STORE_COPIES_COUNT)
        val request = StoreRequest(key, data)
        for (user in storingUsers) {
            networkService.sendPost(user.value, "DHT/Store", request)
        }
    }

    override suspend fun retrieve(key: String): ByteArray {
        val target = generateUserID(key)
        val storingUsers = findNearestNodes(target, STORE_COPIES_COUNT)
        var data = ByteArray(0)
        for (user in storingUsers) {
            val response = networkService.sendGet(user.value, "DHT/Retrieve", key) ?: continue
            data = klaxon.parse<ByteArray>(response) ?: continue
            break
        }
        return data
    }

    override suspend fun saveUser(userID: UserID, address: InetSocketAddress) {
        trySaveUser(userID, address)
    }

    override suspend fun find(userID: UserID): InetSocketAddress? {
        val potentialUser = findNearestNodes(userID, 1).toList()[0]
        return if (potentialUser.first == userID) {
            potentialUser.second
        } else {
            null
        }
    }
}
