package by.dismess.core.network

import com.beust.klaxon.Json
import java.net.InetAddress

enum class MessageType {
    /** request */
    GET,
    POST,
    /** response */
    RESULT,
    APPROVE
}

class NetworkMessage(
    var type: MessageType,
    var tag: String,
    var data: String? = null,
    var verificationTag: String? = null // null for responses
) {
    @Json(ignored = true)
    lateinit var sender: InetAddress

    fun isRequest() = type == MessageType.GET || type == MessageType.POST
    fun isResponse() = !isRequest()
}
