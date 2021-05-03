package by.dismess.core.dht

import by.dismess.core.model.UserID
import java.net.InetSocketAddress

class Bucket(
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<UserID, InetSocketAddress>()
}
