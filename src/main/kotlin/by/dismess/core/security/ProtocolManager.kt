package by.dismess.core.security

enum class DataType {
    SEND_BACK_KEY, KEY, MESSAGE
}

@Suppress("ArrayInDataClass")
data class TypedData(val type: DataType, val data: ByteArray)

class ProtocolManager {
    var lastUpdateTime = System.currentTimeMillis()
    private val encryptor: Encryptor = Encryptor()

    private fun decrypt(data: ByteArray): ByteArray {
        return encryptor.decrypt(data)
    }

    private fun processKey(data: ByteArray): TypedData {
        val order = data[0]
        var type = DataType.SEND_BACK_KEY
        if (order == 1.toByte()) {
            type = DataType.KEY
        }
        encryptor.updateKey()
        encryptor.setReceiverPublicKey(data.sliceArray(1 until data.size))
        return TypedData(type, encryptor.publicKeyBytes(updateSession = false))
    }

    /**
     * Init new session
     * @return 1 byte joined with new public key
     */
    fun updateKey(sendBack: Boolean = true): ByteArray {
        encryptor.updateKey()
        lastUpdateTime = System.currentTimeMillis()
        var secondByte = 0.toByte()
        if (!sendBack) {
            secondByte = 1.toByte()
        }
        return byteArrayOf(1, secondByte) + encryptor.publicKeyBytes(updateSession = true)
    }

    /**
     * @return 0 byte joined with encrypted message
     */
    fun encrypt(data: ByteArray): ByteArray {
        return byteArrayOf(0) + encryptor.encrypt(data)
    }

    /**
     * Decide which type of message received: key or raw message and run appropriate handlers
     * @return null if message type is key, decrypted message otherwise
     */
    fun processData(data: ByteArray): TypedData {
        if (data.first() == 1.toByte()) {
            return processKey(data.sliceArray(1 until data.size))
        }
        return TypedData(DataType.MESSAGE, decrypt(data.sliceArray(1 until data.size)))
    }
}
