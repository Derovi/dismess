package by.dismess.core.dht

import by.dismess.core.services.NetworkService
import by.dismess.core.services.StorageService

class DHTImpl(
    val networkService: NetworkService,
    val storageService: StorageService
) : DHT {
    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not yet implemented")
    }
}
