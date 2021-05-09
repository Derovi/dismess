package by.dismess.core.dht

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

    class TestUser(
        val login: String,
        val address: InetSocketAddress,
        network: VirtualNetwork
    ) {
        val id = generateUserID(login)
        val networkInterface = VirtualNetworkInterface(network, address)
        val networkService = NetworkService(networkInterface)
        val storageInterface = MockStorageInterface()
        val storageService = StorageService(storageInterface)
        val DHT = DHTImpl(networkService, storageService, id, address)
    }

    @Test
    fun findSimpleTest() {
        val network = VirtualNetwork()

        val alice = TestUser("Alice", InetSocketAddress("228.192.201.1", 1234), network)
        val bob = TestUser("Bob", InetSocketAddress("144.169.196.225", 4321), network)
        runBlocking { bob.DHT.connectTo(alice.id, alice.address) }

        Assert.assertEquals(runBlocking { alice.DHT.find(bob.id) }, bob.address)
        Assert.assertEquals(runBlocking { bob.DHT.find(alice.id) }, alice.address)
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    @Test
    fun findTest() {
        val network = VirtualNetwork()

        val firstUser = TestUser("Boss of this gym", InetSocketAddress("228.228.228.228", 2288), network)
        val usersList = mutableListOf<TestUser>(firstUser)

        val loginValidate = mutableMapOf<String, Int>()
        for (i in 1..1000) {
            var randomLogin = getRandomString(Random.nextInt(10, 60))
            while (loginValidate.containsKey(randomLogin)) { randomLogin = getRandomString(Random.nextInt(10, 60)) }
            loginValidate[randomLogin] = 1
            var randomIP = Random.nextInt(256).toString()
            repeat(3) {
                randomIP += "." + Random.nextInt(256)
            }
            val randomAddress = InetSocketAddress(randomIP, Random.nextInt(1000, 10000))
            usersList.add(TestUser(randomLogin, randomAddress, network))
            val randomUser = usersList[Random.nextInt(i)]
            runBlocking { usersList[i].DHT.connectTo(randomUser.id, randomUser.address) }
        }

//        for (i in 1..1) {
//            val user = Random.nextInt(usersList.size)
//            var target = Random.nextInt(usersList.size)
//            while (user == target) {
//                target = Random.nextInt(usersList.size)
//            }
//            val targetID = usersList[target].id
//            val targetAddress = usersList[target].address
//            Assert.assertEquals(runBlocking { usersList[user].DHT.verboseFind(targetID) }, targetAddress)
//        }
    }
}
