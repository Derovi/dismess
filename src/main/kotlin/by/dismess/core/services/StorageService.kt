package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.outer.StorageInterface

class StorageService(
    val storageInterface: StorageInterface
) {
    suspend fun save(key: String, data: Any) {
        storageInterface.saveRawData(key, klaxon.toJsonString(data).toByteArray())
    }
    suspend inline fun <reified T> load(key: String) = klaxon.parse<T>(String(storageInterface.loadRawData(key)))
    suspend fun saveText(key: String, data: String) {
        storageInterface.saveRawData(key, data.toByteArray())
    }
    suspend fun loadText(key: String) = String(storageInterface.loadRawData(key))
}
