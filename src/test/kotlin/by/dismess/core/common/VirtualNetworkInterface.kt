package by.dismess.core.common

import by.dismess.core.outer.NetworkInterface
import java.net.InetSocketAddress

class VirtualNetworkInterface(val network: VirtualNetwork, val ownAddress: InetSocketAddress) : NetworkInterface {
    lateinit var receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit

    init {
        network.register(this)
    }

    override suspend fun start(address: InetSocketAddress?) {
        return
    }

    override suspend fun stop() {
        return
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        network.sendMessage(ownAddress, address, data)
    }

    override fun setMessageReceiver(receiver: (sender: InetSocketAddress, data: ByteArray) -> Unit) {
        this.receiver = receiver
    }
}
