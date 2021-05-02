package by.dismess.core.services

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.net.InetSocketAddress

class NetworkServiceTest : KoinTest {

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
    fun basicsTest() {
        val network = VirtualNetwork()
        val aliceNI = VirtualNetworkInterface(network, InetSocketAddress("127.228.125.1", 1234))
        val bobNI = VirtualNetworkInterface(network, InetSocketAddress("117.85.32.8", 2231))
        val aliceNS = NetworkService(aliceNI)
        val bobNS = NetworkService(bobNI)
        aliceNS.registerGet("avatar") { message ->
            Assert.assertEquals(message.sender, bobNI.ownAddress)
            result("<(^|^)>")
        }
        Assert.assertEquals(runBlocking { bobNS.sendGet(aliceNI.ownAddress, "avatar") }, "<(^|^)>")
    }
}
