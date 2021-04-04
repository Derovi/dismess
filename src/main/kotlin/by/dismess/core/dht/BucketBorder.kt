package by.dismess.core.dht

import java.math.BigInteger

/**
 * The border describes the segment the bucket is responsible for
 * @param left id and
 * @param right id are included in segment
 */
data class BucketBorder(
    val left: BigInteger,
    val right: BigInteger
) {
    fun contains(id: BigInteger) = id in left..right
}
