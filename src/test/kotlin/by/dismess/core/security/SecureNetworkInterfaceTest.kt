package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.koin.test.KoinTest
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress

class SecureNetworkInterfaceTest : KoinTest {
    private lateinit var firstReturn: ByteArray
    private var firstCounter: Int = 0
    private lateinit var secondReturn: ByteArray
    private var secondCounter: Int = 0

    class MockNetworkInterface : NetworkInterface {
        lateinit var receiver: (sender: InetAddress, data: ByteArray) -> Unit
        override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
            receiver(InetAddress.getByName(""), data)
        }

        override fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit) {
            this.receiver = receiver
        }
    }

    private fun init(): SecureNetworkInterface {
        val firstNetworkInterface = MockNetworkInterface()
        val firstSecure = SecureNetworkInterface(firstNetworkInterface)
        firstSecure.setMessageReceiver { _: InetAddress, data: ByteArray ->
            firstReturn = data
            ++firstCounter
        }
        return firstSecure
    }

    private fun check(
        first: SecureNetworkInterface,
        firstCounter: Int,
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
        check(firstSecure, 1)
        sleep(61000L)
        check(firstSecure, 2)
    }

    @Test
    fun testStress() {
        val firstSecure = init()
        var counter = 0
        repeat(61000) {
            ++counter
            check(firstSecure, counter)
            sleep(1L)
        }
    }
}
