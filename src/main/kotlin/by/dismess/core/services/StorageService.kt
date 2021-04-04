package by.dismess.core.services

interface StorageService {
    suspend fun saveRawData(key: String, data: ByteArray)
    suspend fun loadRawData(key: String): ByteArray
}
