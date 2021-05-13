package by.dismess.core.common

import java.net.InetSocketAddress

class VirtualNetwork {
    private val networkInterfaces = mutableMapOf<InetSocketAddress, VirtualNetworkInterface>()

    fun register(networkInterface: VirtualNetworkInterface) {
        networkInterfaces[networkInterface.ownAddress] = networkInterface
    }

    fun sendMessage(from: InetSocketAddress, to: InetSocketAddress, data: ByteArray) {
        networkInterfaces[to]?.receiver?.let { it(from, data) }
    }
}
