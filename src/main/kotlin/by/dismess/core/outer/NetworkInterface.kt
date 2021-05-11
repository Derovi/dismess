package by.dismess.core.outer

import java.net.InetSocketAddress

interface NetworkInterface {
    /**
     * Method should be suspend because sendRawMessage can encapsulate
     * waiting for response from receiver (e.g. for encryption)
     */
    suspend fun start(address: InetSocketAddress?)
    suspend fun stop()
    suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray)
    fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit)
}
