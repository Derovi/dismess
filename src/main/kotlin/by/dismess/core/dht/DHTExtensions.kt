package by.dismess.core.dht

import by.dismess.core.klaxon
import java.math.BigInteger

infix fun BigInteger.inBucket(bucket: Bucket) = bucket.border.contains(this)

fun DHT.store(key: String, data: Any) {
    this.store(key, klaxon.toJsonString(data).toByteArray())
}

inline fun <reified T> DHT.load(key: String) = klaxon.parse<T>(String(this.retrieve(key)))
