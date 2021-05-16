package by.dismess.core.services

import by.dismess.core.outer.StorageInterface
import by.dismess.core.utils.gson

class StorageService(
    val storageInterface: StorageInterface
) {
    suspend fun exists(key: String) = storageInterface.exists(key)
    suspend fun save(key: String, data: Any) {
        var save = gson.toJson(data)
        if (save.startsWith('"')) {
            save = save.slice(1..save.length - 2)
        }
        storageInterface.saveRawData(key, save.toByteArray())
    }

    suspend inline fun <reified T> load(key: String): T? =
        storageInterface.loadRawData(key)?.let {
            val data = String(it)
            println("des" + data)
            println(T::class.java.canonicalName)
            gson.fromJson(data, T::class.java)
        }

    suspend fun saveRaw(key: String, data: String) {
        storageInterface.saveRawData(key, data.toByteArray())
    }

    suspend fun loadRaw(key: String) =
        storageInterface.loadRawData(key)?.let { String(it) }

    suspend fun forget(key: String) {
        storageInterface.forget(key)
    }
}
