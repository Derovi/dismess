package by.dismess.core.common.dht

import by.dismess.core.utils.UniqID
import java.net.InetSocketAddress

class VirtualDHTCommon {
    val storage = mutableMapOf<String, ByteArray>()
    val users = mutableMapOf<UniqID, InetSocketAddress>()
}
