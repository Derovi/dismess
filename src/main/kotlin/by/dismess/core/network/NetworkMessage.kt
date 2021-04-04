package by.dismess.core.network

import com.beust.klaxon.Json
import java.net.InetAddress

data class NetworkMessage(
    var tag: String,
    var data: String
) {
    @Json(ignored = true)
    lateinit var senderAddress: InetAddress
}
