package by.dismess.core.dht

import by.dismess.core.model.UserID
import java.net.InetSocketAddress

class Bucket(
    val border: BucketBorder
) {
    val idToIP = mutableMapOf<UserID, InetSocketAddress>()
    var lastPingTime = System.currentTimeMillis()

    fun printBucketData() {
        println("Borders: " + border.left + " " + border.right)
        println("Users count: ${idToIP.size}")
        for (user in idToIP) {
            println("User: " + user.key + " " + user.value)
        }
    }
}
