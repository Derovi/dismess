package by.dismess.core.services

import by.dismess.core.klaxon
import by.dismess.core.outer.StorageInterface

class StorageService(
    val storageInterface: StorageInterface
) {
    suspend fun exists(key: String) = storageInterface.exists(key)
    suspend fun save(key: String, data: Any) {
        var save = klaxon.toJsonString(data)
        if (save.startsWith('"')) {
            save = save.slice(1..save.length - 2)
        }
        storageInterface.saveRawData(key, save.toByteArray())
    }
    suspend inline fun <reified T> load(key: String) =
        storageInterface.loadRawData(key) ?.let {
            val data = String(it)
            when (T::class) {
                String::class -> data
                Long::class -> data.toLong()
                Int::class -> data.toInt()
                Short::class -> data.toShort()
                Double::class -> data.toDouble()
                Float::class -> data.toFloat()
                Byte::class -> data.toByte()
                else -> klaxon.parse<T>(data)
            }
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
