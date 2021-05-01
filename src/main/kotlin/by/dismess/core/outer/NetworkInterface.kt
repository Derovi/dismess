package by.dismess.core.outer

import java.net.InetSocketAddress

interface NetworkInterface {
    suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray)
    fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit)
}
