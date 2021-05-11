package by.dismess.core.dht

import by.dismess.core.chating.attachments.ImageAttachment
import by.dismess.core.managers.DataManager
import by.dismess.core.model.UserID
import by.dismess.core.outer.NetworkInterface
import by.dismess.core.outer.StorageInterface
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.generateUserID
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.net.InetSocketAddress
import kotlin.random.Random

class DHTTest : KoinTest {

    class VirtualNetwork {
        private val networkInterfaces = mutableMapOf<InetSocketAddress, VirtualNetworkInterface>()

        fun register(networkInterface: VirtualNetworkInterface) {
            networkInterfaces[networkInterface.ownAddress] = networkInterface
        }

        fun sendMessage(from: InetSocketAddress, to: InetSocketAddress, data: ByteArray) {
            networkInterfaces[to]?.receiver?.let { it(from, data) }
        }
    }

    class VirtualNetworkInterface(val network: VirtualNetwork, val ownAddress: InetSocketAddress) : NetworkInterface {
        lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit

        init {
            network.register(this)
        }

        override suspend fun start(address: InetSocketAddress?) {
            return
        }

        override suspend fun stop() {
            return
        }

        override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
            network.sendMessage(ownAddress, address, data)
        }

        override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
            this.receiver = receiver
        }
    }

    class MockStorageInterface : StorageInterface {
        private val storage = mutableMapOf<String, ByteArray>()

        override suspend fun exists(key: String): Boolean = storage.containsKey(key)

        override suspend fun forget(key: String) {
            storage.remove(key)
        }

        override suspend fun loadRawData(key: String): ByteArray? = storage[key]

        override suspend fun saveRawData(key: String, data: ByteArray) {
            storage[key] = data
        }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    class MockDataManager : DataManager {

        private fun getRandomString(length: Int): String {
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        override suspend fun getId(): UserID {
            return generateUserID(getRandomString(Random.nextInt(10, 60)))
        }

        override suspend fun saveLogin(login: String) {}

        override suspend fun getLogin(): String? {
            return null
        }

        override suspend fun saveDisplayName(displayName: String) {}

        override suspend fun getDisplayName(): String? {
            return null
        }

        override suspend fun saveAvatar(avatar: ImageAttachment) {}

        override suspend fun getAvatar(): ImageAttachment? {
            return null
        }

        override suspend fun setOwnIP(ip: InetSocketAddress) {}

        override suspend fun getOwnIP(): InetSocketAddress? {
            var randomIP = Random.nextInt(256).toString()
            repeat(3) {
                randomIP += "." + Random.nextInt(256)
            }
            return InetSocketAddress(randomIP, Random.nextInt(1000, 10000))
        }

        override suspend fun saveLastIP(userID: UserID, ip: InetSocketAddress) {}

        override suspend fun getLastIP(userID: UserID): InetSocketAddress? {
            return null
        }
    }

    class TestUser(
        network: VirtualNetwork
    ) {
        val dataManager = MockDataManager()
        val address = runBlocking { dataManager.getOwnIP()!! }
        val id = runBlocking { dataManager.getId() }
        val networkInterface = VirtualNetworkInterface(network, address)
        val networkService = NetworkService(networkInterface)
        val storageInterface = MockStorageInterface()
        val storageService = StorageService(storageInterface)
        val DHT = DHTImpl(networkService, storageService, dataManager)
    }

    @Test
    fun findTest() {
        val network = VirtualNetwork()

        val firstUser = TestUser(network)
        val usersList = mutableListOf<TestUser>(firstUser)

        for (i in 1..1000) {
            usersList.add(TestUser(network))
            val randomUser = usersList[Random.nextInt(i)]
            runBlocking { usersList[i].DHT.connectTo(randomUser.id, randomUser.address) }
        }

        for (i in 1..1000) {
            val user = Random.nextInt(usersList.size)
            var target = Random.nextInt(usersList.size)
            while (user == target) {
                target = Random.nextInt(usersList.size)
            }
            val targetID = usersList[target].id
            val targetAddress = usersList[target].address
            val findResult = runBlocking { usersList[user].DHT.find(targetID) }
            Assert.assertEquals(findResult, targetAddress)
        }
    }

    @Test
    fun findSimpleTest() {
        val network = VirtualNetwork()

        val alice = TestUser(network)
        val bob = TestUser(network)
        runBlocking { bob.DHT.connectTo(alice.id, alice.address) }

        Assert.assertEquals(runBlocking { alice.DHT.find(bob.id) }, bob.address)
        Assert.assertEquals(runBlocking { bob.DHT.find(alice.id) }, alice.address)
    }

    @Test
    fun storeSimpleTest() {
        val network = VirtualNetwork()

        val alice = TestUser(network)
        val bob = TestUser(network)
        runBlocking { bob.DHT.connectTo(alice.id, alice.address) }

        val message = "Very interesting text"
        Assert.assertTrue(runBlocking { alice.DHT.store("Secrete message", message.toByteArray()) })

        val aliceResponse = String(runBlocking { alice.DHT.retrieve("Secrete message") } ?: ByteArray(0))
        val bobResponse = String(runBlocking { bob.DHT.retrieve("Secrete message") } ?: ByteArray(0))
        Assert.assertEquals(message, aliceResponse)
        Assert.assertEquals(message, bobResponse)

        val emptyResponse = String(runBlocking { alice.DHT.retrieve("Wrong key") } ?: ByteArray(0))
        Assert.assertEquals(String(), emptyResponse)
    }

    @Test
    fun storeTest() {
        val network = VirtualNetwork()
        val firstUser = TestUser(network)
        val usersList = mutableListOf(firstUser)

        for (i in 1..1000) {
            usersList.add(TestUser(network))
            val randomUser = usersList[Random.nextInt(i)]
            runBlocking { usersList[i].DHT.connectTo(randomUser.id, randomUser.address) }
        }

        val data = mutableMapOf<String, String>()
        for (i in 1..1000) {
            val key = getRandomString(Random.nextInt(50, 100))
            val message = getRandomString(Random.nextInt(100))
            data[key] = message
            Assert.assertTrue(runBlocking { firstUser.DHT.store(key, message.toByteArray()) })
        }

        for (request in data) {
            val response = runBlocking { firstUser.DHT.retrieve(request.key) }
            val message = String(response ?: ByteArray(0))
            Assert.assertEquals(request.value, message)
        }

        for (i in 1..100) {
            val wrongKey = getRandomString(Random.nextInt(50, 100))
            val response = runBlocking { firstUser.DHT.retrieve(wrongKey) }
            val message = String(response ?: ByteArray(0))
            Assert.assertEquals(message, String())
        }
    }
}
