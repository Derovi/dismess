package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID
import java.math.BigInteger

data class FindRequest(val targetUser: UserID, val sender: UserID)
data class StoreRequest(val key: String, val data: ByteArray)

infix fun UserID.inBucket(bucket: Bucket) = bucket.border.contains(this.rawID)

infix fun UserID.distanceTo(userID: UserID) = this.rawID xor userID.rawID

infix fun <K, V> MutableMap<K, V>.equalTo(map: MutableMap<K, V>): Boolean {
    if (this.size != map.size) {
        return false
    }

    this.forEach {
        if (!map.contains(it.key) || map[it.key] != it.value) {
            return false
        }
    }

    return true
}

fun splitBucket(bucket: Bucket): Pair<Bucket, Bucket> {
    val bucketBorderMid = (bucket.border.left + bucket.border.right) / BigInteger.TWO
    val leftHalf = Bucket(BucketBorder(bucket.border.left, bucketBorderMid))
    val rightHalf = Bucket(BucketBorder(leftHalf.border.right, bucket.border.right))
    bucket.idToIP.map {
        when {
            it.key inBucket leftHalf -> leftHalf.idToIP.put(it.key, it.value)
            it.key inBucket rightHalf -> rightHalf.idToIP.put(it.key, it.value)
            else -> Unit
        }
    }
    return Pair(leftHalf, rightHalf)
}

suspend fun DHT.store(key: String, data: Any) {
    this.store(key, klaxon.toJsonString(data).toByteArray())
}

suspend inline fun <reified T> DHT.load(key: String) = klaxon.parse<T>(String(this.retrieve(key)))
