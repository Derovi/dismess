package by.dismess.core.services

import by.dismess.core.common.VirtualNetwork
import by.dismess.core.common.VirtualNetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.net.InetSocketAddress

class NetworkServiceTest : KoinTest {

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
