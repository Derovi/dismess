package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.network.NetworkMessage
import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.util.UUID
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
    fun registerHandler(tag: String, handler: MessageHandler): MessageHandler {
        handlers[tag]?.add(handler) ?: run { handlers[tag] = mutableListOf(handler) }
        return handler
    }
    fun forgetHandler(tag: String, handler: MessageHandler) {
        handlers[tag]?.remove(handler)
    }
    suspend fun sendPost(address: InetSocketAddress, tag: String, data: Any, timeout: Long = 1000): Boolean =
        sendPost(address, tag, klaxon.toJsonString(data), timeout)

    suspend fun sendPost(address: InetSocketAddress, tag: String, data: String, timeout: Long = 1000): Boolean =
        sendPost(address, NetworkMessage(tag, data), timeout)

    /**
     * Returns true if message delivered successfully, false if not
     */
    suspend fun sendPost(address: InetSocketAddress, message: NetworkMessage, timeout: Long = 1000): Boolean {
        message.verificationTag = randomTag()
        networkInterface.sendRawMessage(address, klaxon.toJsonString(message).toByteArray())
        return waitForAMessage(message.verificationTag!!, timeout) != null
    }

    /**
     * Returns response if message delivered successfully, null if not
     */
    suspend fun sendGet(address: InetSocketAddress, message: NetworkMessage, timeout: Long = 1000): NetworkMessage? {
        message.verificationTag = randomTag()
        networkInterface.sendRawMessage(address, klaxon.toJsonString(message).toByteArray())
        return waitForAMessage(message.verificationTag!!, timeout)
    }

    /**
     * Waits for a message, but
     * returns null if timeout
     */
    suspend fun waitForAMessage(tag: String, timeout: Long = 1000L): NetworkMessage? {
        var handler: MessageHandler? = null
        val result = withTimeoutOrNull<NetworkMessage>(timeout) {
            suspendCoroutine { continuation ->
                handler = registerHandler(tag) { message ->
                    continuation.resume(message)
                }
            }
        }
        handler?.let { forgetHandler(tag, it) }
        return result
    }

    companion object {
        fun randomTag() = UUID.randomUUID().toString()
    }
}

typealias MessageHandler = (message: NetworkMessage) -> Unit
