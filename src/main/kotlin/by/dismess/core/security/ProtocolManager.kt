package by.dismess.core.security


class ProtocolManager {
    val lastUpdateTime = System.currentTimeMillis()
    private val encryptor: Encryptor = Encryptor()

    private fun decrypt(data: ByteArray): ByteArray {
        return encryptor.decrypt(data)
    }

    private fun processKey(data: ByteArray) {
        TODO("Exchange keys")
    }

    fun updateKey(): ByteArray {
        encryptor.updateKey()
        return byteArrayOf(1) + encryptor.getPublicKey()
    }

    fun encrypt(data: ByteArray): ByteArray {
        return byteArrayOf(0) + encryptor.encrypt(data)
    }

    fun processData(data: ByteArray): ByteArray? {
        if (data[0].compareTo(1) == 0) {
            processKey(data.sliceArray(1 until data.size))
            return null
        }
        return decrypt(data.sliceArray(1 until data.size))
    }
}
