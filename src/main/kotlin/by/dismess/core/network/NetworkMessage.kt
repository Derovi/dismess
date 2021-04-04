package by.dismess.core.network

import java.net.InetAddress

data class NetworkMessage(
    var tag: String,
    var data: String
) {
    lateinit var senderAddress: InetAddress
}
