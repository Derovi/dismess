package by.dismess.core.services

import by.dismess.core.network.MessageType
import by.dismess.core.network.NetworkMessage
import by.dismess.core.outer.NetworkInterface
import by.dismess.core.utils.gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.resume

/**
 * NetworkService is an implementation of L7 Dismess protocol
 * There are four types of messages - GET requests, POST requests, responses and approves
 * Each message has tag to determine message handler
 * GET request expects response (data)
 * POST request expects approve (just signal that request has been received)
 * Request has verificationTag - tag of response or approve
 */
class NetworkService(
    private val networkInterface: NetworkInterface
) {
    /**
     * Tag to list of registered handlers
     * @note You can use several handlers with one tag for debugging
     */
    private val getHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<GetHandler>>()
    private val postHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<PostHandler>>()
    private val responseHandlers = ConcurrentHashMap<String, CopyOnWriteArrayList<ResponseHandler>>()

    init {
        networkInterface.setMessageReceiver { sender, data ->
            val message = gson.fromJson(String(data), NetworkMessage::class.java) ?: return@setMessageReceiver
            if (message.type == MessageType.POST) {
                GlobalScope.launch { // send approve
                    message.verificationTag?.run {
                        sendResponse(sender, NetworkMessage(MessageType.APPROVE, this))
                    }
                }
            }
            message.sender = sender
            when (message.type) {
                MessageType.GET -> {
                    synchronized(getHandlers) {
                        for (handler in getHandlers[message.tag] ?: emptyList()) {
                            GlobalScope.launch {
                                GetContext(sender, message.verificationTag).handler(message)
                            }
                        }
                    }
                }
                MessageType.POST -> {
                    for (handler in postHandlers[message.tag] ?: emptyList()) {
                        GlobalScope.launch {
                            handler(message)
                        }
                    }
                }
                else -> {
                    for (handler in responseHandlers[message.tag] ?: emptyList()) {
                        handler(message)
                    }
                }
            }
        }
    }

    /**
     * Each part of Core, that logically has its own handler, must have its own tag
     * @example DHT has tag "DHT"
     */
    fun registerPost(tag: String, handler: PostHandler): PostHandler {
        postHandlers.getOrPut(tag) { CopyOnWriteArrayList() }.add(handler)
        return handler
    }

    fun registerGet(tag: String, handler: GetHandler): GetHandler {
        getHandlers.getOrPut(tag) { CopyOnWriteArrayList() }.add(handler)
        return handler
    }

    suspend fun sendPost(address: InetSocketAddress, tag: String, data: Any, timeout: Long = 1000): Boolean =
        sendPost(address, tag, gson.toJson(data), timeout)

    suspend fun sendPost(address: InetSocketAddress, tag: String, timeout: Long = 1000): Boolean =
        sendRequest(address, NetworkMessage(MessageType.POST, tag), timeout) != null

    suspend fun sendPost(address: InetSocketAddress, tag: String, data: String, timeout: Long = 1000): Boolean =
        sendRequest(address, NetworkMessage(MessageType.POST, tag, data), timeout) != null

    suspend fun sendGet(address: InetSocketAddress, tag: String, data: Any, timeout: Long = 1000): String? =
        sendGet(address, tag, gson.toJson(data), timeout)

    suspend fun sendGet(address: InetSocketAddress, tag: String, timeout: Long = 1000): String? =
        sendRequest(address, NetworkMessage(MessageType.GET, tag), timeout)?.data

    suspend fun sendGet(address: InetSocketAddress, tag: String, data: String, timeout: Long = 1000): String? =
        sendRequest(address, NetworkMessage(MessageType.GET, tag, data), timeout)?.data

    /**
     * Returns response if request delivered successfully, null if not
     */

    internal suspend fun sendRequest(
        address: InetSocketAddress,
        message: NetworkMessage,
        timeout: Long = 1000
    ): NetworkMessage? {
        message.verificationTag = randomTag()
        var handler: ResponseHandler? = null
        val result = withTimeoutOrNull<NetworkMessage>(timeout) {
            suspendCancellableCoroutine { continuation ->
                handler = { message: NetworkMessage ->
                    continuation.resume(message)
                }.also { responseHandlers.getOrPut(message.verificationTag!!) { CopyOnWriteArrayList() }.add(it) }
                GlobalScope.launch {
                    networkInterface.sendRawMessage(address, gson.toJson(message).toByteArray())
                }
            }
        }
        responseHandlers[message.verificationTag]?.remove(handler)
        return result
    }

    private suspend fun sendResponse(address: InetSocketAddress, message: NetworkMessage) {
        networkInterface.sendRawMessage(address, gson.toJson(message).toByteArray())
    }

    inner class GetContext(val target: InetSocketAddress, val verificationTag: String?) {
        suspend fun result(data: String) {
            if (verificationTag != null) {
                sendResponse(target, NetworkMessage(MessageType.RESULT, verificationTag, data))
            }
        }
        suspend fun result(data: Any) {
            if (verificationTag != null) {
                sendResponse(target, NetworkMessage(MessageType.RESULT, verificationTag, gson.toJson(data)))
            }
        }
    }

    companion object {
        fun randomTag() = UUID.randomUUID().toString()
    }
}

typealias GetHandler = suspend NetworkService.GetContext.(message: NetworkMessage) -> Unit
typealias PostHandler = suspend (message: NetworkMessage) -> Unit
typealias ResponseHandler = (message: NetworkMessage) -> Unit
