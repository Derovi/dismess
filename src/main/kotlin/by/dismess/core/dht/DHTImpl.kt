package by.dismess.core.dht

import by.dismess.core.managers.DataManager
import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.generateUserID
import by.dismess.core.utils.gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigInteger
import java.net.InetSocketAddress
import java.util.TreeMap

const val BUCKET_SIZE = 8
const val PING_INTERVAL = 10 * 60000
const val MAX_FIND_ITERATIONS = 10
const val MAX_FIND_ATTEMPTS = 3
const val FIND_FRONT = 50
const val STORE_COPIES_COUNT = 10
val RIGHT_BUCKET_BORDER = BigInteger("2").pow(160)

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService,
    val dataManager: DataManager
) : DHT {
    private val tableMutex = Mutex()
    private var usersTable = mutableListOf<Bucket>()
    private lateinit var ownerID: UserID
    private lateinit var ownerIP: InetSocketAddress

    init {
        runBlocking {
            ownerID = dataManager.getId()
            ownerIP = dataManager.getOwnIP()!!
        }
        val bucket = Bucket(BucketBorder(BigInteger.ONE, RIGHT_BUCKET_BORDER))
        bucket.idToIP[ownerID] = ownerIP
        usersTable.add(bucket)
        registerGetHandlers()
        registerPostHandlers()
    }

    private fun registerPostHandlers() {
        networkService.registerPost("DHT/Ping") {}

        networkService.registerPost("DHT/Store") { message ->
            val data = message.data ?: return@registerPost
            val request = gson.fromJson(data, StoreRequest::class.java) ?: return@registerPost
            storageService.save(request.key, request.data)
        }
    }

    private fun registerGetHandlers() {
        networkService.registerGet("DHT/Find") { message ->
            val data = message.data ?: return@registerGet
            val request = gson.fromJson(data, FindRequest::class.java) ?: return@registerGet
            val bucket = tableMutex.withLock { getUserBucket(request.targetUser) }
            val response = gson.toJson(bucket)
            result(response)
            tableMutex.withLock { trySaveUser(request.sender, message.sender) }
        }

        networkService.registerGet("DHT/Retrieve") { message ->
            val key = message.data ?: return@registerGet
            val responseData = storageService.load<ByteArray>(key)
            val response = gson.toJson(responseData)
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

    // Executed under mutex
    private suspend fun trySaveUser(user: UserID, address: InetSocketAddress) {
        val bucket = getUserBucket(user)

        if (bucket.idToIP.containsKey(user)) {
            bucket.idToIP[user] = address
            return
        }

        if (System.currentTimeMillis() - bucket.lastPingTime > PING_INTERVAL) {
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

    // Executed under mutex
    private fun getUserBucket(userId: UserID): Bucket = usersTable.first { userId inBucket it }

    private suspend fun findNearestNodes(
        target: UserID,
        count: Int
    ): MutableMap<UserID, InetSocketAddress> {
        val nearestUsers = mutableMapOf<UserID, InetSocketAddress>()
        nearestUsers.putAll(tableMutex.withLock { getUserBucket(target).idToIP })

        var findIterations = 0

        val mapComparator = kotlin.Comparator { firstUser: UserID, secondUser: UserID ->
            when {
                firstUser distanceTo target < secondUser distanceTo target -> return@Comparator -1
                firstUser distanceTo target > secondUser distanceTo target -> return@Comparator 1
                else -> return@Comparator 0
            }
        }
        val buffer = TreeMap<UserID, InetSocketAddress>(mapComparator)

        val previousIterationResult = mutableMapOf<UserID, InetSocketAddress>()
        while (findIterations < MAX_FIND_ITERATIONS && !(nearestUsers equalTo previousIterationResult)) {
            buffer.clear()
            for (user in nearestUsers) {
                val request = FindRequest(target, ownerID)
                val response = networkService.sendGet(user.value, "DHT/Find", request) ?: continue
                val responseBucket = gson.fromJson(response, Bucket::class.java) ?: continue
                buffer.putAll(responseBucket.idToIP)
            }

            previousIterationResult.clear()
            previousIterationResult.putAll(nearestUsers)
            nearestUsers.clear()

            buffer.forEach {
                tableMutex.withLock { trySaveUser(it.key, it.value) }
                if (nearestUsers.size < count) {
                    nearestUsers[it.key] = it.value
                }
            }

            ++findIterations
        }

        return nearestUsers
    }

    override suspend fun store(key: String, data: ByteArray): Boolean {
        val dataOwner = generateUserID(key)
        val storingUsers = findNearestNodes(dataOwner, STORE_COPIES_COUNT)
        val request = StoreRequest(key, data)
        var isStored = false
        for (user in storingUsers) {
            isStored = isStored || networkService.sendPost(user.value, "DHT/Store", request)
        }
        return isStored
    }

    override suspend fun retrieve(key: String): ByteArray? {
        val target = generateUserID(key)
        val storingUsers = findNearestNodes(target, STORE_COPIES_COUNT)
        var data: ByteArray? = null
        for (user in storingUsers) {
            val response = networkService.sendGet(user.value, "DHT/Retrieve", key) ?: continue
            data = gson.fromJson(response, ByteArray::class.java) ?: continue
            break
        }
        return data
    }

    override suspend fun connectTo(userID: UserID, address: InetSocketAddress) {
        tableMutex.withLock { trySaveUser(userID, address) }
        find(ownerID)
    }

    override suspend fun find(userID: UserID): InetSocketAddress? {
        var findAttempt = 0
        var nearestUsers = findNearestNodes(userID, FIND_FRONT).toList()
        while (nearestUsers.isEmpty() && findAttempt < MAX_FIND_ATTEMPTS) {
            ++findAttempt
            nearestUsers = findNearestNodes(userID, FIND_FRONT).toList()
        }

        return when {
            nearestUsers.isEmpty() -> null
            nearestUsers[0].first == userID -> nearestUsers[0].second
            else -> null
        }
    }
}
