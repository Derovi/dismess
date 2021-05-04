package by.dismess.core.dht

import by.dismess.core.model.UserID
import java.math.BigInteger
import java.net.InetSocketAddress

class Bucket(
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<UserID, InetSocketAddress>()
    var lastPingTime = System.currentTimeMillis()

    fun split(): Pair<Bucket, Bucket> {
        val bucketBorderMid = (border.left + border.right) / BigInteger.TWO
        val leftHalf = Bucket(BucketBorder(border.left, bucketBorderMid))
        val rightHalf = Bucket(BucketBorder(leftHalf.border.right, border.right))
        idToIP.map {
            when {
                it.key inBucket leftHalf -> leftHalf.idToIP.put(it.key, it.value)
                it.key inBucket rightHalf -> rightHalf.idToIP.put(it.key, it.value)
                else -> Unit
            }
        }
        return Pair(leftHalf, rightHalf)
    }
}
