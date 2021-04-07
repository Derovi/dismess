package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.network.NetworkMessage
import by.dismess.core.outer.NetworkInterface
import java.net.InetSocketAddress

class NetworkService(
    private val networkInterface: NetworkInterface
) {
    /**
     * Tag to list of registered handlers
     * @note You can use several handlers with one tag for debugging
     */
    private val handlers = mutableMapOf<String, MutableList<(message: NetworkMessage) -> Unit>>()
    init {
        networkInterface.setMessageReceiver { sender, data ->
            val message = klaxon.parse<NetworkMessage>(String(data)) ?: return@setMessageReceiver
            message.senderAddress = sender
            for (handler in handlers[message.tag] ?: emptyList()) {
                handler(message)
            }
        }
    }
    /**
     * Each part of Core, that logically has its own handler, must have its own tag
     * @example DHT has tag "DHT"
     */
    fun registerHandler(tag: String, handler: (message: NetworkMessage) -> Unit) {
        handlers[tag]?.add(handler) ?: run { handlers[tag] = mutableListOf(handler) }
    }
    fun sendMessage(address: InetSocketAddress, tag: String, data: Any) {
        sendMessage(address, tag, klaxon.toJsonString(data))
    }
    fun sendMessage(address: InetSocketAddress, tag: String, data: String) {
        val message = NetworkMessage(tag, data)
        networkInterface.sendRawMessage(address, klaxon.toJsonString(message).toByteArray())
    }
}
