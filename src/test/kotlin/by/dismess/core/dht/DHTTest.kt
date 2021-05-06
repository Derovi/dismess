package by.dismess.core.dht

import by.dismess.core.outer.NetworkInterface
import by.dismess.core.outer.StorageInterface
import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService
import by.dismess.core.utils.generateUserID
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import java.net.InetSocketAddress

class DHTTest : KoinTest {
    private val storageService by inject<StorageService>()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(
            module {
                single { StorageService(get()) }
                single<StorageInterface> {
                    object : StorageInterface {
                        private val map = hashMapOf<String, ByteArray>()

                        override suspend fun exists(key: String): Boolean = map.containsKey(key)

                        override suspend fun saveRawData(key: String, data: ByteArray) {
                            map[key] = data
                        }

                        override suspend fun loadRawData(key: String): ByteArray? = map[key]
                        override suspend fun forget(key: String) {
                            map.remove(key)
                        }
                    }
                }
            }
        )
    }

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

    @Test
    fun findTest() {
        val network = VirtualNetwork()

        val aliceID = generateUserID("Alice")
        val aliceAddress = InetSocketAddress("228.192.201.1", 1234)
        val aliceNI = VirtualNetworkInterface(network, aliceAddress)
        val aliceNS = NetworkService(aliceNI)
        val aliceDHT = DHTImpl(aliceNS, storageService)

        val bobID = generateUserID("Bob")
        val bobAddress = InetSocketAddress("144.169.196.225", 4321)
        val bobNI = VirtualNetworkInterface(network, bobAddress)
        val bobNS = NetworkService(bobNI)
        val bobDHT = DHTImpl(bobNS, storageService)

        runBlocking { aliceDHT.saveUser(bobID, bobAddress) }
        val address = runBlocking {
            aliceDHT.find(bobID)
        }
    }
}
