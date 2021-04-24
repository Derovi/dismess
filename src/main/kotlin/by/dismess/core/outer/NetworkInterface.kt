package by.dismess.core.outer

import java.net.InetAddress
import java.net.InetSocketAddress

interface NetworkInterface {
    suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray)
    suspend fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit)
}
