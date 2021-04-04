package by.dismess.core.dht

import by.dismess.core.services.NetworkInterface
import by.dismess.core.services.StorageService

class DHTImpl(
    val networkInterface: NetworkInterface,
    val storageInterface: StorageService
) : DHT {
    override fun store(key: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun retrieve(key: String): ByteArray {
        TODO("Not yet implemented")
    }
}
