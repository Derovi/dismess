package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.lang.Thread.sleep
import java.net.InetSocketAddress

class SecureNetworkInterfaceTest : KoinTest {
    private lateinit var firstReturn: ByteArray
    private var firstCounter: Int = 0

    class MockNetworkInterface : NetworkInterface {
        lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit
        override suspend fun start(address: InetSocketAddress?) {
            return
        }

        override suspend fun stop() {
            return
        }

        override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
            receiver(InetSocketAddress(1), data)
        }

        override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
            this.receiver = receiver
        }
    }

    private fun init(): SecureNetworkInterface {
        val firstNetworkInterface = MockNetworkInterface()
        val firstSecure = SecureNetworkInterface(firstNetworkInterface)
        firstSecure.setMessageReceiver { _: InetSocketAddress, data: ByteArray ->
            firstReturn = data
            ++firstCounter
        }
        return firstSecure
    }

    private fun check(
        first: SecureNetworkInterface,
        firstCounter: Int
    ) {
        val firstAddress = InetSocketAddress(1)
        val message = "Дарова".toByteArray()
        runBlocking {
            first.sendRawMessage(firstAddress, message)
        }
        Assert.assertArrayEquals(message, firstReturn)
        Assert.assertEquals(firstCounter, this.firstCounter)
    }

    @Test
    fun testSimple() {
        val firstSecure = init()
        check(firstSecure, 1)
    }

    @Test
    fun testSessions() {
        val firstSecure = init()
        firstSecure.setSessionLifetime(10)
        check(firstSecure, 1)
        sleep(100L)
        check(firstSecure, 2)
    }

    @Test
    fun testStress() {
        val firstSecure = init()
        firstSecure.setSessionLifetime(10)
        var counter = 0
        repeat(100000) {
            ++counter
            check(firstSecure, counter)
        }
    }
}
