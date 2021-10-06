package by.dismess.core.common

import by.dismess.core.outer.StorageInterface

class InMemoryStorageInterface : StorageInterface {
    private val storage = mutableMapOf<String, ByteArray>()

    override suspend fun exists(key: String): Boolean = storage.containsKey(key)

    override suspend fun forget(key: String) {
        storage.remove(key)
    }

    override suspend fun loadRawData(key: String): ByteArray? = storage[key]

    override suspend fun saveRawData(key: String, data: ByteArray) {
        storage[key] = data
    }
}
