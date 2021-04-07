package by.dismess.core.outer

import java.net.InetAddress
import java.net.InetSocketAddress

interface NetworkInterface {
    fun sendRawMessage(address: InetSocketAddress, data: ByteArray)
    fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit)
}
