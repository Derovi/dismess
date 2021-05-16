package by.dismess.core.dht

import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

class Bucket(
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<UniqID, InetSocketAddress>()
    var lastPingTime = System.currentTimeMillis()
}
