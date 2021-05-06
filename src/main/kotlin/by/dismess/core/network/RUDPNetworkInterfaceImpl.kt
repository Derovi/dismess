package by.dismess.core.network

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.joinu.rudp.QueuedDatagramPacket
import net.joinu.rudp.RUDPSocket
import net.joinu.rudp.runSuspending
import net.joinu.rudp.send
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import java.net.ServerSocket

class RUDPNetworkInterfaceImpl : NetworkInterface {
    private lateinit var rudpSocket: RUDPSocket
    private var stopped: Boolean = true
    private lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit

    private fun receive(dataPacket: QueuedDatagramPacket) {
        val dataBytes = ByteArray(dataPacket.data.capacity())
        dataPacket.data.get(dataBytes)
        receiver(dataPacket.address, dataBytes)
    }

    private suspend fun run() {
        while (!stopped) {
            receive(rudpSocket.receive())
        }
    }

    private fun findFreePort() = ServerSocket(0).use { it.localPort }

    override suspend fun start(address: InetSocketAddress?) {
        var serverAddress = address
        if (serverAddress == null) {
            serverAddress = InetSocketAddress(findFreePort())
        }
        stopped = false
        rudpSocket = RUDPSocket()
        rudpSocket.bind(serverAddress)
        GlobalScope.launch {
            try {
                rudpSocket.runSuspending()
            } catch (exception: IllegalStateException) {
                if (!stopped) {
                    throw exception
                }
            }
        }
        GlobalScope.launch { run() }
    }

    override suspend fun stop() {
        stopped = true
        rudpSocket.close()
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        coroutineScope {
            launch { rudpSocket.send(data, address) }
        }
    }

    override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
        this.receiver = receiver
    }
}
