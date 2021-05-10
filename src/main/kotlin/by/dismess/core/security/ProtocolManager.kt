package by.dismess.core.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

enum class DataType {
    SEND_BACK_KEY, KEY, MESSAGE
}

@Suppress("ArrayInDataClass")
data class TypedData(val type: DataType, val data: ByteArray)

class ProtocolManager {
    private val encryptor: Encryptor = Encryptor()
    var lastUpdateTime = System.currentTimeMillis()
    val channel: Channel<Unit> = Channel()

    private suspend fun decrypt(data: ByteArray): ByteArray = encryptor.decrypt(data)

    @ExperimentalCoroutinesApi
    private suspend fun processKey(data: ByteArray): TypedData {
        val order = data[0]
        var type = DataType.SEND_BACK_KEY
        if (order == 1.toByte()) {
            type = DataType.KEY
        }
        encryptor.updateKey()
        var backOrder: Byte = 1
        val updated = encryptor.setReceiverPublicKey(data.sliceArray(1 until data.size))
        if (!updated) {
            backOrder = 0
        }
        val protocolPrefix: ByteArray = byteArrayOf(1, backOrder)
        return TypedData(type, protocolPrefix + encryptor.publicKeyBytes(updateSession = !updated))
    }

    /**
     * Init new session
     * @return 1 byte joined with new public key
     */
    suspend fun updateKey(): ByteArray {
        encryptor.updateKey()
        lastUpdateTime = System.currentTimeMillis()
        val order = 0.toByte()
        return byteArrayOf(1, order) + encryptor.publicKeyBytes(updateSession = true)
    }

    /**
     * @return 0 byte joined with encrypted message
     */
    suspend fun encrypt(data: ByteArray): ByteArray = byteArrayOf(0) + encryptor.encrypt(data)

    /**
     * Decide which type of message received: key or raw message and run appropriate handlers
     * @return null if message type is key, decrypted message otherwise
     */
    @ExperimentalCoroutinesApi
    suspend fun processData(data: ByteArray): TypedData {
        if (data.first() == 1.toByte()) {
            return processKey(data.sliceArray(1 until data.size))
        }
        return TypedData(DataType.MESSAGE, decrypt(data.sliceArray(1 until data.size)))
    }
}
