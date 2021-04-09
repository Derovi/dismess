package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.network.NetworkMessage
import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkService(
    private val networkInterface: NetworkInterface
) {
    /**
     * Tag to list of registered handlers
     * @note You can use several handlers with one tag for debugging
     */
    private val handlers = mutableMapOf<String, MutableList<MessageHandler>>()
    init {
        networkInterface.setMessageReceiver { sender, data ->
            val message = klaxon.parse<NetworkMessage>(String(data)) ?: return@setMessageReceiver
            message.sender = sender
            for (handler in handlers[message.tag] ?: emptyList()) {
                handler(message)
            }
        }
    }
    /**
     * Each part of Core, that logically has its own handler, must have its own tag
     * @example DHT has tag "DHT"
     */
    fun registerHandler(tag: String, handler: MessageHandler) {
        handlers[tag]?.add(handler) ?: run { handlers[tag] = mutableListOf(handler) }
    }
    suspend fun sendMessage(address: InetSocketAddress, tag: String, data: Any) {
        sendMessage(address, tag, klaxon.toJsonString(data))
    }
    suspend fun sendMessage(address: InetSocketAddress, tag: String, data: String) {
        sendMessage(address, NetworkMessage(tag, data))
    }
    suspend fun sendMessage(address: InetSocketAddress, message: NetworkMessage) {
        networkInterface.sendRawMessage(address, klaxon.toJsonString(message).toByteArray())
    }

    /**
     * Waits for a message, but
     * returns null if timeout
     */
    suspend fun waitForAMessage(tag: String, timeout: Long = 1000L) : NetworkMessage? =
            withTimeoutOrNull(timeout) {
                suspendCoroutine { continuation ->
                    registerHandler(tag) { message ->
                        continuation.resume(message)
                    }
                }
            }

    companion object {
        fun randomTag() = UUID.randomUUID().toString()
    }
}

typealias MessageHandler = (message: NetworkMessage) -> Unit
