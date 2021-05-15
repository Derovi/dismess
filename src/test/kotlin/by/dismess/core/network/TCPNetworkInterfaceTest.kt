package by.dismess.core.network

import by.dismess.core.utils.gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.random.Random

class TCPNetworkInterfaceTest {
    private lateinit var message: ByteArray
    private var sendCounter: Int = 0
    private var receiveCounter: Int = 0

    private fun init(): Pair<TCPNetworkInterfaceImpl, TCPNetworkInterfaceImpl> {
        val first = TCPNetworkInterfaceImpl()
        val second = TCPNetworkInterfaceImpl()
        val receiver = { _: InetSocketAddress, data: ByteArray ->
            Assert.assertArrayEquals(message, data)
            ++receiveCounter
            Unit
        }
        first.setMessageReceiver(receiver)
        second.setMessageReceiver(receiver)
        return Pair(first, second)
    }

    private suspend fun send(tcp: TCPNetworkInterfaceImpl, port: Int, message: ByteArray) {
        tcp.sendRawMessage(InetSocketAddress(port), message)
        ++sendCounter
    }

    @Test
    fun testSimple() {
        val (first, second) = init()
        message = "Дарова".toByteArray()
        runBlocking {
            first.start(InetSocketAddress(1234))
            second.start(InetSocketAddress(2228))
            send(first, 2228, message)
            send(second, 1234, message)
            Assert.assertEquals(sendCounter, receiveCounter)
            Assert.assertNotEquals(receiveCounter, 0)
            first.stop()
            second.stop()
        }
    }
}
