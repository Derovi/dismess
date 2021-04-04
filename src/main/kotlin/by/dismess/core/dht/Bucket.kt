package by.dismess.core.dht

import java.math.BigInteger
import java.util.*

data class Bucket(
    val leftBorder : BigInteger,
    val rightBorder : BigInteger,
) {
    var data = mutableListOf<Map.Entry<String, ByteArray>>()
}