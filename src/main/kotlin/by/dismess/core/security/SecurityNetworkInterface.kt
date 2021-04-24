package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import java.net.InetAddress
import java.net.InetSocketAddress

/**
*                           DISMESS PROTOCOL
*
*                       Protocol for message
*                +------+-------------+------------+....+----+
*                |   0  | NEXT OFFSET |     MESSAGE     |NULL|
*                +------+-------------+------------+....+----+
# of bytes:	         1        1            variable       1
*
*                       Protocol for key
*                +------+--------------------------+....+----+
*                |   1  |               KEY             |NULL|
*                +------+--------------------------+....+----+
# of bytes:	         1               variable             1
*
* Key is sent without encryption.
**/

class SecurityNetworkInterface(
    private val networkInterface: NetworkInterface
) : NetworkInterface {
    private val sessionManager: SessionManager = SessionManager()

    private fun tryUpdateKey(address: InetSocketAddress) {
        val key = sessionManager.tryUpdateKey(address) ?: return
        TODO("Exchange keys")
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        // mutex lock
        tryUpdateKey(address)
        val encryptedMessage = sessionManager.encrypt(address, data)
        networkInterface.sendRawMessage(address, encryptedMessage)
    }

    override suspend fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit) {
        // mutex lock
        TODO("Not yet implemented")
    }
}
