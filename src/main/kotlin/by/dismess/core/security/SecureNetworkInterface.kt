package by.dismess.core.security

import by.dismess.core.outer.NetworkInterface
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 *                           DISMESS PROTOCOL
 *
 *                       Protocol for message
 *                +------+-----------+-------------+------------+....+----+
 *                |   0  |  SESSION  | NEXT OFFSET |     MESSAGE     |NULL|
 *                +------+-----------+-------------+------------+....+----+
# of bytes:	         1        2              1           variable       1
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

    private fun inetAddressToInetSocketAddress(address: InetAddress): InetSocketAddress {
        return InetSocketAddress(1)
    }

    override suspend fun sendRawMessage(address: InetSocketAddress, data: ByteArray) {
        val updatedKey = tryUpdateKey(address)
        if (updatedKey != null) {
            networkInterface.sendRawMessage(address, updatedKey)
            sessionManager.block(address)
        }
        val encryptedMessage = sessionManager.encrypt(address, data)
        networkInterface.sendRawMessage(address, encryptedMessage)
    }

    override fun setMessageReceiver(receiver: (sender: InetAddress, data: ByteArray) -> Unit) {
        networkInterface.setMessageReceiver { sender: InetAddress, data: ByteArray ->
            val senderSocketAddress = inetAddressToInetSocketAddress(sender)
            val response: TypedData = sessionManager.processData(senderSocketAddress, data)
            when (response.type) {
                DataType.MESSAGE -> {
                    receiver(sender, response.data)
                }
                DataType.SEND_BACK_KEY -> {
                    GlobalScope.launch {
                        networkInterface.sendRawMessage(senderSocketAddress, response.data)
                    }
                }
                DataType.KEY -> {
                    GlobalScope.launch {
                        sessionManager.release(senderSocketAddress)
                    }
                }
            }
        }
    }
}
