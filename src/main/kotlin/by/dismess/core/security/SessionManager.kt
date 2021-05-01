package by.dismess.core.security

import java.net.InetSocketAddress

const val KEY_LIFETIME = 60000

class SessionManager {
    private val addressToProtocolManager = mutableMapOf<InetSocketAddress, ProtocolManager>()

    /**
     * Check session lifetime, create new session if needed
     * @param address socket correspond to session
     * @return if key was updated not more than KEY_LIFETIME ms before, null is returned,
     * new key otherwise
     */
    fun tryUpdateKey(address: InetSocketAddress): ByteArray? {
        val now = System.currentTimeMillis()
        var protocolManager: ProtocolManager? = addressToProtocolManager[address]
        if (protocolManager == null) {
            protocolManager = ProtocolManager()
            addressToProtocolManager[address] = protocolManager
            return protocolManager.updateKey()
        }
        if (now - protocolManager.lastUpdateTime > KEY_LIFETIME) {
            return protocolManager.updateKey()
        }
        return null
    }

    fun encrypt(address: InetSocketAddress, data: ByteArray): ByteArray {
        return addressToProtocolManager.getValue(address).encrypt(data)
    }

    /**
     * Process message if it is a key or raw message, create new session if needed
     * @return decrypted message, null if key was passed
     */
    fun processData(address: InetSocketAddress, data: ByteArray): TypedData {
        var protocolManager: ProtocolManager? = addressToProtocolManager[address]
        if (protocolManager == null) {
            protocolManager = ProtocolManager()
            addressToProtocolManager[address] = protocolManager
        }
        return protocolManager.processData(data)
    }

    suspend fun block(address: InetSocketAddress) {
        val protocolManager: ProtocolManager = addressToProtocolManager.getValue(address)
        protocolManager.channel.receive()
    }

    suspend fun release(address: InetSocketAddress) {
        val protocolManager: ProtocolManager = addressToProtocolManager.getValue(address)
        protocolManager.channel.send(Unit)
    }
}
