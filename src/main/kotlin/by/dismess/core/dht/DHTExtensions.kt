package by.dismess.core.dht

import by.dismess.core.utils.UniqID

data class FindRequest(val targetUser: UniqID, val sender: UniqID)
data class StoreRequest(val key: String, val data: ByteArray)

infix fun UniqID.inBucket(bucket: Bucket) = bucket.border.contains(this)

infix fun UniqID.distanceTo(userID: UniqID) = this xor userID

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
    val usersList = bucket.idToIP.toList().sortedBy { it.first }
    val newBucketBorder = usersList[usersList.size / 2].first
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
