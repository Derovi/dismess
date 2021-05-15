package by.dismess.core.network

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.random.Random

class TCPNetworkInterfaceTest {
    private lateinit var message: ByteArray
    private var sendCounter: Int = 0
    private var receiveCounter: Int = 0

    private fun init(): Pair<NetworkInterface, NetworkInterface> {
        val first = TCPNetworkInterfaceImpl()
        val second = TCPNetworkInterfaceImpl()
        val receiver = { _: InetSocketAddress, data: ByteArray ->
            ++receiveCounter
            Assert.assertArrayEquals(message, data)
            println(message.decodeToString())
        }
        first.setMessageReceiver(receiver)
        second.setMessageReceiver(receiver)
        return Pair(first, second)
    }

    private suspend fun send(ni: NetworkInterface, port: Int, message: ByteArray) {
        ++sendCounter
        ni.sendRawMessage(InetSocketAddress(port), message)
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
            first.stop()
            second.stop()
        }
        Assert.assertNotEquals(receiveCounter, 0)
        Assert.assertEquals(sendCounter, receiveCounter)
    }

    @Test
    fun testBigData() {
        val (first, second) = init()
        message = ByteArray(200000) { 65 }
        runBlocking {
            first.start(InetSocketAddress(1234))
            second.start(InetSocketAddress(2228))
            send(first, 2228, message)
            send(second, 1234, message)
            first.stop()
            second.stop()
        }
        Assert.assertEquals(sendCounter, receiveCounter)
        Assert.assertNotEquals(receiveCounter, 0)
    }

    @Test
    fun testSimpleStress() {
        val (first, second) = init()
        message = ByteArray(200) { 65 }
        runBlocking {
            first.start(InetSocketAddress(1234))
            second.start(InetSocketAddress(2228))
            repeat(1000) {
                send(first, 2228, message)
                send(second, 1234, message)
            }
            first.stop()
            second.stop()
        }
        Assert.assertEquals(sendCounter, receiveCounter)
        Assert.assertNotEquals(receiveCounter, 0)
    }

    @Test
    fun testStressOrder() {
        val (first, second) = init()
        val receiver = { _: InetSocketAddress, data: ByteArray ->
            Assert.assertArrayEquals(byteArrayOf(receiveCounter.toByte()), data)
            receiveCounter += 1
        }
        first.setMessageReceiver(receiver)
        second.setMessageReceiver(receiver)
        runBlocking {
            first.start(InetSocketAddress(1234))
            second.start(InetSocketAddress(2228))
            repeat(1000) {
                message = byteArrayOf(sendCounter.toByte())
                send(first, 2228, message)
            }
            sleep(10L)
            first.stop()
            second.stop()
        }
        Assert.assertEquals(sendCounter, receiveCounter)
        Assert.assertNotEquals(receiveCounter, 0)
    }

    private fun findFreePort() = ServerSocket(0).use { it.localPort }

    private fun generateSenderAndReceiver(): Pair<Int, Int> {
        val senderInd = (0 until 10).random()
        var receiverInd = (0 until 10).random()
        while (receiverInd == senderInd) {
            receiverInd = (0 until 10).random()
        }
        return Pair(senderInd, receiverInd)
    }

    @Test
    fun testStressMultipleUsers() {
        val usersNumber = 10
        val sockets: MutableList<InetSocketAddress> = mutableListOf()
        repeat(usersNumber) {
            sockets.add(InetSocketAddress(findFreePort()))
        }
        val users: MutableList<NetworkInterface> = mutableListOf()
        val messages: MutableList<MutableList<MutableList<ByteArray>>> = mutableListOf()
        for (i in 0 until usersNumber) {
            messages.add(mutableListOf())
            for (j in 0 until usersNumber) {
                messages[i].add(mutableListOf())
            }
        }
        runBlocking {
            for (i in 0 until sockets.size) {
                val user = TCPNetworkInterfaceImpl()
                user.start(sockets[i])
                user.setMessageReceiver { sender: InetSocketAddress, data: ByteArray ->
                    ++receiveCounter
                    var senderInd = 0
                    for (j in 0 until sockets.size) {
                        if (sockets[j].port == sender.port) {
                            senderInd = j
                        }
                    }
                    val message = messages[senderInd][i][0]
                    messages[senderInd][i].removeAt(0)
                    Assert.assertArrayEquals(message, data)
                    Unit
                }
                users.add(user)
            }
            repeat(1000) {
                val (senderInd, receiverInd) = generateSenderAndReceiver()
                message = ByteArray(200)
                Random.nextBytes(message)
                messages[senderInd][receiverInd].add(message)
                send(users[senderInd], sockets[receiverInd].port, message)
            }
            sleep(10L)
            for (user in users) {
                user.stop()
            }
        }
        Assert.assertEquals(receiveCounter, sendCounter)
        Assert.assertNotEquals(receiveCounter, 0)
    }
}
