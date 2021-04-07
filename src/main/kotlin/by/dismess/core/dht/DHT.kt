package by.dismess.core.dht

import java.math.BigInteger

interface DHT {
    fun store(key: String, data: ByteArray)
    fun getKeyBucket(key: String): Bucket
    fun findNodes(target: String, count: Int, maxDistance: BigInteger): Bucket
    fun retrieve(key: String): ByteArray
}
