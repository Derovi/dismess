package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID

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
    val usersList = bucket.idToIP.toList().sortedBy { it.first.rawID }
    val newBucketBorder = usersList[usersList.size / 2].first.rawID
    val leftBucket = Bucket(BucketBorder(bucket.border.left, newBucketBorder))
    val rightBucket = Bucket(BucketBorder(newBucketBorder, bucket.border.right))
    bucket.idToIP.map {
        when {
            it.key inBucket leftBucket -> leftBucket.idToIP.put(it.key, it.value)
            it.key inBucket rightBucket -> rightBucket.idToIP.put(it.key, it.value)
            else -> Unit
        }
    }
    return Pair(leftBucket, rightBucket)
}

suspend fun DHT.store(key: String, data: Any) {
    this.store(key, klaxon.toJsonString(data).toByteArray())
}

suspend inline fun <reified T> DHT.load(key: String) = klaxon.parse<T>(String(this.retrieve(key)))
