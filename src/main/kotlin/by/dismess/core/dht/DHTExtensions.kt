package by.dismess.core.dht

import by.dismess.core.klaxon
import by.dismess.core.model.UserID

infix fun UserID.inBucket(bucket: Bucket) = bucket.border.contains(this.rawID)

infix fun UserID.distanceTo(userID: UserID) = this.rawID xor userID.rawID

fun DHT.store(key: String, data: Any) {
    this.store(key, klaxon.toJsonString(data).toByteArray())
}

data class FindRequest(val targetUser: UserID, val sender: UserID) {}

inline fun <reified T> DHT.load(key: String) = klaxon.parse<T>(String(this.retrieve(key)))
