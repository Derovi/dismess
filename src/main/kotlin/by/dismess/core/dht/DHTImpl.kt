package by.dismess.core.dht

import by.dismess.core.model.UserID
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.generateUserID
import by.dismess.core.utils.gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigInteger
import java.net.InetSocketAddress
import java.util.TreeMap

const val BUCKET_SIZE = 8
const val PING_TIMER = 10 * 60000
const val MAX_FIND_ITERATIONS = 100
const val STORE_COPIES_COUNT = 10

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService,
    var ownerID: UserID,
    var ownerIP: InetSocketAddress
) : DHT {
    private val tableMutex = Mutex()
    private var usersTable = mutableListOf<Bucket>()

    init {
        val bucket = Bucket(BucketBorder(BigInteger.ONE, BigInteger.TWO.pow(160)))
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
            val bucket = tableMutex.withLock { getNearestUserBucket(request.targetUser) }
            val response = gson.toJson(bucket)
            tableMutex.withLock { trySaveUser(request.sender, message.sender) }
            result(response)
        }

        networkService.registerGet("DHT/Retrieve") { message ->
            val data = message.data ?: return@registerGet
            val key = gson.fromJson(data, String::class.java) ?: return@registerGet
            val responseData = storageService.load<ByteArray>(key)
            val response = gson.toJson(responseData)
            result(response)
        }
    }

    private suspend fun pingBucket(bucket: Bucket) {
        tableMutex.lock()
        for (user in bucket.idToIP) {
            if (!networkService.sendPost(user.value, "DHT/Ping")) {
                bucket.idToIP.remove(user)
            }
        }
        bucket.lastPingTime = System.currentTimeMillis()
        tableMutex.unlock()
    }

    // Executed under mutex
    private suspend fun trySaveUser(user: UserID, address: InetSocketAddress) {
        val bucket = getUserBucket(user)

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
    // Executed under mutex
    private fun getUserBucket(userId: UserID): Bucket = usersTable.first { userId inBucket it }
    // Executed under mutex
    private fun getNearestUserBucket(userID: UserID): Bucket {
        val potentialBucketIndex = usersTable.indexOfFirst { userID inBucket it }
        val leftIndex = usersTable.subList(0, potentialBucketIndex).indexOfLast { it.idToIP.isNotEmpty() }
        val rightIndex =
            usersTable.subList(potentialBucketIndex, usersTable.size).indexOfFirst { it.idToIP.isNotEmpty() }
        return when {
            leftIndex == -1 -> usersTable[rightIndex]
            rightIndex == -1 -> usersTable[leftIndex]
            potentialBucketIndex - leftIndex < rightIndex - potentialBucketIndex -> usersTable[leftIndex]
            else -> usersTable[rightIndex]
        }
    }

    private suspend fun findNearestNodes(
        target: UserID,
        count: Int,
        verbose: Boolean = false
    ): MutableMap<UserID, InetSocketAddress> {
        val nearestUsers = mutableMapOf<UserID, InetSocketAddress>()
        nearestUsers.putAll(tableMutex.withLock { getNearestUserBucket(target).idToIP })

        var findIterations = 0

        val mapComparator = kotlin.Comparator { firstUser: UserID, secondUser: UserID ->
            when {
                firstUser distanceTo target < secondUser distanceTo target -> return@Comparator -1
                firstUser distanceTo target > secondUser distanceTo target -> return@Comparator 1
                else -> return@Comparator 0
            }
        }

        val previousIterationResult = mutableMapOf<UserID, InetSocketAddress>()
        while (findIterations < MAX_FIND_ITERATIONS && !(nearestUsers equalTo previousIterationResult)) {
            if (verbose) {
                println("Iteration: $findIterations")
            }

            val buffer = TreeMap<UserID, InetSocketAddress>(mapComparator)
            for (user in nearestUsers) {
                if (user.key == ownerID) {
                    continue
                }
                val request = FindRequest(target, ownerID)
                val response = networkService.sendGet(user.value, "DHT/Find", request) ?: continue
                val responseBucket = gson.fromJson(response, Bucket::class.java) ?: continue
                buffer.putAll(responseBucket.idToIP)
            }

            previousIterationResult.clear()
            previousIterationResult.putAll(nearestUsers)
            nearestUsers.clear()

            buffer.forEach {
                if (verbose) {
                    println("Distance: ${it.key distanceTo target}")
                    println("Try save it")
                }
                tableMutex.withLock { trySaveUser(it.key, it.value) }
                if (nearestUsers.size < count) {
                    nearestUsers[it.key] = it.value
                }
            }
            if (verbose) {
                println()
            }
            ++findIterations
        }

        return nearestUsers
    }

    fun printTable() {
        println()
        println("Owner: $ownerID")
        for (bucket in usersTable) {
            println("-----------------------------------------------------------------")
            bucket.printBucketData()
            println("-----------------------------------------------------------------")
        }
        println()
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
            data = gson.fromJson(response, ByteArray::class.java) ?: continue
            break
        }
        return data
    }

    override suspend fun connectTo(userID: UserID, address: InetSocketAddress) {
        tableMutex.withLock { trySaveUser(userID, address) }
//        verboseFind(ownerID)
        find(ownerID)
    }

    override suspend fun find(userID: UserID): InetSocketAddress? {
        val potentialUser = findNearestNodes(userID, 10).toList()[0]
        return if (potentialUser.first == userID) {
            potentialUser.second
        } else {
            null
        }
    }

    suspend fun verboseFind(userID: UserID): InetSocketAddress? {
        println("Starting distance: ${ownerID distanceTo userID}")
        val potentialUser = findNearestNodes(userID, 10, true).toList()[0]
        return if (potentialUser.first == userID) {
            potentialUser.second
        } else {
            null
        }
    }
}
