package by.dismess.core.outer

interface StorageInterface {
    suspend fun exists(key: String): Boolean
    suspend fun saveRawData(key: String, data: ByteArray)
    suspend fun loadRawData(key: String): ByteArray
}
