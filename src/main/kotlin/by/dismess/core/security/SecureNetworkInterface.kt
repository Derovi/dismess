package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 *                           DISMESS PROTOCOL
 *
 *                       Protocol for message
 *                +------+-------------+-----------+------------+....+----+
 *                |   0  | NEXT OFFSET |  SESSION  |     MESSAGE     |NULL|
 *                +------+-------------+-----------+------------+....+----+
# of bytes:	         1        1              2           variable       1
 * SESSION is number of current session, goes unencrypted
 *
 *                       Protocol for key
 *                +------+-----------+-----------+--------------+....+----+
 *                |   1  |   ORDER   |  SESSION  |        KEY        |NULL|
 *                +------+-----------+-----------+--------------+....+----+
# of bytes:	         1        1              2           variable       1
 * ORDER is equal to 0 if receiver should send his public key back, 1 if shouldn't
 * KEY is sent without encryption.
 **/

class SecureNetworkInterface(
    private val networkInterface: NetworkInterface
) : NetworkInterface {
    private val sessionManager: SessionManager = SessionManager()

    private fun tryUpdateKey(address: InetSocketAddress): ByteArray? {
        return sessionManager.tryUpdateKey(address)
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        val updatedKey = tryUpdateKey(address)
        if (updatedKey != null) {
            networkInterface.sendRawMessage(address, updatedKey)
            TODO("BLOCK")
        }
        val encryptedMessage = sessionManager.encrypt(address, data)
        networkInterface.sendRawMessage(address, encryptedMessage)
    }

    override fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit) {
        TODO("Not yet implemented")
    }
}
