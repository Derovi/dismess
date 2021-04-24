package by.dismess.core.security

import java.net.InetSocketAddress

const val KEY_LIFETIME = 60000

class SessionManager {
    private val addressToProtocolManager = mutableMapOf<InetSocketAddress, ProtocolManager>()

    fun tryUpdateKey(address: InetSocketAddress): ByteArray? {
        val now = System.currentTimeMillis()
        val protocolManager: ProtocolManager = addressToProtocolManager[address] ?: return null
        if (now - protocolManager.lastUpdateTime < KEY_LIFETIME) {
            return null
        }
        return protocolManager.updateKey()
    }

    fun encrypt(address: InetSocketAddress, data: ByteArray): ByteArray {
        val protocolManager: ProtocolManager = addressToProtocolManager[address]!!
        return protocolManager.encrypt(data)
    }

    fun processData(address: InetSocketAddress, data: ByteArray): ByteArray? {
        var protocolManager: ProtocolManager? = addressToProtocolManager[address]
        if (protocolManager == null) {
            protocolManager = ProtocolManager()
            addressToProtocolManager[address] = protocolManager
        }
        return protocolManager.processData(data)
    }

}