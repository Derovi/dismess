package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.network.NetworkMessage
import by.dismess.core.outer.NetworkInterface
import java.net.InetSocketAddress

class NetworkService(
    val networkInterface: NetworkInterface
) {
    /**
     * Tag to list of registered handlers
     */
    private val handlers = mutableMapOf<String, MutableList<(message: NetworkMessage) -> Unit>>()
        .withDefault { mutableListOf() }
    init {
        networkInterface.setMessageReceiver { sender, data ->
            val message = klaxon.parse<NetworkMessage>(String(data)) ?: return@setMessageReceiver
            message.senderAddress = sender
            for (handler in handlers[message.tag]!!) { // handlers has default mutableList
                handler(message)
            }
        }
    }
    fun registerHandler(tag: String, handler: (message: NetworkMessage) -> Unit) {
        handlers[tag]!!.add(handler) // has default mutableList
    }
    fun sendMessage(address: InetSocketAddress, tag: String, data: String) {
        val message = NetworkMessage(tag, data)
        networkInterface.sendRawMessage(address, klaxon.toJsonString(message).toByteArray())
    }
}
