package by.dismess.core.outer

interface StorageInterface {
    suspend fun saveRawData(key: String, data: ByteArray)
    suspend fun loadRawData(key: String): ByteArray
}
