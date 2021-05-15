package by.dismess.core.network

import by.dismess.core.outer.NetworkInterface
import by.dismess.core.utils.fourBytesToInt
import by.dismess.core.utils.intToBytes
import by.dismess.core.utils.twoBytesToInt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

data class Connection(var socket: Socket) {
    var lastSendingTime = System.currentTimeMillis()
    var isUsing = true
}

class TCPNetworkInterfaceImpl : NetworkInterface {
    private lateinit var serverSocket: ServerSocket
    private var stopped: Boolean = true
    private lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit
    private var serverAddress: InetSocketAddress? = null
    private var connections: CopyOnWriteArrayList<Connection> = CopyOnWriteArrayList()

    private suspend fun run() {
        while (!stopped) {
            val socket = serverSocket.accept()
            val connection = Connection(socket)
            connections.add(connection)
            GlobalScope.launch { handleConnection(connection) }
        }
    }

    private suspend fun handleConnection(connection: Connection) {
        val stream = connection.socket.getInputStream()
        val senderAddress = InetAddress.getByAddress(stream.readNBytes(4))
        val senderPort = twoBytesToInt(stream.readNBytes(2))
        val sender = InetSocketAddress(senderAddress, senderPort)
        var dataSize = fourBytesToInt(stream.readNBytes(4))
        while (dataSize > 0) {
            connection.lastSendingTime = System.currentTimeMillis()
            val data = stream.readNBytes(dataSize)
            receiver(sender, data)
            dataSize = fourBytesToInt(stream.readNBytes(4))
        }
        connection.isUsing = false
        stream.close()
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
        for (connection in connections) {
//            while (connection.isUsing) {}
            connection.socket.close()
        }
        serverSocket.close()
    }

    private fun getConnection(address: InetSocketAddress): Pair<Connection, Boolean> {
        val connectionIndx = connections.indexOfFirst {
            it.socket.inetAddress == address.address && it.socket.port == address.port
        }
        val connection = if (connectionIndx != -1) {
            Pair(connections[connectionIndx], false)
        } else {
            val newConnection = Connection(Socket(address.address, address.port))
            connections.add(newConnection)
            Pair(newConnection, true)
        }
        return connection
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        val (connection, isNew) = getConnection(address)
        if (serverAddress == null) {
            serverAddress = InetSocketAddress(findFreePort())
        }
        connection.isUsing = true
        val stream = connection.socket.getOutputStream()
        if (isNew) {
            stream.write(serverAddress!!.address.address)
            stream.write(intToBytes(serverAddress!!.port, 2))
        }
        stream.write(intToBytes(data.size, 4))
        stream.write(data)
        stream.close()
        connection.isUsing = false
    }

    override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
        this.receiver = receiver
    }
}
