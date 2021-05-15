package by.dismess.core.network

import by.dismess.core.outer.NetworkInterface
import by.dismess.core.utils.gson
import by.dismess.core.utils.intToBytes
import by.dismess.core.utils.twoBytesToInt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.joinu.rudp.QueuedDatagramPacket
import net.joinu.rudp.RUDPSocket
import net.joinu.rudp.runSuspending
import net.joinu.rudp.send
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.IllegalStateException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class TCPNetworkInterfaceImpl : NetworkInterface {
    private lateinit var serverSocket: ServerSocket
    private var stopped: Boolean = true
    private lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit
    private var serverAddress: InetSocketAddress? = null

    private suspend fun run() {
        while (!stopped) {
            val connection = serverSocket.accept()
            handleConnection(connection)
        }
    }

    private fun handleConnection(connection: Socket) {
        val stream = connection.getInputStream()
        val senderAddress = InetAddress.getByAddress(stream.readNBytes(4))
        val senderPort = twoBytesToInt(stream.readNBytes(2))
        val sender = InetSocketAddress(senderAddress, senderPort)
        val data = stream.readAllBytes()
        receiver(sender, data)
    }

    private fun findFreePort() = ServerSocket(0).use { it.localPort }

    override suspend fun start(address: InetSocketAddress?) {
        serverAddress = address
        if (serverAddress == null) {
            serverAddress = InetSocketAddress(findFreePort())
        }
        stopped = false
        serverSocket = ServerSocket(serverAddress!!.port)
        GlobalScope.launch { run() }
    }

    override suspend fun stop() {
        stopped = true
        serverSocket.close()
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        val connection = Socket(address.address, address.port)
        if (serverAddress == null) {
            serverAddress = InetSocketAddress(findFreePort())
        }
        connection.getOutputStream().write(serverAddress!!.address.address)
        connection.getOutputStream().write(intToBytes(serverAddress!!.port, 2))
        connection.getOutputStream().write(data)
        connection.getOutputStream().close()
    }

    override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
        this.receiver = receiver
    }
}
