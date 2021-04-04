package by.dismess.core.dht

interface DHT {
    fun store(key: String, data: ByteArray)
    fun retrieve(key: String): ByteArray
}
