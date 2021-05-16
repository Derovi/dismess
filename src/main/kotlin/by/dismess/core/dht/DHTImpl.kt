package by.dismess.core.dht

import by.dismess.core.managers.DataManager
import by.dismess.core.outer.StorageInterface
import by.dismess.core.services.NetworkService
import by.dismess.core.utils.UniqID
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
val RIGHT_BUCKET_BORDER: BigInteger = BigInteger("2").pow(160)

class DHTImpl(
    val networkService: NetworkService,
    val storageInterface: StorageInterface,
    val dataManager: DataManager
) : DHT {
    private val tableMutex = Mutex()
    private var usersTable = mutableListOf<Bucket>()
    private var ownerID: UniqID = runBlocking { dataManager.getId() }
    private var ownerIP: InetSocketAddress = runBlocking { dataManager.getOwnIP()!! }

    init {
        val bucket = Bucket(BucketBorder(BigInteger.ONE, RIGHT_BUCKET_BORDER))
        usersTable.add(bucket)
        registerGetHandlers()
        registerPostHandlers()
    }

    override fun initSelf(ownerID: UniqID, ownerIP: InetSocketAddress) {
        this.ownerID = ownerID
        this.ownerIP = ownerIP
        runBlocking {
            trySaveUser(ownerID, ownerIP)
        }
    }

    private fun registerPostHandlers() {
        networkService.registerPost("DHT/Ping") {}

        networkService.registerPost("DHT/Store") { message ->
            val data = message.data ?: return@registerPost
            val request = gson.fromJson(data, StoreRequest::class.java) ?: return@registerPost
            storageInterface.saveRawData(request.key, request.data)
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
            val responseData = storageInterface.loadRawData(key)
            val response = gson.toJson(responseData)
            result(response)
        }
        networkService.registerGet("DHT/Validate") { message ->
            val login = message.data ?: return@registerGet
            val id = generateUserID(login)
            val user: InetSocketAddress? = find(id)
            val response = gson.toJson(user == null)
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
    private suspend fun trySaveUser(user: UniqID, address: InetSocketAddress) {
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
    private fun getUserBucket(userId: UniqID): Bucket = usersTable.first { userId inBucket it }

    private suspend fun findNearestNodes(
        target: UniqID,
        count: Int
    ): MutableMap<UniqID, InetSocketAddress> {
        val nearestUsers = mutableMapOf<UniqID, InetSocketAddress>()
        nearestUsers.putAll(tableMutex.withLock { getUserBucket(target).idToIP })

        var findIterations = 0

        val mapComparator = kotlin.Comparator { firstUser: UniqID, secondUser: UniqID ->
            when {
                firstUser distanceTo target < secondUser distanceTo target -> return@Comparator -1
                firstUser distanceTo target > secondUser distanceTo target -> return@Comparator 1
                else -> return@Comparator 0
            }
        }
        val buffer = TreeMap<UniqID, InetSocketAddress>(mapComparator)

        val previousIterationResult = mutableMapOf<UniqID, InetSocketAddress>()
        while (findIterations < MAX_FIND_ITERATIONS && !(nearestUsers equalTo previousIterationResult)) {
            buffer.clear()
            for (user in nearestUsers) {
                val request = FindRequest(target, ownerID!!)
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

    override suspend fun connectTo(userID: UniqID, address: InetSocketAddress) {
        tableMutex.withLock { trySaveUser(userID, address) }
        find(ownerID)
    }

    override suspend fun find(userID: UniqID): InetSocketAddress? {
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

    override suspend fun isValidLogin(address: InetSocketAddress, login: String): Boolean {
        val response = networkService.sendGet(address, "DHT/Validate", login) ?: return false
        return gson.fromJson(response, Boolean::class.java) ?: false
    }
}
