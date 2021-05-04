package by.dismess.core.security

import java.net.InetSocketAddress

class SessionManager() {
    private val addressToProtocolManager = mutableMapOf<InetSocketAddress, ProtocolManager>()
    var keyLifetimeMS: Int = 60000

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
        if (now - protocolManager.lastUpdateTime > keyLifetimeMS) {
            return protocolManager.updateKey()
        }
        return null
    }

    fun encrypt(address: InetSocketAddress, data: ByteArray): ByteArray =
        addressToProtocolManager.getValue(address).encrypt(data)

    /**
     * Define protocol manager for passed address, creates new if needed.
     * Forward data to defined protocol manager
     * @return TypedData:
     * type - whether data is a message, key with request to send it back or simple key,
     * data - processed data
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
