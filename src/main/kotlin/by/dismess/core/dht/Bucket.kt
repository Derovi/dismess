package by.dismess.core.dht

import java.math.BigInteger
import java.net.InetSocketAddress

class Bucket(
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<BigInteger, InetSocketAddress>()
}
