package by.dismess.core.dht

import java.math.BigInteger

class Bucket(
    val border: BucketBorder
) {
    val data = mutableListOf<Pair<BigInteger, ByteArray>>()
}
