package by.dismess.core.services

import by.dismess.core.outer.NetworkInterface
import java.net.InetAddress

class NetworkService(
    networkInterface: NetworkInterface
) {
    /**
     * Tag to list of registered handlers
     */
    private val handlers = mutableMapOf<String, MutableList<(data: String) -> Unit>>()
    init {
        networkInterface.setMessageReceiver { sender, data ->
        }
    }
}
