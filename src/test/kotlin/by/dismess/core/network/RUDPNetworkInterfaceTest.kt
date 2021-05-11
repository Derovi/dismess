package by.dismess.core.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.random.Random

class RUDPNetworkInterfaceTest {
    private lateinit var message: ByteArray
    private var sendCounter: Int = 0
    private var receiveCounter: Int = 0

    private fun init(): Pair<RUDPNetworkInterfaceImpl, RUDPNetworkInterfaceImpl> {
        val first = RUDPNetworkInterfaceImpl()
        val second = RUDPNetworkInterfaceImpl()
        val receiver = { _: InetSocketAddress, data: ByteArray ->
            Assert.assertArrayEquals(message, data)
            ++receiveCounter
            Unit
        }
        first.setMessageReceiver(receiver)
        second.setMessageReceiver(receiver)
        return Pair(first, second)
    }

    private suspend fun send(rudp: RUDPNetworkInterfaceImpl, port: Int, message: ByteArray) {
        rudp.sendRawMessage(InetSocketAddress(port), message)
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

    @Test
    fun testBigData() {
        val (first, second) = init()
        message = ByteArray(200000) { 65 }
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
            Assert.assertEquals(sendCounter, receiveCounter)
            Assert.assertNotEquals(receiveCounter, 0)
            first.stop()
            second.stop()
        }
    }

    @Test
    fun testStressOrder() {
        val first = RUDPNetworkInterfaceImpl()
        val second = RUDPNetworkInterfaceImpl()
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
            Assert.assertEquals(sendCounter, receiveCounter)
            Assert.assertNotEquals(receiveCounter, 0)
            first.stop()
            second.stop()
        }
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
        val users: MutableList<RUDPNetworkInterfaceImpl> = mutableListOf()
        val messages: MutableList<MutableList<MutableList<ByteArray>>> = mutableListOf()
        for (i in 0 until usersNumber) {
            messages.add(mutableListOf())
            for (j in 0 until usersNumber) {
                messages[i].add(mutableListOf())
            }
        }
        runBlocking {
            for (i in 0 until sockets.size) {
                val user = RUDPNetworkInterfaceImpl()
                user.start(sockets[i])
                user.setMessageReceiver { sender: InetSocketAddress, data: ByteArray ->
                    var senderInd = 0
                    for (j in 0 until sockets.size) {
                        if (sockets[j].port == sender.port) {
                            senderInd = j
                        }
                    }
                    val message = messages[senderInd][i][0]
                    messages[senderInd][i].removeAt(0)
                    Assert.assertArrayEquals(message, data)
                    ++receiveCounter
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
            Assert.assertEquals(receiveCounter, sendCounter)
            Assert.assertNotEquals(receiveCounter, 0)
            for (user in users) {
                user.stop()
            }
        }
    }
}
